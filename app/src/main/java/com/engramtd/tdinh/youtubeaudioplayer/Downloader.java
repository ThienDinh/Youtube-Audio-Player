package com.engramtd.tdinh.youtubeaudioplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tdinh on 11/7/2017.
 */

public class Downloader {

    public interface OnDownloadCompletionListener{
        public void onCompleted(String savedFileName);
    }

    public static void download(final Context context, final String link, final String fileName,
                                Downloader.OnDownloadCompletionListener listener) {
                try {
//                    Log.i("engramtd_log", "Trying to download " + fileName);
                    URL downloadUrl = new URL(link);
                    URLConnection urlConnection = downloadUrl.openConnection();
                    InputStream audioStream = urlConnection.getInputStream();
                    FileOutputStream fileOutputStream = context
                            .openFileOutput(fileName, MODE_PRIVATE);
                    int data = audioStream.read();
                    while (data != -1) {
                        fileOutputStream.write(data);
                        data = audioStream.read();
                    }
                    audioStream.close();
                    fileOutputStream.close();
//                    Log.i("engramtd_log", "Done downloading the file " + fileName);

                    String savedFileName = context.getFilesDir().getAbsolutePath()+ "/"+ fileName;
                    listener.onCompleted(savedFileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
    }
}
