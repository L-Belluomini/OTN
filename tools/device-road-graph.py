#!/usr/bin/env python3
"""Prepare/remove one owned test graph and restore only its OTN selection preference.

ATAK must be stopped after an idle-navigation preflight. Keep the private manifest
outside Git. This helper does not stop/start ATAK, install APKs or clear app data.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shlex
import subprocess
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('action', choices=['prepare', 'restore'])
parser.add_argument('--serial', required=True)
parser.add_argument('--manifest', type=Path, required=True)
parser.add_argument('--graph', type=Path)
parser.add_argument('--name', default='Codex-Qualification-road-9775dc3')
parser.add_argument('--adb', default=str(Path.home() / 'Library/Android/sdk/platform-tools/adb'))
args = parser.parse_args()
host = 'com.atakmap.app.civ'
key = 'OTNSelectedGraph'
graph_root = '/sdcard/atak/tools/OTN/graphs/'

def adb(*command, check=True, content=None):
    result = subprocess.run([args.adb, '-s', args.serial, *command], input=content,
                            capture_output=True, timeout=120)
    if check and result.returncode:
        raise RuntimeError('ADB failed: ' + repr(command) + ': ' + result.stderr.decode(errors='replace'))
    return result

if adb('shell', 'pidof', host, check=False).returncode == 0:
    raise SystemExit('Stop idle ATAK before modifying its test graph or preferences')
if not re.fullmatch(r'Codex-Qualification-[a-z0-9-]+', args.name):
    raise SystemExit('Invalid owned test-graph name')
target = graph_root + args.name

def read_preferences(name):
    if not re.fullmatch(r'[A-Za-z0-9_.-]+\.xml', name):
        raise RuntimeError('Invalid preference filename')
    path = 'shared_prefs/' + name
    backup = adb('shell', 'run-as', host, 'test', '-f', path + '.bak', check=False)
    effective = path + '.bak' if backup.returncode == 0 else path
    raw = adb('exec-out', 'run-as', host, 'cat', effective).stdout
    return ET.fromstring(raw), raw

if args.action == 'prepare':
    if args.manifest.exists() or not args.graph or not (args.graph / 'config.json').is_file():
        raise SystemExit('Require a new private manifest and a prepared graph')
    if adb('shell', 'test', '-e', target, check=False).returncode == 0:
        raise SystemExit('Refusing to overwrite an existing graph directory')
    names = adb('shell', 'run-as', host, 'ls', 'shared_prefs').stdout.decode().splitlines()
    preferences = []
    for name in names:
        if not re.fullmatch(r'[A-Za-z0-9_.-]+\.xml', name):
            continue
        tree, raw = read_preferences(name)
        original = next((node for node in tree if node.get('name') == key), None)
        if original is not None or name == host + '_preferences.xml':
            preferences.append({'file': name, 'original': ET.tostring(original, encoding='unicode')
                                if original is not None else None})
    if not preferences:
        raise SystemExit('ATAK preference storage was not found')
    files = sorted(args.graph.iterdir())
    if any(not p.is_file() or p.is_symlink() or not re.fullmatch(r'[A-Za-z0-9_.-]+', p.name) for p in files):
        raise SystemExit('Graph contains an unsupported file/directory')
    record = {'serial': args.serial, 'graphName': args.name, 'target': target, 'preferences': preferences,
              'files': {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in files}, 'prepared': False,
              'restored': False, 'scope': 'One disposable routing graph; exact original OTN selection only.'}
    args.manifest.parent.mkdir(parents=True, exist_ok=True)
    args.manifest.write_text(json.dumps(record, indent=2) + '\n')
    args.manifest.chmod(0o600)
    adb('shell', 'mkdir', '-p', target)
    for path in files:
        adb('push', str(path), target + '/' + path.name)
    for name, expected in record['files'].items():
        actual = adb('shell', 'sha256sum', target + '/' + name).stdout.decode().split()[0]
        if actual != expected:
            raise RuntimeError('Graph transfer hash mismatch')
    record['prepared'] = True
else:
    record = json.loads(args.manifest.read_text())
    if record['serial'] != args.serial or record['target'] != target or record['graphName'] != args.name:
        raise SystemExit('Private manifest does not identify this device/test graph')
    if record['restored']:
        raise SystemExit('This graph has already been restored; no repeated mutation')
    exists = adb('shell', 'test', '-d', target, check=False).returncode == 0
    if exists:
        files = set(adb('shell', 'ls', '-1', target).stdout.decode().splitlines())
        if not files.issubset(set(record['files']) | {'gh.lock'}):
            raise SystemExit('Unexpected content in owned graph; inspect before cleanup')
        adb('shell', 'rm', '-rf', target)
        if adb('shell', 'test', '-e', target, check=False).returncode == 0:
            raise RuntimeError('Owned graph cleanup failed')
    for preference in record['preferences']:
        tree, current = read_preferences(preference['file'])
        for node in list(tree):
            if node.get('name') == key:
                tree.remove(node)
        if preference['original'] is not None:
            tree.append(ET.fromstring(preference['original']))
        encoded = ET.tostring(tree, encoding='utf-8', xml_declaration=True)
        path = 'shared_prefs/' + preference['file']
        operation = 'cat > ' + path + '.auto-test-tmp && mv ' + path + '.auto-test-tmp ' + path + ' && rm -f ' + path + '.bak'
        adb('shell', 'run-as ' + host + ' sh -c ' + shlex.quote(operation), content=encoded)
        checked, _ = read_preferences(preference['file'])
        restored = next((node for node in checked if node.get('name') == key), None)
        expected = ET.fromstring(preference['original']) if preference['original'] is not None else None
        if (restored is None) != (expected is None) or (expected is not None and
                (restored.tag, restored.attrib, restored.text) != (expected.tag, expected.attrib, expected.text)):
            raise RuntimeError('Original OTN selection did not restore')
        # Verify semantic preservation of every unrelated preference.
        original_tree = ET.fromstring(current)
        def semantic(node):
            return (node.tag, node.attrib, node.text, tuple(semantic(child) for child in node))
        unchanged = lambda root: {n.get('name'): semantic(n) for n in root if n.get('name') != key}
        if unchanged(original_tree) != unchanged(checked):
            raise RuntimeError('An unrelated preference changed during restore')
    record['restored'] = True
args.manifest.write_text(json.dumps(record, indent=2) + '\n')
print(json.dumps({'action': args.action, 'ownedGraph': args.name, 'prepared': record['prepared'],
                  'restored': record['restored'], 'unrelatedPreferencesOverwritten': False}))
