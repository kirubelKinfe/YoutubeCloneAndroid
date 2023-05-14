package com.example.youtubeandroidclone.Dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.youtubeandroidclone.R;

public class PlaylistDashboard extends Fragment {

    public PlaylistDashboard() {

    }

    public static PlaylistDashboard newInstance() {
        PlaylistDashboard fragment = new PlaylistDashboard();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_playlist, container, false);
    }

}