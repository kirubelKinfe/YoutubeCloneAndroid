package com.example.youtubeandroidclone.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeandroidclone.Models.ContentModel;
import com.example.youtubeandroidclone.Models.PlaylistModel;
import com.example.youtubeandroidclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentHolder> {
    Context context;
    ArrayList<ContentModel> list;
    ContentAdapter.OnItemClickListener listener;

    public ContentAdapter(Context context, ArrayList<ContentModel> list, ContentAdapter.OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContentAdapter.ContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContentHolder(LayoutInflater.from(context).inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContentAdapter.ContentHolder holder, int position) {
        ContentModel contentModel = list.get(position);
        holder.video_title.setText(contentModel.getVideo_title());
        holder.view_count.setText(contentModel.getViews() + " views");
        holder.date.setText(contentModel.getDate());

        try {
            Bitmap bitmap = retriveVideoFrameFromVideo(contentModel.getVideo_url());
            if (bitmap != null) {
                holder.thumbnail.setImageBitmap(bitmap);
            }
        } catch (Throwable throwable) {
               throwable.printStackTrace();
        }

        DatabaseReference channelReference = FirebaseDatabase.getInstance().getReference().child("Channels");

        channelReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(Objects.equals(contentModel.getPublisher(), dataSnapshot.getKey())) {
                            String channel_name = dataSnapshot.child("channel_name").getValue().toString();
                            String profile = dataSnapshot.child("channel_logo").getValue().toString();
                            holder.channel_name.setText(channel_name);
                            Picasso.get().load(profile).placeholder(R.drawable.account)
                                    .into(holder.user_profile_image);
                        }
                    }
                }
            }
            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameVideo(String videoPath)" + e.getMessage());
        } finally {
            if(mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ContentHolder extends RecyclerView.ViewHolder {
        TextView video_title, view_count, date, channel_name;
        ImageView thumbnail, user_profile_image;


        public ContentHolder(@NonNull View itemView) {
            super(itemView);

            video_title = itemView.findViewById(R.id.video_title);
            view_count = itemView.findViewById(R.id.views_count);
            date = itemView.findViewById(R.id.date);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            user_profile_image = itemView.findViewById(R.id.user_profile_image);
            channel_name = itemView.findViewById(R.id.channel_name);

            thumbnail.setOnClickListener(v -> {
                if(listener != null) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
}
