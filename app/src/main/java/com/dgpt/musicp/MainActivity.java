package com.dgpt.musicp;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.dgpt.musicp.MusicService.MyBinder;

public class MainActivity extends Activity implements OnClickListener {
	private EditText path;
	private Intent intent;
	private MyConn conn;
	private MyBinder binder;
	private SeekBar mSeekBar;
	private TextView tv_now;
	private MusicService musicService;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 100:
					int currentPosition = (Integer) msg.obj;
					mSeekBar.setProgress(currentPosition);
					tv_now.setText(ShowTime(currentPosition));
					break;
				default:
					break;
			}
		};
	};
	private Thread mThread;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		path = (EditText) findViewById(R.id.et_inputpath);
		findViewById(R.id.bt_play).setOnClickListener(this);
		findViewById(R.id.bt_pause).setOnClickListener(this);
		findViewById(R.id.bt_replay).setOnClickListener(this);
		findViewById(R.id.bt_stop).setOnClickListener(this);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		tv_now=(TextView)findViewById(R.id.tv_now);
		conn = new MyConn();
		intent = new Intent(this, MusicService.class);
		bindService(intent, conn, BIND_AUTO_CREATE);

		if (Build.VERSION.SDK_INT >= 23) {
			int REQUEST_CODE_CONTACT = 101;
			String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
			//验证是否许可权限
			for (String str : permissions) {
				if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
					//申请权限
					this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
				}
			}
		}

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				tv_now.setText(ShowTime(seekBar.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				tv_now.setText(ShowTime(seekBar.getProgress()));
				//将media进度设置为当前seekbar的进度
				musicService.mediaPlayer.seekTo(seekBar.getProgress());

			}
		});
	}

	//时间显示函数,我们获得音乐信息的是以毫秒为单位的，把把转换成我们熟悉的00:00格式
	public String ShowTime(int time) {
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}


	// 初始化进度条的长度,获取音乐文件的长度
	private void initSeekBar() {
		// TODO Auto-generated method stub
		int musicWidth = musicService.getMusicLength();
		mSeekBar.setMax(musicWidth);
	}

	// 更新音乐播放的进度
	private void UpdateProgress() {
		mThread = new Thread() {
			public void run() {
				while (!interrupted()) {
					// 调用服务中的获取当前播放进度
					int currentPosition = musicService.getCurrentProgress();
					Message message = Message.obtain();
					message.obj = currentPosition;
					message.what = 100;
					handler.sendMessage(message);
				}
			};
		};
		mThread.start();
	}

	private class MyConn implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MyBinder) service;
			musicService=binder.getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			musicService=null;
		}
	}

	public void onClick(View v) {
		String pathway = path.getText().toString().trim();
		File SDpath = Environment.getExternalStorageDirectory();
		File file = new File(SDpath, pathway);
		String path = file.getAbsolutePath();
		Log.i("Mainactivity", path);
		switch (v.getId()) {
			case R.id.bt_play:
				if (file.exists() && file.length() > 0) {
					musicService.play(path);
					initSeekBar();
					UpdateProgress();
				}else{
					Toast.makeText(this, "找不到音乐文件", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.bt_pause:
				musicService.pause();;
				break;
			case R.id.bt_replay:
				musicService.replay(pathway);
				break;
			case R.id.bt_stop:
				// 停止音乐之前首先要退出子线程
				mThread.interrupt();
				if (mThread.isInterrupted()) {
					musicService.stop();
				}
				tv_now.setText("00:00" );
				mSeekBar.setProgress(0);
				break;
		}
	}

	protected void onDestroy() {
		// 如果线程没有退出,则退出
		if (mThread != null & !mThread.isInterrupted()) {
			mThread.interrupt();
		}
		unbindService(conn);
		super.onDestroy();
	}
}
