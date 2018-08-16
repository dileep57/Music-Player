package com.mytech.lab.musicplayer.Recyclerview_adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytech.lab.musicplayer.Activity.Home;
import com.mytech.lab.musicplayer.R;
import com.mytech.lab.musicplayer.utils.Song_base;
import com.mytech.lab.musicplayer.Activity.Home;
import com.mytech.lab.musicplayer.utils.Song_base;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

/**
 * Created by lnx on 27/2/18.
 */

public class Artist_adapter extends RecyclerView.Adapter<Artist_adapter.MyViewHolder> implements FastScrollRecyclerView.SectionedAdapter{

    ArrayList<Song_base> song_info;
    Context context;
    LayoutInflater inflater;

    public Artist_adapter(ArrayList<Song_base> song_info, Context context) {
        this.song_info = song_info;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    Communicator communicator;

    @NonNull
    @Override
    public String getSectionName(int position) {
        return song_info.get(position).getSong_name().substring(0,1);
    }

    public interface Communicator {
        public void clickonplaybutton(View v, Song_base s, int position);
    }

    public void setCommnicator(Communicator c) {
        this.communicator = c;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.recycler_artist_layout,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Song_base s = song_info.get(position);
        holder.artist.setText(s.getArtist());
        holder.track.setText("Track "+Integer.toString(Home.Companion.getArtistmap().get(s.getArtist()).size()));
        holder.album_cunt.setText("Album "+Integer.toString(Home.Companion.getArtist_to_album().get(s.getArtist())));


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (communicator != null) {

                    communicator.clickonplaybutton(view, s, position);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return song_info.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView artist,track,album_cunt;
        RelativeLayout relativeLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            artist = itemView.findViewById(R.id.artist_name);
            track = itemView.findViewById(R.id.track_count);
            album_cunt = itemView.findViewById(R.id.album_count);
            relativeLayout = itemView.findViewById(R.id.cardrelative);
        }
    }
}
