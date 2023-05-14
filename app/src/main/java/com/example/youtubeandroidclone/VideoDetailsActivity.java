package com.example.youtubeandroidclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youtubeandroidclone.Adapter.ContentAdapter;
import com.example.youtubeandroidclone.Models.ContentModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class VideoDetailsActivity extends AppCompatActivity {

    StyledPlayerView playerView;
    ExoPlayer player;

    String video_url, video_title, video_description, date;
    int views;
    ImageView logo;
    TextView channel_name_TV, video_title_TV, video_description_TV, views_TV, date_TV;

    ArrayList<ContentModel> list;
    DatabaseReference reference;
    RecyclerView recyclerView;
    ContentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

        Intent intent = getIntent();
        video_url = intent.getStringExtra("video_url");
        String publisher = intent.getStringExtra("publisher");
        video_title = intent.getStringExtra("video_title");
        video_description = intent.getStringExtra("video_description");
        views = intent.getIntExtra("views", 0);
        date = intent.getStringExtra("date");

        playerView = findViewById(R.id.exo_player);
        logo = findViewById(R.id.user_profile_image);
        video_title_TV = findViewById(R.id.video_title);
        video_description_TV = findViewById(R.id.description);
        channel_name_TV = findViewById(R.id.channel_name);
        views_TV = findViewById(R.id.views_count);
        date_TV = findViewById(R.id.date);

        DatabaseReference channelReference = FirebaseDatabase.getInstance().getReference().child("Channels");

        channelReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(Objects.equals(publisher, dataSnapshot.getKey())) {
                            String channel_name = dataSnapshot.child("channel_name").getValue().toString();
                            String profile = dataSnapshot.child("channel_logo").getValue().toString();
                            channel_name_TV.setText(channel_name);
                            Picasso.get().load(profile).placeholder(R.drawable.account)
                                    .into(logo);
                        }
                    }
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        video_title_TV.setText(video_title);
        video_description_TV.setText(video_description);
        views_TV.setText(views + " views");
        date_TV.setText(date);

        reference = FirebaseDatabase.getInstance().getReference().child("Content");
        recyclerView = findViewById(R.id.recycler_view);

        list = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    list.clear();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        if(!Objects.equals(video_url, dataSnapshot.child("video_url").getValue().toString())) {
                            ContentModel model = dataSnapshot.getValue(ContentModel.class);
                            list.add(model);
                        }
                    }
                    Collections.shuffle(list);

                    adapter = new ContentAdapter(getApplicationContext(), list, pos -> {
                        Intent intent = getIntent();
                        finish();
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
                    Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(video_url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(player == null) {
            initializePlayer();
        }
    }

}