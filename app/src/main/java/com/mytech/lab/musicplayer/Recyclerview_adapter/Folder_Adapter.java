package com.mytech.lab.musicplayer.Recyclerview_adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mytech.lab.musicplayer.Constants;
import com.mytech.lab.musicplayer.R;
import com.mytech.lab.musicplayer.Constants;

import java.io.File;
import java.util.ArrayList;

public class Folder_Adapter extends RecyclerView.Adapter<Folder_Adapter.MyViewHolder> {

    ArrayList<String> folderinfo;
    LayoutInflater inflater;
    Context context;


    public Folder_Adapter(ArrayList<String> folderinfo,Context context) {
        this.folderinfo = folderinfo;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }


    Communicator communicator;

    public interface Communicator {
        public void clickonfolder(View v, String foldername, int position);
    }

    public void clickfolder(Communicator c) {
        this.communicator = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.recycler_folderview, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final String folder_name = folderinfo.get(position);
        holder.folder_name.setText(folder_name);
        String pathname = "";
        for(String filename: Constants.getCurrent_directory_array())
        {
            pathname = pathname + filename + "/";
        }
        pathname = pathname + folder_name;
        File f = new File(pathname);
        if(f.isFile())
        {
            holder.music_icon.setImageResource(R.drawable.music);
        }
        if(f.isDirectory())
        {
            holder.music_icon.setImageResource(R.drawable.folder_icon);
        }

        holder.head_section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (communicator != null) {
                    communicator.clickonfolder(view, folder_name, position);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return folderinfo.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView folder_name;
        LinearLayout head_section;
        ImageView music_icon;
        public MyViewHolder(View itemView) {
            super(itemView);
            folder_name = itemView.findViewById(R.id.foldername);
            head_section = itemView.findViewById(R.id.head_section);
            music_icon = itemView.findViewById(R.id.music_folder_icon);

        }
    }
}
