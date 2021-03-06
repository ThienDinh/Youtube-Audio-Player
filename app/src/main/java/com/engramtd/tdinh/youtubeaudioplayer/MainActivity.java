package com.engramtd.tdinh.youtubeaudioplayer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private class CustomAdapter<T> extends ArrayAdapter {
        public CustomAdapter(Context context, int resource, List list) {
            super(context, resource, list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_song, parent, false);
                ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(convertView, "scaleX",
                        0f, 1f);
                ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(convertView, "scaleY",
                        0f, 1f);
                ObjectAnimator rotationX = ObjectAnimator.ofFloat(convertView, "rotationX",
                        0, 360);
                ObjectAnimator rotationY = ObjectAnimator.ofFloat(convertView, "rotationX",
                        0, 360);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(objectAnimatorX, objectAnimatorY,
                        rotationX, rotationY);
                animatorSet.setDuration(750);
                animatorSet.start();
            }
            YoutubeVideo ytVideo = (YoutubeVideo) getItem(position);
            ImageView ivThumbnail = (ImageView) convertView.findViewById(R.id.youtube_thumbnail);
            Drawable drawable = Drawable.createFromPath(ytVideo.getLocalImageSrc());
            ivThumbnail.setImageDrawable(drawable);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.youtube_title);
            tvTitle.setText(((YoutubeVideo) getItem(position)).getTitle());
            TextView tvId = (TextView) convertView.findViewById(R.id.youtube_id);
            tvId.setText(((YoutubeVideo) getItem(position)).getId());
            TextView duration = (TextView) convertView.findViewById(R.id.youtube_duration);
            duration.setText(String.valueOf(ytVideo.getDuration()));
            ImageView imgViewDelete = (ImageView) convertView.findViewById(R.id.btn_delete);
            final View videoView = convertView;
            imgViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(videoView, "scaleX",
                            1f, 0f);
                    ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(videoView, "scaleY",
                            1f, 0f);
                    ObjectAnimator rotationX = ObjectAnimator.ofFloat(videoView, "rotationX",
                            0, 180);
                    ObjectAnimator rotationY = ObjectAnimator.ofFloat(videoView, "rotationX",
                            0, 180);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(objectAnimatorX, objectAnimatorY,
                            rotationX, rotationY);
                    animatorSet.setDuration(750);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            CustomAdapter.this.remove(
                                    CustomAdapter.this.getItem(position));
                            videoView.setScaleX(1);
                            videoView.setScaleY(1);
                            videoView.setRotationX(0);
                            videoView.setRotationY(0);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    animatorSet.start();
                }
            });
            return convertView;
        }

    }

    private YoutubePlayer mp;
    private Playlist onlyPlaylist;
    private ArrayAdapter youtubeAdapter;
    private SQLiteDatabase youtubeDB;
    private ProgressBar downloadProgress;
    private String[] songs = {
            "SunkrH3TRH4", "DGixjx5vpiM"
    };
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar == null) {
            Log.i("engramtd_log", "Toolbar is null.");
        }
        View playback = getLayoutInflater()
                .inflate(R.layout.playback, null);
        toolbar.addView(playback);

        // Inflate menu.
        Menu toolbarMenu = toolbar.getMenu();
        getMenuInflater().inflate(R.menu.toolbar_menu, toolbarMenu);
        toolbarMenu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                addYoutubeVideo(songs[index]);
                index = (index + 1) % songs.length;
                return true;
            }
        });
        toolbarMenu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                youtubeAdapter.clear();
                Log.i("engramtd_log", "Menu Item 2 clicked:" + menuItem.getTitle().toString());
                return true;
            }
        });

        Toolbar.LayoutParams lp = (Toolbar.LayoutParams) playback.getLayoutParams();
        playback.setLayoutParams(new Toolbar.LayoutParams(lp.width,
                lp.height, Gravity.RIGHT));

        // Create database.
        youtubeDB = openOrCreateDatabase("youtube_player.db", MODE_PRIVATE, null);

        // Load songs to playlist.
        Log.i("engramtd_log", "Reading the database.");
        youtubeDB.execSQL("CREATE TABLE IF NOT EXISTS youtube_videos(" +
                "video_id TEXT PRIMARY KEY," +
                "title TEXT," +
                "duration INTEGER," +
                "extension TEXT," +
                "video_link TEXT," +
                "thumbnail_link TEXT," +
                "local_img_src TEXT," +
                "local_audio_src TEXT);");
        onlyPlaylist = new Playlist("single_list");

        // Retrieve Youtube Videos.
        Cursor results = youtubeDB.rawQuery("SELECT * FROM youtube_videos;", null);
        Log.i("engramtd_log", "Open database. Number of records: " + results.getCount());
        while(results.moveToNext()){
            try {
                // Move to the next row.
                Log.i("engramtd_log", "Trying to load Youtube Video entry.");
                YoutubeVideo video = new YoutubeVideo(
                        results.getString(0), results.getString(1), results.getLong(2),
                        results.getString(3), results.getString(4), results.getString(5));
                video.setLocalImageSrc(results.getString(6));
                video.setLocalAudioSrc(results.getString(7));
                // Add the youtube into the playlist.
                onlyPlaylist.add(video);
                Log.i("engramtd_log", "Done Trying to load Youtube Video entry.");
            } catch(Exception ex){
                Log.i("engramtd_log", "Reading result: " + ex.getMessage());
            }
        }
        youtubeDB.close();
        Log.i("engramtd_log", "Done reading the database.");

        // Seekbar.
        final SeekBar sBar = (SeekBar) findViewById(R.id.seek_bar);
        sBar.setProgress(0);
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                   mp.seek(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Create YoutubePlayer.
        mp = new YoutubePlayer();
        mp.setPlaylist(onlyPlaylist);
        mp.setOnBeginPlaying(new YoutubePlayer.OnPlaybackListener() {

            @Override
            public void onReadyToPlay(final YoutubeVideo video) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView currentTitle = (TextView) findViewById(R.id.text_current_title);
                        currentTitle.setText(video.getTitle());
                        sBar.setProgress(0);
                    }
                });
            }

            @Override
            public void onStartPlaying(final int duration) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sBar.setMax(duration);
                        Log.i("engramtd_log", "MainActivity Duration:" + duration);
                    }
                });
            }

            @Override
            public void onPositionUpdated(final int currentPosition) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sBar.setProgress(currentPosition);
                    }
                });
            }


        });
        onlyPlaylist.setOnItemAdded(new Playlist.OnItemAddedListener() {
            @Override
            public void onItemAdded(Object obj) {
                YoutubeVideo yt = (YoutubeVideo) obj;
                Log.i("engramtd_log", "A new video " + yt.getTitle() + " is added into the playlist.");
                // Update the playlist.
                youtubeDB = openOrCreateDatabase("youtube_player.db", MODE_PRIVATE, null);
                try {
                    youtubeDB.execSQL(String.format("INSERT INTO youtube_videos VALUES ('%s'," +
                                    " '%s', %d, '%s', '%s', '%s', '%s', '%s');",
                            yt.getId(), yt.getTitle().replace("'","''"), yt.getDuration(), yt.getExtension(),
                            yt.getVideoLink(), yt.getThumbnailLink(), yt.getLocalImageSrc(), yt.getLocalAudioSrc()));
                }catch(Exception ex){
                    Log.i("engramtd_log", "Raw query error:" + ex.getMessage());
                }
                Cursor results = youtubeDB.rawQuery("SELECT * FROM youtube_videos;", null);
                Log.i("engramtd_log", "Inserted! New Number of records: " + results.getCount());
                youtubeDB.close();
            }
        });
        onlyPlaylist.setOnItemRemoved(new Playlist.OnItemRemovedListener() {
            @Override
            public void onItemRemoved(Object obj) {
                YoutubeVideo yt = (YoutubeVideo) obj;
                // Delete the record from the database.
                Log.i("engramtd_log", "Trying to delete a record: " + yt.getId());
                youtubeDB = openOrCreateDatabase("youtube_player.db", MODE_PRIVATE, null);
                youtubeDB.execSQL(String.format("DELETE FROM youtube_videos WHERE video_id = '%s';", yt.getId()));

                Cursor results = youtubeDB.rawQuery("SELECT * FROM youtube_videos;", null);
                Log.i("engramtd_log", "Deleted! New Number of records: " + results.getCount());
                youtubeDB.close();
            }
        });

        // Create the list view with adapter.
        youtubeAdapter = new CustomAdapter<>(this, R.layout.item_song, onlyPlaylist);
        final ListView listView = (ListView) findViewById(R.id.listview_songs);
        final ImageView btnPlay = (ImageView) findViewById(R.id.btn_play);
        listView.setAdapter(youtubeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TextView tv = (TextView) view.findViewById(R.id.youtube_id);
                if (tv == null) {
                    Log.i("engramtd_log", "TextView is null!");
                    return;
                }
                final String videoId = tv.getText().toString();
                Log.i("engramtd_log", "Video Id is " + videoId);
                mp.play(i);
                btnPlay.setImageResource(R.drawable.pause);
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(onlyPlaylist.size() == 0){
                    return;
                }
                if (mp != null) {
                    if (mp.isPlaying()) {
//                        Log.i("engramtd_log", "isPlaying() true.");
                        mp.pause();
                        btnPlay.setImageResource(R.drawable.play);
                    } else {
                        if (mp.getCurrentPlayingIndex() == -1) {
//                            Log.i("engramtd_log", "Play 0.");
                            mp.play(0);
                        } else {
//                            Log.i("engramtd_log", "Resume.");
                            mp.resume();
                        }
                        btnPlay.setImageResource(R.drawable.pause);
                    }
                }
            }
        });

        final ImageView btnNext = (ImageView) findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.play(mp.next());
            }
        });

        final ImageView btnPrevious = (ImageView) findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.play(mp.previous());
            }
        });

        final ImageView btnRepeat = (ImageView) findViewById(R.id.btn_repeat);
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mp.getRepeatMode()) {
                    case YoutubePlayer.REPEAT_NONE:
                        // Change to repeat one.
                        mp.setRepeatMode(YoutubePlayer.REPEAT_ONE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnRepeat.setImageResource(R.drawable.repeat_one);
                            }
                        });
                        break;
                    case YoutubePlayer.REPEAT_ONE:
                        // Change to repeat all.
                        mp.setRepeatMode(YoutubePlayer.REPEAT_ALL);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnRepeat.setImageResource(R.drawable.repeat_all);
                            }
                        });
                        break;
                    case YoutubePlayer.REPEAT_ALL:
                        // Change to repeat none.
                        mp.setRepeatMode(YoutubePlayer.REPEAT_NONE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnRepeat.setImageResource(R.drawable.repeat_none);
                            }
                        });
                        break;
                    default:
                }
            }
        });
    }

    private void displayToast(String msg, boolean longDuration){
        if(longDuration){
            Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void addYoutubeVideo(final String videoId) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayToast("YouTube Video with Id " + videoId + " has been requested!", false);
            }
        });
        GoRetriever gr = new GoRetriever(videoId, getApplicationContext());
        gr.getVideo(new GoRetriever.OnAudioDownloadedListener() {
            @Override
            public void onAudioDownloaded(final YoutubeVideo ytVideo) {
                // Update UI Thread.
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("engramtd_log", "Update GUI");
                        Toast.makeText(getApplicationContext(), ytVideo.getTitle()
                                + " is ready to be played.", Toast.LENGTH_SHORT).show();
                        youtubeAdapter.add(ytVideo);
                    }
                });
            }
        });
    }

    protected void onResume() {
        super.onResume();
        try {
            //Attempt to invoke virtual method 'java.lang.String android.content.ClipData.toString()' on a null object reference.
            Log.i("engramtd_log", "Youtube passed in " + getIntent().getClipData().toString());
            String[] youtubeLink = String.valueOf(getIntent().getClipData().getItemAt(0).getText())
                    .split("/");
            String id = youtubeLink[youtubeLink.length - 1];
            Log.i("engramtd_log", "Youtube id is " + id);
            // Check if it exists.
            youtubeDB = openOrCreateDatabase("youtube_player.db", MODE_PRIVATE, null);

            Cursor results = youtubeDB.rawQuery(
                    String.format("SELECT * FROM youtube_videos WHERE video_id = '%s';", id), null);
            if(results.getCount() == 0) {
                addYoutubeVideo(id);
                Log.i("engramtd_log", "New video detected!");
            } else {
                Log.i("engramtd_log", "Old video detected!");
            }
            youtubeDB.close();
        } catch (Exception ex) {
            Log.i("engramtd_log", ex.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
