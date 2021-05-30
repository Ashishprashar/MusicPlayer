package com.example.musicplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> implements SectionIndexer {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;
    final private ListItemClickListener mOnClickListener ;
    int heartFlag=0;
    private ArrayList<Integer> mSectionPositions;



    MusicAdapter(Context context, ArrayList<MusicFiles> mFiles, ListItemClickListener mOnClickListener){
        this.mContext = context;
        this.mFiles = mFiles;
        this.mOnClickListener = mOnClickListener;
    }



    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = MainActivity.musicFiles.size(); i < size; i++) {
            String section = String.valueOf(MainActivity.musicFiles.get(i).getTitle().charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int i) {
        return mSectionPositions.get(i);
    }

    @Override
    public int getSectionForPosition(int i) {
        return i;
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
        holder.artist.setText(mFiles.get(position).getArtist());
        if(mFiles.get(position).getFav()==(int)1){
            holder.heart.setImageResource(R.drawable.favorite);
        }
        holder.heart.setOnClickListener(view -> {
            if(heartFlag==0){
                heartFlag=1;
                mFiles.get(position).setFav(1);
                holder.heart.setImageResource(R.drawable.favorite);
            }
            else{
                heartFlag=0;
                mFiles.get(position).setFav(0);
                MainActivity.heart.setImageResource(R.drawable.fav_white);
                holder.heart.setImageResource(0);
//
            }
        });
        try{

            byte[] image = getAlbumArt(mFiles.get(position).getPath());
            if (image!=null){
                Glide.with(mContext).asBitmap().load(image).into(holder.songImage);
            }
            else{
                Glide.with(mContext).load(R.drawable.music).into(holder.songImage);
            }
        }catch(Exception e){

        }


    }
    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        TextView title,artist;
        CircularImageView songImage;
        LinearLayout linearLayout;
        ImageView heart;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            songImage=itemView.findViewById(R.id.songImage);
            artist =itemView.findViewById(R.id.artist);
            linearLayout=itemView.findViewById(R.id.info);
            heart=itemView.findViewById(R.id.heart);

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