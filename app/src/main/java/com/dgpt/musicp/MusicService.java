package com.dgpt.musicp;

import java.io.IOException;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
	private static final String TAG = "MusicService";
	public MediaPlayer mediaPlayer;

	class MyBinder extends Binder {
		MusicService getService(){
			return MusicService.this;
		}
	}

	public void onCreate() {
		super.onCreate();
	}

	// 播放音乐
	public void play(String path) {
		try {
			if (mediaPlayer == null) {
				Log.i(TAG, "开始播放音乐");
				// 创建一个MediaPlayer播放器
				mediaPlayer = new MediaPlayer();
				// 指定参数为音频文件
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				// 指定播放的路径
				mediaPlayer.setDataSource(path);
				// 准备播放
				mediaPlayer.prepare();
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						// TODO Auto-generated method stub
						// 开始播放
						mediaPlayer.start();
					}
				});
			} else {
				int position = getCurrentProgress();
				mediaPlayer.seekTo(position);
				try {
					mediaPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				}
				mediaPlayer.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 暂停音乐
	public void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			Log.i(TAG, "播放暂停");
			mediaPlayer.pause(); // 暂停播放
		} else if (mediaPlayer != null && (!mediaPlayer.isPlaying())) {
			mediaPlayer.start();
		}
	}

	// 重新播放音乐
	public void replay(String path) {
		if (mediaPlayer != null) {
			Log.i(TAG, "重新开始播放");
			mediaPlayer.seekTo(0);
			try {
				mediaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			mediaPlayer.start();
		}
	}

	// 停止音乐
	public void stop() {
		if (mediaPlayer != null) {
			Log.i(TAG, "停止播放");
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		} else {
			Toast.makeText(getApplicationContext(), "已停止", Toast.LENGTH_SHORT).show();
		}
	}

	// 获取资源文件的长度
	public int getMusicLength() {
		if (mediaPlayer != null) {
			return mediaPlayer.getDuration();
		}
		return 0;
	}

	// 获取当前进度
	public int getCurrentProgress() {
		if (mediaPlayer != null & mediaPlayer.isPlaying()) {
			Log.i(TAG, "获取当前进度");
			return mediaPlayer.getCurrentPosition();
		} else if (mediaPlayer != null & (!mediaPlayer.isPlaying())) {
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		// 第一步执行onBind方法
		return new MyBinder();
	}
}
