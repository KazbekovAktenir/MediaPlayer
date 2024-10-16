package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //переменные для интерфейса
    private TextView songTitle;
    private TextView currentTime, totalTime;
    private ImageButton playPauseButton, nextButton, prevButton;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    //массив песен
    private int[] songs;
    private int currentSongIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //инициализация элементов интерфейса
        songTitle = findViewById(R.id.songTitle);
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime); //текущее время
        totalTime = findViewById(R.id.totalTime);     //общее время

        //загрузка песен из ресурсов
        loadSongsFromResources();

        //установка медиаплеера для первого трека
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex]);

        //установка названия трека
        songTitle.setText("Track " + (currentSongIndex + 1));

        //истановка общего времени трека
        totalTime.setText(convertTime(mediaPlayer.getDuration()));

        //инициализация прогресс-бара
        seekBar.setMax(mediaPlayer.getDuration());

        //обновление прогресс-бара и времени каждую секунду
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTime.setText(convertTime(mediaPlayer.getCurrentPosition())); //обновление текущего времени
                handler.postDelayed(this, 1000);
            }
        };

        //кнопка Play/Pause
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    handler.postDelayed(updateSeekBar, 0); // Начать обновление seekBar
                }
            }
        });

        //кнопка Next
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTrack(1);
            }
        });

        //кнопка Previous
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTrack(-1);
            }
        });

        //перемотка по seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(convertTime(mediaPlayer.getCurrentPosition())); //обновление текущего времени при перемотке
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //автоматический переход на следующий трек после завершения текущего
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                changeTrack(1); // Переход на следующий трек
            }
        });
    }

    //метод для загрузки песен из ресурсов
    private void loadSongsFromResources() {
        //получение массива имен песен из ресурсов
        String[] songNames = getResources().getStringArray(R.array.song_names);
        songs = new int[songNames.length];

        for (int i = 0; i < songNames.length; i++) {
            //получение идентификатора ресурса по имени файла
            int resId = getResources().getIdentifier(songNames[i], "raw", getPackageName());
            songs[i] = resId;
        }
    }
    //метод для смены трека
    private void changeTrack(int direction) {
        mediaPlayer.stop();
        mediaPlayer.release();

        //изменение индекса песни
        currentSongIndex = (currentSongIndex + direction + songs.length) % songs.length;

        //установка нового трека
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex]);

        //получаем название песни из массива имен песен и устанавливаем его как заголовок
        String[] songNames = getResources().getStringArray(R.array.song_names);
        songTitle.setText(songNames[currentSongIndex]); //устанавливаем название трека
        seekBar.setMax(mediaPlayer.getDuration());
        totalTime.setText(convertTime(mediaPlayer.getDuration())); //установка общего времени для нового трека

        //автоматически запускать новый трек
        mediaPlayer.start();
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);

        handler.postDelayed(updateSeekBar, 0); //начать обновление seekBar

        //установка слушателя окончания трека для нового трека
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                changeTrack(1); //автоматический переход на следующий трек
            }
        });
    }

    //метод для конвертации миллисекунд в минуты и секунды
    private String convertTime(int ms) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
        }
    }
}
