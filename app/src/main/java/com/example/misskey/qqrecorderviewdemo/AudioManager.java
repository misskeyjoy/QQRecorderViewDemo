package com.example.misskey.qqrecorderviewdemo;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by misskey on 15-10-28.
 */
public class AudioManager {
    private MediaRecorder mMediaRecorder;
    private String mDir;
    private String mCurrentFilePath;
    private boolean isPrepared;

    private static AudioManager mInstance;

    public static AudioManager getIntance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                mInstance = new AudioManager(dir);
            }
        }
        return mInstance;

    }

    public AudioStateListener mListener;

    public AudioManager(String dir) {
        this.mDir = dir;
    }

    public interface AudioStateListener {
        void wellPrepared();
    }

    public void setOnAudioStateListener(AudioStateListener listener) {
        mListener=listener;

    }
    public void prepareAudio(){
        try{
            isPrepared=false;
            File dir=new File(mDir);
            if(!dir.exists()){
                dir.mkdir();
            }
            String filename=generateFileName();
            File file=new File(dir,filename);
            mCurrentFilePath=file.getAbsolutePath();
            mMediaRecorder=new MediaRecorder();
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            isPrepared=true;
            if(mListener!=null){
                mListener.wellPrepared();
            }
            mMediaRecorder.prepare();
            mMediaRecorder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 随机生成文件的名称
     * @return
     */
    private String generateFileName(){
        return UUID.randomUUID().toString()+".amr";
    }


    /**
     * 获得音量的等级
     * @param maxLevel
     * @return
     */
    public int getVoiceLevel(int maxLevel){
        if(isPrepared){
            try {
                return maxLevel*mMediaRecorder.getMaxAmplitude()/32768+1;
            }
            catch (Exception e){

            }
        }
        return 1;
    }

    /**
     * 释放资源
     */
    public void releas(){
        mMediaRecorder.release();
        mMediaRecorder=null;
        isPrepared=false;
    }
    public String getmCurrentFilePath(){
        return mCurrentFilePath;
    }
    public void cancle(){
        releas();
        if(mCurrentFilePath!=null){
            File file=new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath=null;
        }
    }
}
