package com.example.youtubeandroidclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtubeandroidclone.Models.PlaylistModel;
import com.example.youtubeandroidclone.R;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {
    Context context;
    ArrayList<PlaylistModel> list;
    OnItemClickListener listener;

    public PlaylistAdapter(Context context, ArrayList<PlaylistModel> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistAdapter.PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistHolder(LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.PlaylistHolder holder, int position) {
        holder.text_playlist_name.setText(list.get(position).getPlaylist_name());
        holder.text_videos_count.setText("Videos " + list.get(position).getVideos());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PlaylistHolder extends RecyclerView.ViewHolder {
        TextView text_playlist_name, text_videos_count;

        public PlaylistHolder(@NonNull View itemView) {
            super(itemView);

            text_playlist_name = itemView.findViewById(R.id.playlist_name);
            text_videos_count = itemView.findViewById(R.id.videos_count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
}
