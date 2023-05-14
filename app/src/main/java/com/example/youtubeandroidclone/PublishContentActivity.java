package com.example.youtubeandroidclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.youtubeandroidclone.Adapter.PlaylistAdapter;
import com.example.youtubeandroidclone.Models.PlaylistModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PublishContentActivity extends AppCompatActivity {

    VideoView videoView;
    MediaController mediaController;
    String type;
    Uri videoUri;

    ChipGroup chipGroup;
    EditText input_tag, input_video_title, input_video_description;
    LinearLayout progressLayout;
    ProgressBar progress_bar;
    TextView upload, text_choose_playlist, progress_text;
    Dialog dialog;

    ArrayList<PlaylistModel> list;
    PlaylistAdapter adapter;
    RecyclerView recyclerView;


    FirebaseUser user;
    DatabaseReference reference;
    StorageReference storageReference;
    UploadTask uploadTask;

    String selectedPlaylist;
    int videosCount;

    String tags = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_content);


        text_choose_playlist = findViewById(R.id.choose_playlist);
        input_video_title = findViewById(R.id.input_video_title);
        input_video_description = findViewById(R.id.input_video_description);
        progressLayout = findViewById(R.id.progressLayout);
        progress_text = findViewById(R.id.progress_text);
        progress_bar = findViewById(R.id.progress_bar);

        videoView = findViewById(R.id.videoView);
        chipGroup = findViewById(R.id.chipGroup);
        input_tag = findViewById(R.id.input_video_tag);
        upload = findViewById(R.id.upload_video);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Content");
        storageReference = FirebaseStorage.getInstance().getReference().child("Content");



        input_tag.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String input_tags = input_tag.getText().toString().trim();
                setChips(input_tags);
                input_tag.setText("");
                return true;
            }

            return false;
        });

        upload.setOnClickListener(v -> {
            String title = input_video_title.getText().toString().trim();
            String description = input_video_description.getText().toString().trim();

            for(int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                tags += chip.getText().toString() + " ";
            }

            if(videoUri == null && title.isEmpty()) {
                Toast.makeText(PublishContentActivity.this, "Fill all fields...", Toast.LENGTH_SHORT).show();
            } else if(text_choose_playlist.getText().toString().equals("Choose Playlist")) {
                Toast.makeText(PublishContentActivity.this, "Please select playlist", Toast.LENGTH_SHORT).show();
            } else {
                uploadVideoToStorage(title, description);
            }
        });

        text_choose_playlist.setOnClickListener(v -> showPlaylistDialog());

        mediaController = new MediaController(PublishContentActivity.this);

        Intent intent = getIntent();

        if(intent != null) {
            videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(mediaController);
            videoView.start();
        }
    }

    private void uploadVideoToStorage(String title, String description) {
        progressLayout.setVisibility(View.VISIBLE);
        final StorageReference sReference = storageReference.child(user.getUid()).child(System.currentTimeMillis()+"."+getFileExtension(videoUri));
        sReference.putFile(videoUri).addOnSuccessListener(taskSnapshot ->
                sReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String videoUrl = uri.toString();
            saveDataToFirebase(title,description,videoUrl);
        })).addOnProgressListener(snapshot -> {
            double progress = 100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
            progress_bar.setProgress((int) progress);
            progress_text.setText("Uploading " + (int)progress+"%");
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
        });

//        uploadTask = sReference.putFile(videoUri);
//
//        Task<Uri> urltask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if(!task.isSuccessful()) {
//                    throw task.getException();
//                }
//                return sReference.getDownloadUrl();
//            }
//        }).addOnCompleteListener(task -> {
//            if(task.isSuccessful()) {
//                Uri downloadUrl = task.getResult();
//                String videoUrl = downloadUrl.toString();
//                saveDataToFirebase(title,description,videoUrl);
//            }
//        });
    }

    private void saveDataToFirebase(String title, String description, String videoUrl) {
        String currentDate = DateFormat.getDateInstance().format(new Date());
        String id = reference.push().getKey();
        Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("video_title", title);
        map.put("video_description", description);
        map.put("video_tag", tags);
        map.put("playlist", selectedPlaylist);
        map.put("video_url", videoUrl);
        map.put("publisher", user.getUid());
        map.put("type", "video");
        map.put("views",0);
        map.put("date", currentDate);

        assert id != null;
        reference.child(id).setValue(map).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                progressLayout.setVisibility(View.GONE);
                Toast.makeText(PublishContentActivity.this, "Video Uploaded", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PublishContentActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                updateVideoCount();
            }
        });
    }

    private void updateVideoCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        int update = videosCount + 1;
        HashMap<String, Object> map = new HashMap<>();
        map.put("videos", update);

        assert user != null;
        reference.child(user.getUid()).child(selectedPlaylist).updateChildren(map);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void setChips(String input_tag) {
        final Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.single_chip, null, false);
        chip.setText(input_tag);

        chip.setOnClickListener(v -> chipGroup.removeView(chip));
        chipGroup.addView(chip);
    }

    private void showPlaylistDialog() {
        dialog = new Dialog(PublishContentActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.playlist_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText input_playlist_name = dialog.findViewById(R.id.input_playlist_name);
        TextView text_add = dialog.findViewById(R.id.text_add);
        recyclerView = dialog.findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        recyclerView.setHasFixedSize(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(user.getUid().equals(dataSnapshot.child("uid").getValue().toString())) {
                            PlaylistModel model = new PlaylistModel();
                            model.setPlaylist_name(dataSnapshot.child("playlist").getValue().toString());
                            model.setVideos(Integer.parseInt(dataSnapshot.child("videos").getValue().toString()));
                            model.setUid(dataSnapshot.child("uid").getValue().toString());
                            list.add(model);
                        }
                    }
                    adapter = new PlaylistAdapter(PublishContentActivity.this, list, pos -> {
                        dialog.dismiss();
                        selectedPlaylist = list.get(pos).getPlaylist_name();
                        videosCount = list.get(pos).getVideos();
                        text_choose_playlist.setText("Playlist: " + list.get(pos).getPlaylist_name());
                    });

                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PublishContentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        checkUserAlreadyHavePlaylist(recyclerView);

        text_add.setOnClickListener(v -> {
            String value = input_playlist_name.getText().toString().trim();
            if(value.isEmpty()) {
                Toast.makeText(PublishContentActivity.this, "Enter Playlist Name", Toast.LENGTH_SHORT).show();
            } else {
                createNewPlaylist(value);
            }
        });

        dialog.show();
    }



    private void createNewPlaylist(String value) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Playlists");

        HashMap<String, Object> playlist = new HashMap<>();
        playlist.put("playlist", value);
        playlist.put("videos", 0);
        assert user != null;
        playlist.put("uid", user.getUid());

        reference.child(user.getUid()).child(value).setValue(playlist).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(PublishContentActivity.this, "New Playlist Created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PublishContentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkUserAlreadyHavePlaylist(RecyclerView recyclerView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        assert user != null;
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PublishContentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}