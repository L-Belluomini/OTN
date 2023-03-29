package com.atakmap.android.OTN;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import com.atakmap.android.gui.ColorPalette;
import com.atakmap.android.gui.ImportFileBrowserDialog;
import com.atakmap.android.gui.PanEditTextPreference;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.preference.PluginPreferenceFragment;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;

import java.io.File;

import gov.tak.platform.graphics.Color;

public class OTNPreferenceFragment extends PluginPreferenceFragment {

    private static Context staticPluginContext;
    public static final String TAG = "OTNPreferenceFragment";

    /*
     * Only will be called after this has been instantiated with the 1-arg constructor.
     * Fragments must has a zero arg constructor.
     */
    public OTNPreferenceFragment() {
        super(staticPluginContext, R.xml.preferences);
    }

    @SuppressLint("ValidFragment")
    public OTNPreferenceFragment(final Context pluginContext) {
        super(pluginContext, R.xml.preferences);
        staticPluginContext = pluginContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Preference defaultGraphColor = findPreference(
                "defaultGraphColor");
        defaultGraphColor
                .setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(
                                    Preference preference) {

                                final SharedPreferences _prefs = PreferenceManager
                                        .getDefaultSharedPreferences(
                                                getActivity());

                                AlertDialog.Builder b = new AlertDialog.Builder(
                                        getActivity());
                                b.setTitle(defaultGraphColor.getTitle());
                                int color = Color.WHITE;
                                try {
                                    color = Integer.parseInt(_prefs.getString(
                                            "defaultGraphColor",
                                            Integer.toString(Color.WHITE)));
                                } catch (Exception e) {
                                    Log.d(TAG,
                                            "error occurred getting preference");
                                }
                                ColorPalette palette = new ColorPalette(
                                        getActivity(), color);
                                b.setView(palette);
                                final AlertDialog alert = b.create();
                                ColorPalette.OnColorSelectedListener l = new ColorPalette.OnColorSelectedListener() {
                                    @Override
                                    public void onColorSelected(int color,
                                                                String label) {
                                        _prefs.edit()
                                                .putString("defaultGraphColor",
                                                        Integer.toString(color))
                                                .apply();
                                        alert.dismiss();
                                    }
                                };
                                palette.setOnColorSelectedListener(l);
                                alert.show();
                                return true;
                            }
                        });

        //final ListPreference routingInstructionLang = findPreference("RoutingInstructionLang");


        /*try {
            ((PanEditTextPreference) findPreference("key_for_helloworld"))
                    .checkValidInteger();
        } catch (Exception ignored) {
        }
        findPreference("test_file_browser")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {

                        ImportFileBrowserDialog.show("Test File Browser",
                                null,
                                new String[] {
                                        ".txt"
                                },
                                new ImportFileBrowserDialog.DialogDismissed() {
                                    public void onFileSelected(
                                            final File file) {
                                        if (FileSystemUtils.isFile(file)) {
                                            Toast.makeText(getActivity(),
                                                    "file: " + file,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    public void onDialogClosed() {
                                        //Do nothing
                                    }
                                }, HelloWorldPreferenceFragment.this
                                        .getActivity());
                        return true;
                    }
                });

        // launch nested pref screen on click
        findPreference("nested_pref")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference p) {
                        HelloWorldPreferenceFragment.this
                                .showScreen(new HelloWorldSubPreferenceFragment(
                                        staticPluginContext));
                        return true;
                    }
                });*/
    }

    @Override
    public String getSubTitle() {
        return getSubTitle("Tool Preferences", "OTN Preferences");
    }
}
