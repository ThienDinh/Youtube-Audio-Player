package com.engramtd.tdinh.youtubeaudioplayer;

import java.io.Serializable;
import java.time.Duration;

/**
 * Created by tdinh on 11/6/2017.
 */

public class YoutubeVideo{
    private String id;
    private String title;
    private long duration;
    private String extension;
    private String videoLink;
    private String thumbnailLink;
    private String localImageSrc;
    private String localAudioSrc;

    public YoutubeVideo(String id, String title, long duration,
                        String extension, String videoLink, String thumbnailLink) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.extension = extension;
        this.videoLink = videoLink;
        this.thumbnailLink = thumbnailLink;
    }

    public String getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public long getDuration() {return duration;}

    public String getExtension(){
        return extension;
    }

    public String getVideoLink(){
        return videoLink;
    }

    public String getThumbnailLink(){
        return thumbnailLink;
    }

    public void setLocalImageSrc(String filePath){
        this.localImageSrc = filePath;
    }

    public String getLocalImageSrc(){
        return this.localImageSrc;
    }

    public void setLocalAudioSrc(String filePath){
        this.localAudioSrc = filePath;
    }

    public String getLocalAudioSrc(){
        return this.localAudioSrc;
    }

    public String toString(){
        return id;
    }
}
