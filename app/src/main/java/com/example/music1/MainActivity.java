package com.example.music1;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button last,go,stop,next,rand,order;
    Handler handler;
    ListView listView;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;

    private final MusicService musicService = new MusicService();
    int UPDATE = 0x101;
    int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();

        last = findViewById(R.id.last);
        go = findViewById(R.id.go);
        stop = findViewById(R.id.stop);
        next = findViewById(R.id.next);
        rand = findViewById(R.id.rand);
        order = findViewById(R.id.order);

        listView = findViewById(R.id.musicList);

        listView.getAdapter();

        try {
            setListViewAdapter();//添加文件名字
        } catch (Exception e) {
            Log.i("TAG", "读取信息失败");

        }





        go.setOnClickListener(view -> {                      //开始播放  暂停播放   继续播放
            try {

                /*
                 引入flag作为标志，当flag为1 的时候，此时player内没有东西，所以执行musicService.play()函数
                 进行第一次播放，然后flag自增二不再进行第一次播放
                 当再次点击“开始/暂停”按钮次数即大于1 将执行暂停或继续播放goplay()函数
                 */
                //开始为空，
                if (flag == 1) {
                    musicService.play();
                    flag++;
                } else {
                    if (!musicService.player.isPlaying()) {      //如果播放器不是正在播放则执行继续播放
                        musicService.goPlay();
                    } else if (musicService.player.isPlaying()) {  //否则执行暂停播放
                        musicService.pause();
                    }
                }
            } catch (Exception e) {
                Log.i("LAT", "播放异常！");
            }
        });

        stop.setOnClickListener(view -> {                       //结束播放
            try {
                musicService.stop();
                flag = 1;//当点击停止按钮时，flag置为1
                seekBar.setProgress(0);
            } catch (Exception e) {
                Log.i("LAT", "停止异常！");

            }
        });
        next.setOnClickListener(view -> {                          //下一曲
            try {
                musicService.next();
            } catch (Exception e) {
                Log.i("LAT", "下一曲异常！");
            }
        });
        last.setOnClickListener(view -> {                         //下一曲
            try {
                musicService.last();
            } catch (Exception e) {
                Log.i("LAT", "上一曲异常！");
            }
        });
        rand.setOnClickListener(view -> {

        });
        order.setOnClickListener(view -> {                       //顺序播放
            musicService.play();
        });




        seekBar = findViewById(R.id.seekBar);                                    //进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {//用于监听SeekBar进度值的改变
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//用于监听SeekBar开始拖动
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//用于监听SeekBar停止拖动  SeekBar停止拖动后的事件
                int progress = seekBar.getProgress();
                Log.i("TAG:", "" + progress + "");
                int musicMax = musicService.player.getDuration(); //得到该首歌曲最长秒数
                int seekBarMax = seekBar.getMax();
                musicService.player
                        .seekTo(musicMax * progress / seekBarMax);//跳到该曲该秒
            }
        });
/*
        Thread t = new Thread((Runnable) this);// 自动改变进度条的线程
        //实例化一个handler对象
        //更新UI
        //最大秒数
        // 处理改变进度条事件
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //更新UI
                int mMax = musicService.player.getDuration();//最大秒数
                if (msg.what == UPDATE) {
                    try {
                        seekBar.setProgress(msg.arg1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    seekBar.setProgress(0);
                }
            }
        };
        t.start();
*/
    }
                                                                //从指定路径获得歌曲并添加到歌去列表中
    private void setListViewAdapter() {
        String[] str = new String[musicService.musicList.size()];
        int i = 0;
        for (String path : musicService.musicList) {
            File file = new File(path);
            str[i++] = file.getName();
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, str);
        listView.setAdapter(adapter);
    }


    //设置当前播放的信息

    private String setPlayInfo(int position, int max) {
        String info = "正在播放:  " + musicService.songName + "\t\t";
        int pMinutes = 0;
        while (position >= 60) {
            pMinutes++;
            position -= 60;
        }
        String now = (pMinutes < 10 ? "0" + pMinutes : pMinutes) + ":"
                + (position < 10 ? "0" + position : position);
        int mMinutes = 0;
        while (max >= 60) {
            mMinutes++;
            max -= 60;
        }
        String all = ((mMinutes < 10) ? ("0" + mMinutes) : mMinutes) + ":"
                + ((max < 10) ? ("0" + max) : max);
        return info + now + " / " + all;
    }

    public void run() {
        int position, mMax, sMax;
        while (!Thread.currentThread().isInterrupted()) {
            if (musicService.player != null && musicService.player.isPlaying()) {
                position = musicService.getCurrentProgress();//得到当前歌曲播放进度(秒)
                mMax = musicService.player.getDuration();//最大秒数
                sMax = seekBar.getMax();//seekBar最大值，算百分比
                Message m = handler.obtainMessage();//获取一个Message
                m.arg1 = position * sMax / mMax;//seekBar进度条的百分比
                m.arg2 = position;
                m.what = UPDATE;
                handler.sendMessage(m);
                //  handler.sendEmptyMessage(UPDATE);
                try {
                    Thread.sleep(1000);// 每间隔1秒发送一次更新消息
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void openAssetMusics() {
        try {
            //播放 assets/a2.mp3 音乐文件
            AssetFileDescriptor fd = getAssets().openFd("胡歌-忘记时间.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}