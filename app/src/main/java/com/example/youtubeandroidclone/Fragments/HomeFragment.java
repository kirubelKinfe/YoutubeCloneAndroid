package com.example.youtubeandroidclone.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeandroidclone.Adapter.ContentAdapter;
import com.example.youtubeandroidclone.Models.ContentModel;
import com.example.youtubeandroidclone.R;
import com.example.youtubeandroidclone.VideoDetailsActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeFragment extends Fragment {

    ArrayList<ContentModel> list;
    DatabaseReference reference;
    RecyclerView recyclerView;
    ContentAdapter adapter;


    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        reference = FirebaseDatabase.getInstance().getReference().child("Content");
        recyclerView = view.findViewById(R.id.recyclerView);

        list = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    list.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        ContentModel model = dataSnapshot.getValue(ContentModel.class);
                        list.add(model);
                    }
                    Collections.shuffle(list);

                    adapter = new ContentAdapter(getContext(), list, pos -> {
                        Intent intent = new Intent(getContext(), VideoDetailsActivity.class);
                        intent.putExtra("video_url", list.get(pos).getVideo_url());
                        intent.putExtra("video_title", list.get(pos).getVideo_title());
                        intent.putExtra("video_description", list.get(pos).getVideo_description());
                        intent.putExtra("views", list.get(pos).getViews());
                        intent.putExtra("date", list.get(pos).getDate());
                        intent.putExtra("publisher", list.get(pos).getPublisher());
                        startActivity(intent);
                    });

                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}