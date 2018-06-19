package com.example.djden.reminder;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;

public class ManagementSoundVibrate {
    private final static String LOG_TAG = "ManagementSoundVibrate";

    private Context _context;
    private Vibrator _vibrator;
    private SoundPool _mSoundPool;
    private int _soundID;
    int actualVolume;

    private static ManagementSoundVibrate _instance;

    private ManagementSoundVibrate(Context context) {
        _context = context;
    }

    public static ManagementSoundVibrate getInstance(Context c) {
        if (_instance == null) {
            _instance = new ManagementSoundVibrate(c);
        }
        return _instance;
    }

    private void _vibrate() {
        if (_vibrator != null) return;
        //Log.w(LOG_TAG, "_vibrate");

        _vibrator = (Vibrator) _context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] mVibratePattern = new long[]{
                0, 2000,
                500, 500,
                500, 500,
                500, 500,
                1000, 300,
                1000, 300,
                1000, 300
        };

        // 3 : Repeat this pattern from 3rd element of an array
        _vibrator.vibrate(mVibratePattern, -1);
    }

    private void _playSound() {
        if (_mSoundPool != null) return;
        //Log.w(LOG_TAG, "_playSound");

        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        _mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(1).build();
        _mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                // Getting the user sound settings
                AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
                actualVolume = audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                //float volume = actualVolume / maxVolume;
                float volume = maxVolume;

                //Выставляем громкость на максимум
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                // Is the sound loaded already?
                Log.w(LOG_TAG, String.format(
                        "maxVolume: %s, actualVolume: %s",
                        maxVolume,
                        actualVolume
                ));
                soundPool.play(_soundID, volume, volume, 1, 4, 1f);
            }
        });
        //Загружаем звуки в память
        _soundID = _mSoundPool.load(_context, R.raw.s_reminder_1, 1);
    }


    public void start(boolean isSound) {
        _vibrate();
        if (isSound) {
            _playSound();
        }
    }

    public void stop() {
        if (_vibrator != null) {
            _vibrator.cancel();
            _vibrator = null;
        }
        if (_mSoundPool != null) {
            _mSoundPool.release();
            _mSoundPool = null;
            //Выставляем громкость как была
            AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, actualVolume, 0);
        }
    }

}
