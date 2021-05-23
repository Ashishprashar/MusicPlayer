package com.example.musicplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;
    final private ListItemClickListener mOnClickListener ;




    MusicAdapter(Context context, ArrayList<MusicFiles> mFiles, ListItemClickListener mOnClickListener){
        this.mContext = context;
        this.mFiles = mFiles;
        this.mOnClickListener = mOnClickListener;
    }



    interface ListItemClickListener{
        void onListItemClick(int position);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_file,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.title.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image!=null){
            Glide.with(mContext).asBitmap().load(image).into(holder.songImage);
        }
        else{
            Glide.with(mContext).load(R.drawable.play).into(holder.songImage);
        }


    }
    private void setView() {

    }
    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        TextView title,artist;
        CircularImageView songImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            songImage=itemView.findViewById(R.id.songImage);
            artist =itemView.findViewById(R.id.artist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position =getAdapterPosition();
            mOnClickListener.onListItemClick(position);
            Log.d("TAG", "Clicked!");
        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever =new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art =retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}