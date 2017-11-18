package com.engramtd.tdinh.youtubeaudioplayer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tdinh on 11/14/2017.
 */

public class GoRetriever {

    public interface OnAudioDownloadedListener{
        public void onAudioDownloaded(YoutubeVideo ytVideo);
    }

    private String videoId;
    private Context appContext;

    public GoRetriever(String videoId, Context appContext){
        this.videoId = videoId;
        this.appContext = appContext;
    }

    public void getVideo(final OnAudioDownloadedListener listener){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://engramtd.com?id=" + videoId);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    int code = httpCon.getResponseCode();
                    String message = httpCon.getResponseMessage();
//                    Log.i("engramtd_log", code + " " + message);
                    if(code != HttpURLConnection.HTTP_OK){
                        return;
                    }
                    InputStream inputStream = httpCon.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder("");
                    int data = reader.read();
                    while(data != -1) {
                        stringBuilder.append((char) data);
                        data = reader.read();
                    }
                    reader.close();
                    inputStream.close();

                    String[] videoInfo = stringBuilder.toString().split("\n");
                    final YoutubeVideo ytVideoInfo = new YoutubeVideo(videoInfo[0],
                            videoInfo[1], Long.valueOf(videoInfo[2]), videoInfo[3],
                            videoInfo[4], videoInfo[5]);

//                    Log.i("engramtd_log", ytVideoInfo.getId());
//                    Log.i("engramtd_log", ytVideoInfo.getTitle());
//                    Log.i("engramtd_log", String.valueOf(ytVideoInfo.getDuration()));
//                    Log.i("engramtd_log", ytVideoInfo.getExtension());
//                    Log.i("engramtd_log", ytVideoInfo.getVideoLink());
//                    Log.i("engramtd_log", ytVideoInfo.getThumbnailLink());

//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(),
//                                    "Downloading " + ytVideoInfo.getTitle(), Toast.LENGTH_LONG).show();
//                        }
//                    });
                    // Download the thumbnail.
                    String[] thumbLink = ytVideoInfo.getThumbnailLink().split("\\.");
                    Downloader.download(appContext,
                            ytVideoInfo.getThumbnailLink(),
                            ytVideoInfo.getId()
                                    + "." + thumbLink[thumbLink.length - 1],
                            new Downloader.OnDownloadCompletionListener() {
                                @Override
                                public void onCompleted(String savedFileName) {
                                    ytVideoInfo.setLocalImageSrc(savedFileName);
//                                    Log.i("engramtd_log", "Image file " + savedFileName + " is downloaded.");
                                }
                            });
                    // Download the audio.
                    Downloader.download(appContext,
                            ytVideoInfo.getVideoLink(),
                            ytVideoInfo.getId()
                                    + "." + ytVideoInfo.getExtension(),
                            new Downloader.OnDownloadCompletionListener() {
                                @Override
                                public void onCompleted(String savedFileName) {
                                    ytVideoInfo.setLocalAudioSrc(savedFileName);
//                                    Log.i("engramtd_log", "Audio file " + savedFileName + " is downloaded.");
                                    listener.onAudioDownloaded(ytVideoInfo);
                                }
                            });
                } catch (MalformedURLException e) {
                    Log.i("engramtd_log", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.i("engramtd_log", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
