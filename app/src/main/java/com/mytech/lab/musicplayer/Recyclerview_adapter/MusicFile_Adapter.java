package com.mytech.lab.musicplayer.Recyclerview_adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytech.lab.musicplayer.R;

import java.util.ArrayList;

public class MusicFile_Adapter extends RecyclerView.Adapter<MusicFile_Adapter.MyViewHolder> {

    ArrayList<String> musicfile_info;
    LayoutInflater inflater;
    Context context;

    public MusicFile_Adapter(ArrayList<String> music_info,Context context) {
        this.musicfile_info = music_info;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    Communicator communicator;

    public interface Communicator {
        public void clickon_musicfile(View v, String foldername, int position);
    }

    public void click_musicfile(Communicator c) {
        this.communicator = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.recycler_folderview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final String file_name = musicfile_info.get(position);
        holder.music_file_name.setText(file_name);
        holder.music_icon.setImageResource(R.drawable.music);


        holder.head_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (communicator != null) {
                    communicator.clickon_musicfile(view, file_name, position);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return musicfile_info.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView music_file_name;
        LinearLayout head_section;
        ImageView music_icon;
        public MyViewHolder(View itemView) {
            super(itemView);
            music_file_name = itemView.findViewById(R.id.foldername);
            head_section = itemView.findViewById(R.id.head_section);
            music_icon = itemView.findViewById(R.id.music_folder_icon);

        }
    }
}
