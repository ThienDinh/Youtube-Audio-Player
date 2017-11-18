package com.engramtd.tdinh.youtubeaudioplayer;

import java.util.ArrayList;

/**
 * Created by tdinh on 11/14/2017.
 */

public class Playlist<YoutubeVideo> extends ArrayList{

    public interface OnItemAddedListener{
        public void onItemAdded(Object obj);
    }

    public interface OnItemRemovedListener{
        public void onItemRemoved(Object obj);
    }

    private OnItemAddedListener addListener;
    private OnItemRemovedListener removalListener;
    private String title;

    public Playlist(String title){
        super();
        this.title = title;
    }

    @Override
    public boolean add(Object obj){
        boolean result = super.add(obj);
        if(addListener != null) {
            addListener.onItemAdded(obj);
        }
        return result;
    }

    @Override
    public boolean remove(Object obj){
        boolean result = super.remove(obj);
        if(removalListener != null) {
            removalListener.onItemRemoved(obj);
        }
        return result;
    }

    public void setOnItemAdded(OnItemAddedListener listener){
        this.addListener = listener;
    }

    public void setOnItemRemoved(OnItemRemovedListener removalListener){
        this.removalListener = removalListener;
    }

    public String getTitle(){
        return title;
    }
}
