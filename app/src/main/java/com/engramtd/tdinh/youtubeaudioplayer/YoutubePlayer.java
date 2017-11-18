package com.engramtd.tdinh.youtubeaudioplayer;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by tdinh on 11/14/2017.
 */

public class YoutubePlayer {

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;

    private int currentPlayingIndex;
    private Playlist currentPlayList;
    private MediaPlayer mp;
    private int repeatMode;
    private OnBeginPlayingListener beginListener;

    public interface OnBeginPlayingListener{
        public void onBeginPlaying(YoutubeVideo video);
    }

    public YoutubePlayer(){
        mp = new MediaPlayer();
        currentPlayingIndex = -1;
        repeatMode = REPEAT_ALL;
    }

    public void stop(){
        if(mp != null) {
            mp.stop();
        }
    }
    public void pause(){
        if(mp != null) {
            mp.pause();
        }
    }
    public void resume(){
        if(mp != null) {
            mp.start();
        }
    }

    public int getCurrentPlayingIndex(){
        return currentPlayingIndex;
    }
    public int getRepeatMode(){return repeatMode;}

    public int next(){
        switch (repeatMode){
            case REPEAT_NONE:
                int nextIndex = currentPlayingIndex + 1;
                if(nextIndex == currentPlayList.size()) {
                    return -1;
                } else {
                    return nextIndex;
                }
            case REPEAT_ONE:
                return currentPlayingIndex;
            case REPEAT_ALL:
                nextIndex = currentPlayingIndex + 1;
                if(nextIndex >= currentPlayList.size()) {
                    return 0;
                } else {
                    return nextIndex;
                }
            default:return -1;
        }
    }

    public int previous(){

        switch (repeatMode){
            case REPEAT_NONE:
                int nextIndex = currentPlayingIndex - 1;
                if(nextIndex < 0) {
                    return -1;
                } else {
                    return nextIndex;
                }
            case REPEAT_ONE:
                return currentPlayingIndex;
            case REPEAT_ALL:
                nextIndex = currentPlayingIndex - 1;
                if(nextIndex < 0) {
                    return currentPlayList.size() - 1;
                } else {
                    return nextIndex;
                }
            default:return -1;
        }
    }

    public void setRepeatMode(int repeatMode){
        this.repeatMode = repeatMode;
    }

    public void setPlaylist(Playlist playlist){
        mp.release();
        mp = new MediaPlayer();
        this.currentPlayList = playlist;
        currentPlayingIndex = -1;
    }

    public boolean isPlaying(){
        if(mp != null) {
            return mp.isPlaying();
        }
        return false;
    }

    public void setOnBeginPlaying(OnBeginPlayingListener listener){
        this.beginListener = listener;
    }

    /**
     * Play a specific song in the playlist.
     * @param index
     */
    public void play(int index){
        if(index == -1) return;
        if(beginListener != null){
            beginListener.onBeginPlaying((YoutubeVideo)currentPlayList.get(index));
        }
        currentPlayingIndex = index;
        final YoutubeVideo ytVideo = (YoutubeVideo) currentPlayList.get(currentPlayingIndex);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    Log.i("engramtd_log", "Inside Thread.");
                    if (mp != null) {
                        mp.reset();
                        mp.setDataSource(ytVideo.getLocalAudioSrc());
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                int nextIndex = next();
                                if(nextIndex > -1){
                                    currentPlayingIndex = nextIndex;
                                    play(currentPlayingIndex);
                                } else {
                                    // It finishes playing the playlist, so reset the media
                                    // player and not play anything thing.
                                    mp.reset();
                                    currentPlayingIndex = 0;
                                }
                            }
                        });
                        mp.prepare();
                        mp.start();
                    }
                } catch (IOException e) {
                    Log.i("engramtd_log", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
