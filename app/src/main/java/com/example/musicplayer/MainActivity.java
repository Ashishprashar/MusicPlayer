package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MusicAdapter.ListItemClickListener{
    static ArrayList<MusicFiles> musicFiles;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    TextView title,duration,progress,songName,artist;
    ImageView prev, playPause, next;
    SeekBar seekBar;
    int position;
    Uri uri;
    Handler handler = new Handler(      );
    MusicAdapter musicAdapter;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        recyclerView = findViewById(R.id.recyclerView);
        try{
            setView();
            runtimepermission();
            display();

            onClick();
        }catch (Exception e){
            Log.e("tag",e+"");
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    int curr = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(curr);
                    progress.setText(millisecondsToString(curr));

                }
                handler.postDelayed(this,1000);
            }
        });



    }

    private void onClick() {
        playPause.setOnClickListener(view -> {
            if(!musicFiles.isEmpty()){

            Log.e("tag", "clicked");
            if (view.getId() == R.id.playPause && mediaPlayer!=null) {


                if (mediaPlayer.isPlaying()) {
                    // is playing
                    mediaPlayer.pause();
                    playPause.setImageResource(R.drawable.play);
                } else {
                    // on pause
                    mediaPlayer.start();
                    playPause.setImageResource(R.drawable.pause);
                }

            }
            }
        });
        next.setOnClickListener(view -> {
            if(mediaPlayer!= null){
                if(position==musicFiles.size()-1){
                    position=0;
                }else{

                position+=1;
                }

                uri = Uri.parse(musicFiles.get(position).getPath());
                play();
            }
        });
        prev.setOnClickListener(view -> {
            if(mediaPlayer!= null){
                if(position==0){
                    position=musicFiles.size()-1;
                }
                else{
                    position-=1;
                }

                uri = Uri.parse(musicFiles.get(position).getPath());
                play();
            }
        });

    }


    private void setView() {
        prev =findViewById(R.id.prev);
        playPause =findViewById(R.id.playPause);
        next =findViewById(R.id.next);
        seekBar =findViewById(R.id.seekBar);
        prev =findViewById(R.id.prev);
        title = findViewById(R.id.title1);
        title.setSelected(true);
        songName = findViewById(R.id.title);
        linearLayout = findViewById(R.id.linear);
        artist = findViewById(R.id.artist);
        duration=findViewById(R.id.duration);
        progress = findViewById(R.id.position);
    }

    @SuppressLint("SetTextI18n")
    private void display() {

        recyclerView.setHasFixedSize(true);
        try{

        if ((musicFiles!=null)){
            musicAdapter =new MusicAdapter(this,musicFiles,this);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false));

            title.setText("No slected file");
            Log.e("error check","in display");
        }
        }
        catch (Exception e){
            title.setText("No .mp3 Files found ;)");
        }

    }

    public void runtimepermission(){

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        musicFiles = getAllMusic(MainActivity.this);

                        display();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permissionRequest, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();

    }

    public static ArrayList<MusicFiles> getAllMusic(Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String path = cursor.getString(3);
                String duration = cursor.getString(4);
                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, Integer.parseInt(duration));
                Log.e("path:-" + path, "album" + album);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;
    }

    @Override
    public void onListItemClick(int position) {
        this.position=position;

        setSong(position);
    }

    private void setSong(int position) {


//        releaseMediaPlayer();

        if(musicFiles!=null){

            playPause.setImageResource(R.drawable.pause);
            uri = Uri.parse(musicFiles.get(position).getPath());

        }

        play();

        }

    private void play() {
        title.setText(musicFiles.get(position).getTitle());
        playPause.setImageResource(R.drawable.pause);
        duration.setText(musicFiles.get(position).getDuration());
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        seekBar.setMax(musicFiles.get(position).getduration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(  b){
                    mediaPlayer.seekTo(i);
                    seekBar.setProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public String millisecondsToString(int time) {
        String elapsedTime;
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        elapsedTime = minutes+":";
        if(seconds < 10) {
            elapsedTime += "0";
        }
        elapsedTime += seconds;

        return  elapsedTime;
    }
}