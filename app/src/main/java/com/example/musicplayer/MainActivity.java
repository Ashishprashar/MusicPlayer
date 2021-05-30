package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MusicAdapter.ListItemClickListener {
    public static ArrayList<MusicFiles> musicFiles,favMusicList;
    in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView recyclerView;
        RecyclerView favRecycler;
//    RelativeLayout relativeLayout;
    TextView title,duration,progress,songName,artist;
    ImageView prev, playPause, next,shuffle,favList;

//    RelativeLayout l1;
    SeekBar seekBar;
    int position,flag=0;
    public static ImageView heart;
    AlertDialog.Builder dailogBuilder;
    Dialog dialog;
    Uri uri;
    Handler handler = new Handler(      );
    MusicAdapter musicAdapter,musicAdapter1;
    MediaPlayer mediaPlayer;
    static  final String POSITION = "position";

//    ImageView ivView =recyclerView.findViewById(R.id.heart);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setIndexBarTransparentValue((float)0);
        recyclerView.setIndexBarStrokeVisibility(false);
        //        try{
            setView();
            runtimepermission();
             loadData();
//        musicFiles = getAllMusic(MainActivity.this);
//        display();
            display();

            onClick();
//        }catch (Exception e){
//            Log.e("tag",e+"error");
//        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    int curr = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(curr);
                    progress.setText(millisecondsToString(curr));
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            nextSong();
                        }


                    });
                }
                handler.postDelayed(this,1000);
            }
        });


    }


    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared Preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson =new Gson();
        String json = gson.toJson(musicFiles);
        editor.putString("saved",json);
        editor.apply();
        Log.e("save","happed"+json);
    }
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared Preferences",MODE_PRIVATE);
        Gson gson =new Gson();
        String json =sharedPreferences.getString("saved",null);
        Type type =new TypeToken<ArrayList<MusicFiles>>() {}.getType();
        musicFiles =gson.fromJson(json,type);
        if(musicFiles==null){
            musicFiles=getAllMusic(this);
            display();
        }
//        Log.e("load","happed"+musicFiles.get(0).getFav());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
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
            nextSong();
        });
        prev.setOnClickListener(view -> {
            if(mediaPlayer!= null){
                if(flag==1){
                    double rand=Math.random()*10;
                    Log.e("shuffle",rand+" "+musicFiles.size());
                    position = (int)rand%(musicFiles.size()-1);
                }else{
                    if(position==0){
                        position=musicFiles.size()-1;
                    }
                    else{
                        position-=1;
                    }
                }


                uri = Uri.parse(musicFiles.get(position).getPath());
                play();
            }
        });
        shuffle.setOnClickListener(view -> {
            if(flag==0){

                flag=1;
                shuffle.setImageResource(R.drawable.shuffle_on);
            }
            else{
                flag=0;
                shuffle.setImageResource(R.drawable.shuffle);
            }
        });
        heart.setOnClickListener(view -> {
            if(musicFiles.get(position).getFav()!=1){
                musicFiles.get(position).setFav(1);
                heart.setImageResource(R.drawable.favorite);
            }
            else{
                musicFiles.get(position).setFav(0);
                heart.setImageResource(R.drawable.fav_white);
            }
            musicAdapter.notifyItemChanged(position);
            saveData();


        });

    }
     void nextSong() {
         if(mediaPlayer!= null){
             if(flag==1){
                 int rand = (int) (Math.random()*(musicFiles.size()-1));
                 Log.e("shuffle",rand+" "+musicFiles.size());
                 position = (int)rand;
             }
             else {
                 if(position==musicFiles.size()-1){

                     position=0;
                 }else{
                     position+=1;
                 }
             }

             uri = Uri.parse(musicFiles.get(position).getPath());
             play();
         }
    }

    @SuppressLint("WrongViewCast")
    private void setView() {
        prev =findViewById(R.id.prev);
        playPause =findViewById(R.id.playPause);
        next =findViewById(R.id.next);
        seekBar =findViewById(R.id.seekBar);
        prev =findViewById(R.id.prev);
        title = findViewById(R.id.title1);
        title.setSelected(true);
        songName = findViewById(R.id.title);
//        relativeLayout = findViewById(R.id.info);
        artist = findViewById(R.id.artist);
        duration=findViewById(R.id.duration);
        progress = findViewById(R.id.position);
        shuffle = findViewById(R.id.shuffle);
//        favList = findViewById(R.id.favlist);
//        l1 = findViewById(R.id.l1);
        heart = findViewById(R.id.heart1);
    }

    @SuppressLint("SetTextI18n")
    private void display() {

        recyclerView.setHasFixedSize(true);
        try{

        if ((musicFiles!=null)){
            musicAdapter =new MusicAdapter(this,musicFiles,MainActivity.this);
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

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
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

    public ArrayList<MusicFiles> getAllMusic(Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
         String sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+"";


        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String path = cursor.getString(3);
                long duration = cursor.getLong(4);
                MusicFiles musicFiles;
//                if(duration=="null"){
                  Log.e("lag",duration+"");
//                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                retriever.setDataSource(this,Uri.parse(MediaStore.Audio.Media.DATA));
//                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                int millSecond = Integer.parseInt(duration);
                    musicFiles = new MusicFiles(path, title, artist, album, (int) duration);
//                }else{
//                    musicFiles = new MusicFiles(path, title, artist, album,0);
//                }
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

        Log.e("cj",musicFiles.get((int)position).getFav()+"");
        setSong(position);
        play();
    }

    private void setSong(int position) {


//        releaseMediaPlayer();

        if(musicFiles!=null){

            playPause.setImageResource(R.drawable.pause);
            uri = Uri.parse(musicFiles.get(position).getPath());

        }



        }

    private void play() {
        title.setText(musicFiles.get(position).getTitle());
        playPause.setImageResource(R.drawable.pause);
        duration.setText(musicFiles.get(position).getDuration());
        if(musicFiles.get(position).getFav()!=1){
            heart.setImageResource(R.drawable.fav_white);
//            ivView.setImageResource(0);
        }
        else{
            heart.setImageResource(R.drawable.favorite);
//            ivView.setImageResource(R.drawable.favorite);

        }
//        View view = LayoutManger.fi

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