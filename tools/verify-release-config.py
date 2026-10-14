#!/usr/bin/env python3
"""Execute the original graph JSON contract against R8-obfuscated GraphHopper models.

Run ./gradlew -p tools/graph-fixture writeRuntimeClasspath first. This is a JVM
serialization regression; it does not qualify the Android release APK or engine.
"""
import argparse
import json
from pathlib import Path
import subprocess
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--r8-jar', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
project = Path(__file__).resolve().parents[1]
fixture = project / 'tools/graph-fixture/build'
classpath = [Path(s) for s in (fixture / 'runtime-classpath.txt').read_text().splitlines() if s]
args.output.mkdir(parents=True, exist_ok=True)
program = args.output / 'config-program.jar'
models = ['com/graphhopper/GraphHopperConfig.class', 'com/graphhopper/util/PMap.class',
          'com/graphhopper/config/Profile.class', 'com/graphhopper/config/CHProfile.class',
          'com/graphhopper/config/LMProfile.class']
with zipfile.ZipFile(program, 'w') as target:
    for name in models:
        for dependency in classpath:
            with zipfile.ZipFile(dependency) as source:
                if name in source.namelist():
                    target.writestr(name, source.read(name))
                    break
        else:
            raise SystemExit('Missing GraphHopper model: ' + name)
    name = 'fixture/ReleaseConfigContract.class'
    target.writestr(name, (fixture / 'classes/java/main' / name).read_bytes())
rules = (project / 'app/proguard-gson-config.txt').read_text()
java = str(args.java_home / 'bin/java')
results = []
for name, policy in [('legacy', '\n'.join(s for s in rules.splitlines() if 'com.graphhopper.util.PMap' not in s)),
                     ('fixed', rules)]:
    config = args.output / (name + '.pro')
    config.write_text('-dontshrink\n-dontoptimize\n-keepattributes Signature\n'
                      '-repackageclasses fixture.obfuscated\n'
                      '-keep class fixture.ReleaseConfigContract { public static void main(java.lang.String[]); }\n' + policy)
    output = args.output / (name + '.jar')
    command = [java, '-cp', str(args.r8_jar), 'com.android.tools.r8.R8', '--classfile',
               '--release', '--lib', str(args.java_home), '--pg-conf', str(config),
               '--output', str(output), '--pg-map-output', str(args.output / (name + '-mapping.txt'))]
    for dependency in classpath:
        command += ['--classpath', str(dependency)]
    command.append(str(program))
    compiled = subprocess.run(command, capture_output=True, text=True, timeout=120)
    (args.output / (name + '-r8.log')).write_text(compiled.stdout + compiled.stderr)
    if compiled.returncode:
        raise SystemExit('R8 fixture compilation failed: ' + str(args.output / (name + '-r8.log')))
    runtime = ':'.join([str(output)] + [str(p) for p in classpath])
    executed = subprocess.run([java, '-cp', runtime, 'fixture.ReleaseConfigContract'],
                              capture_output=True, text=True, timeout=30)
    (args.output / (name + '-runtime.log')).write_text(executed.stdout + executed.stderr)
    results.append({'policy': name, 'compileExit': compiled.returncode, 'runtimeExit': executed.returncode,
                    'configurationPreserved': 'RELEASE_GSON_CONFIG_PASS' in executed.stdout})
if results[0]['configurationPreserved'] or results[0]['runtimeExit'] == 0:
    raise SystemExit('Legacy field-loss regression was not reproduced')
if not results[1]['configurationPreserved'] or results[1]['runtimeExit'] != 0:
    raise SystemExit('Fixed field contract failed; see runtime log')
report = {'scope': 'R8 classfile/Gson graph configuration contract on JVM, not Android engine qualification',
          'r8Version': subprocess.check_output([java, '-cp', str(args.r8_jar), 'com.android.tools.r8.R8', '--version'], text=True).strip(),
          'results': results}
(args.output / 'report.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))
