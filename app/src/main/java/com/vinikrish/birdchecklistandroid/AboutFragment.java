package com.vinikrish.birdchecklistandroid;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        
        // Update version information dynamically
        TextView versionTextView = view.findViewById(R.id.version_text);
        if (versionTextView != null) {
            try {
                PackageInfo packageInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
                String versionText = "Version " + packageInfo.versionName + " (Build " + packageInfo.versionCode + ")\nDeveloped for bird enthusiasts worldwide";
                versionTextView.setText(versionText);
            } catch (PackageManager.NameNotFoundException e) {
                versionTextView.setText("Version information unavailable\nDeveloped for bird enthusiasts worldwide");
            }
        }
        
        return view;
    }
}