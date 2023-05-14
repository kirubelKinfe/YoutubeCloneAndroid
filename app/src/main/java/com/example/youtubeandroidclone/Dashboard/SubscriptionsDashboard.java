package com.example.youtubeandroidclone.Dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.youtubeandroidclone.R;

public class SubscriptionsDashboard extends Fragment {

    public SubscriptionsDashboard() {

    }

    public static SubscriptionsDashboard newInstance() {
        SubscriptionsDashboard fragment = new SubscriptionsDashboard();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_subscriptions, container, false);
    }

}