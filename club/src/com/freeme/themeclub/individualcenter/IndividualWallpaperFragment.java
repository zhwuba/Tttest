package com.freeme.themeclub.individualcenter;

import com.freeme.themeclub.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class IndividualWallpaperFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(
                R.layout.fragment_individual_wallpaper, container, false);
        return contentView;
    }
}
