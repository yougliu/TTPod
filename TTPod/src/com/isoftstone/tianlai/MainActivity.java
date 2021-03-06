package com.isoftstone.tianlai;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.isoftstone.tianlai.domain.Music;
import com.isoftstone.tianlai.media.MediaService;
import com.isoftstone.tianlai.util.LyrcUtil;
import com.isoftstone.tianlai.view.LyricShow;
import com.isoftstone.tianlai.view.ScrollableViewGroup;
import com.isoftstone.tianlai.view.ViewGroupHook;
import com.isoftstone.tianlai.view.ScrollableViewGroup.OnCurrentViewChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	
	// string
	private static final String TIANLAIZHIYIN = "tianlaizhiyin";
	private static final String LASTPATH = "lastPath";
	private static final String PLAYMODE = "playMode";
	public static final String ONPLAYCOMPLETED = "onplaycompleted";
	public static final String LRCPATH = "/sdcard/TTPod/lyric/";
	
	public static final int PLAY_STOP = 0;
	public static final int PLAY_PAUSE = 1;
	public static final int PLAY_PLAY = 2;
	
	public int currentPlayStatus = PLAY_STOP;
	//data
	public ArrayList<Music> musicList = new ArrayList<Music>();
	public static int currentPosition = 0;
	//adapter
	private MyAdapter listAdapter;
	
	//notification
	private NotificationManager notifManager;
	//Lrc
	public static LyrcUtil lycrUtil;
	
	//playMode
	public static final int SINGE = 0;
	public static final int SINGE_R = 1;
	public static final int REPEAT = 2;
	public static final int ORDER = 3;
	public int currentPlayMode = 0;
	
	//views
	//TODO
	private RelativeLayout framelayout;
	private ScrollableViewGroup scrollView;
	private ViewGroupHook playLayout;
	private ViewGroupHook lyrcLayout;
	private static TextView playTime;
	private TextView durationTime;
	private TextView currentMusicTitle;
	private TextView currentLrc;
	private static SeekBar seekBar;
	private LinearLayout listLayout;
	private ListView musicListView;
	private static LyricShow lrcShow;
	
	private ImageView playMode;
//	private ImageView preMusic;
//	private ImageView nextMusic;
	private ImageView playAndPause;
	private ImageView settings;
	private ImageButton playModeButton;
	private ImageButton preMusicButton;
	private ImageButton nextMusicButton;
	private ImageButton playAndPauseButton;
	private ImageButton settingsPauseButton;
	
	private ImageButton mainImageButton;
	private ImageButton listImageButton;
	private ImageButton lyricImageButton;
	private ImageButton volumeImageButton;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			int playMode = getPlayMode();
			if(playMode == SINGE){
				
				playAndPause.setImageDrawable(getResources().getDrawable(R.drawable.img_playback_bt_play));
			}else if(playMode == SINGE_R){
				
				currentPosition --;
				//下一曲
				//playNext();
			}else if(playMode == REPEAT){
				
				if(currentPosition < musicList.size()){
					
					currentPosition++;
					play();
				}else{
					currentPosition = 0;
					play();
				}
			}else if(playMode == ORDER){
				
				if(currentPosition >= musicList.size()){
					
					playAndPause.setImageDrawable(getResources().getDrawable(R.drawable.img_playback_bt_play));
				}else{
					currentPosition++;
					play();
				}
			}
		}
	};
	
	public static Handler seekBarHandle = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case -1:
				lycrUtil.RefreshLRC(msg.getData().getInt("posiztion"));
				lrcShow.SetNowPlayIndex(msg.getData().getInt("posiztion"));
				int j = msg.getData().getInt("posiztion")/1000;
				String pos = "";
				int min = j/60;
				int sec = j%60;
				
				if(min > 9){
					if(sec > 9){
						pos = min + ":" + sec;
					}
					if(sec <= 9){
						pos = min + ":0" +sec;
					}
				}else{
					if(sec > 9){
						pos = "0" + min + ":" + sec;
					}
					if(sec <= 9){
						pos = "0" +min + ":0" +sec;
					}
				}
				playTime.setText(pos);
				seekBar.setProgress(msg.getData().getInt("posiztion"));
				break;

			}
			
			super.handleMessage(msg);
		}
	};
	private OnClickListener l = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			//TODO
			if(v.getId() == R.id.IndMain){
				
				scrollView.setCurrentView(0);
			}else if(v.getId() == R.id.IndList){
				
				scrollView.setCurrentView(1);
			}else if(v.getId() == R.id.IndLyric){
				
				scrollView.setCurrentView(2);
			}else if(v.getId() == R.id.ImgBtnVolume){

				
			}else if(v.getId() == R.id.IndPlayMode){
				
				changePlayMode();
				
			}else if(v.getId() == R.id.btnPrev && musicList.size()>0){
				if(currentPosition-1>=0){
					
					currentPosition--;
				}else{
					
					currentPosition = musicList.size();
				}
				play();
			}else if(v.getId() == R.id.btnPlay && musicList.size()>0){
				
				if(currentPlayStatus == PLAY_PLAY){
					
					pause();
				}else if(currentPlayStatus == PLAY_PAUSE){
					
					continuing();
				}else if(currentPlayStatus == PLAY_STOP){
					
					play();
				}
				
			}else if(v.getId() == R.id.btnNext && musicList.size()>0){
				
				if(currentPosition+1<=musicList.size()){
					currentPosition++;
					play();
				}else{
					currentPosition = 0;
				}
				play();
			}else if(v.getId() == R.id.IndMenu){
			
			}
			
		}
	};

	private OnSeekBarChangeListener seekBarChageListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
				int posiztion = seekBar.getProgress();
				Intent i = new Intent();
				i.setAction(MediaService.ACTION);
				i.putExtra("mean", MediaService.SEEK);
				i.putExtra("posiztion", posiztion);
				sendBroadcast(i);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
		}
	};
	
	private OnCurrentViewChangedListener mOnCurrentViewChangedListener = new OnCurrentViewChangedListener() {
		
		@Override
		public void onCurrentViewChanged(View view, int currentview) {
			
			if(currentview == 0){
				mainImageButton.setEnabled(false);
				listImageButton.setEnabled(true);
				lyricImageButton.setEnabled(true);
			}else if(currentview == 1){
				
				listImageButton.setEnabled(false);
				mainImageButton.setEnabled(true);
				lyricImageButton.setEnabled(true);
			}else if(currentview == 2){
				
				lyricImageButton.setEnabled(false);
				mainImageButton.setEnabled(true);
				listImageButton.setEnabled(true);
			}
			
		}
	};
	
	private OnItemClickListener listListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			
			currentPosition = position;
			play();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        new InitDataTask().execute((Void[])null);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		cancelNotification();
	}

	@Override
	protected void onDestroy() {
		if(musicList.size() > 0){
			
			setLastPath(musicList.get(currentPosition).getPath());
		}
		
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			
			showNotification();
			 if(keyCode == KeyEvent.KEYCODE_BACK){  

				 Intent intent = new Intent(Intent.ACTION_MAIN);  
				 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意  
				 intent.addCategory(Intent.CATEGORY_HOME);  
				 this.startActivity(intent);  
				 
				 return true;  
			 }  
		}
		
		if(keyCode == KeyEvent.KEYCODE_MENU){
			
			showDialog();
			return true;
		}
		
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			
			AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
			manager.getStreamVolume(AudioManager.STREAM_RING);
			manager.adjustVolume( AudioManager.ADJUST_LOWER, 0);
			return true;
		}
		
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			
			AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
			manager.getStreamVolume(AudioManager.STREAM_RING);
			manager.adjustVolume( AudioManager.ADJUST_RAISE, manager.getStreamMaxVolume(AudioManager.STREAM_RING));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	
	private void exit(){
		
		Intent i = new Intent(this,MediaService.class);
		stopService(i);
		super.onDestroy();
	}

	private void showNotification() {

		notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.icon = R.drawable.icon_notification_play;
		
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, "天籁之音", null, contentIntent);
		
		notifManager.notify(0,notification);
	}
	
	private void cancelNotification() {

		notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notifManager.cancel(0);
	}

	private void showDialog() {
		
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle("提醒");
		dialog.setMessage("确定退出天籁之音吗？");
		dialog.setButton2("取消", (android.content.DialogInterface.OnClickListener) null);
		dialog.setButton("确定",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
						
				finish();
				exit();
			}
		});
		dialog.show();
	}

	//method
	public String getLastPath(){
    	
    	SharedPreferences preference = getSharedPreferences(TIANLAIZHIYIN, Context.MODE_PRIVATE);
    	String path = preference.getString(LASTPATH, "");
    	return path;
    }
    
	private void setLastPath(String path) {
		SharedPreferences preference = getSharedPreferences(TIANLAIZHIYIN, Context.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putString(LASTPATH, path);
		editor.commit();
	}
	
	public int getPlayMode(){
    	
    	SharedPreferences preference = getSharedPreferences(TIANLAIZHIYIN, Context.MODE_PRIVATE);
    	int mode = preference.getInt(PLAYMODE, 0);
    	return mode;
    }
	
	protected void changePlayMode() {
		
		int current = getPlayMode();
		if(current == SINGE){
			
			setPlayMode(SINGE_R);
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_repeat_single));
		}else if(current == SINGE_R){
			
			setPlayMode(REPEAT);
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_repeat));
		}else if(current == REPEAT){
			
			setPlayMode(ORDER);
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_normal));
		}else if(current == ORDER){
			
			setPlayMode(SINGE);
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_shuffle));
		}
	}
	
	private void setPlayMode(int mode) {
		SharedPreferences preference = getSharedPreferences(TIANLAIZHIYIN, Context.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putInt(PLAYMODE, mode);
		editor.commit();
	}
	
	private void initMusics() {
		
		Cursor cur = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA},
                null, null, null);
		
		if(cur!=null){
			Log.d("bonus", "cur is null?"+(cur == null));
			while(cur.moveToNext()){
				Music m = new Music();
				m.setTitle(cur.getString(0));
				Log.d("bonus", cur.getString(0)+"");
				m.setDuration(cur.getString(1));
				m.setArtist(cur.getString(2));
				m.setId(cur.getString(3));
				m.setPath(cur.getString(4));
				Log.d("bonus", cur.getString(4)+"");
				musicList.add(m);
			}
			Log.d("bonus", musicList.size()+"");
		}else{
			Log.d("bonus", "cur is null?"+(cur == null));
		}
	}
	
	private void initViews() {
		//TODO
		framelayout = (RelativeLayout)LayoutInflater.from(getApplicationContext()).inflate(R.layout.framelayout, null);
		
		scrollView = (ScrollableViewGroup) framelayout.findViewById(R.id.ViewFlipper);
		scrollView.setOnCurrentViewChangedListener(mOnCurrentViewChangedListener);
		
		lyrcLayout = (ViewGroupHook) scrollView.findViewById(R.id.frmLyric);
		lrcShow = (LyricShow) lyrcLayout.findViewById(R.id.LyricShow);
		
		playLayout = (ViewGroupHook) scrollView.findViewById(R.id.frmMain);
		currentLrc = (TextView) playLayout.findViewById(R.id.MiniLyricShow);
		playTime = (TextView) playLayout.findViewById(R.id.txtLapse);
		durationTime = (TextView) playLayout.findViewById(R.id.txtDuration);
		seekBar = (SeekBar) playLayout.findViewById(R.id.skbGuage);
		seekBar.setClickable(true);
		seekBar.setOnSeekBarChangeListener(seekBarChageListener);
		
		playMode = (ImageView) framelayout.findViewById(R.id.imgPlayMode);
//		preMusic = (ImageView) framelayout.findViewById(R.id.imgPrev);
		playAndPause = (ImageView) framelayout.findViewById(R.id.imgPlay);
//		nextMusic = (ImageView) framelayout.findViewById(R.id.btnNext);
		settings = (ImageView) framelayout.findViewById(R.id.ImgMenu);
		
		listLayout = (LinearLayout) scrollView.findViewById(R.id.frmList);
		musicListView = (ListView) listLayout.findViewById(R.id.PlayList);
		listAdapter = new MyAdapter();
		musicListView.setAdapter(listAdapter);
		musicListView.setOnItemClickListener(listListener );
		
		mainImageButton = (ImageButton) framelayout.findViewById(R.id.IndMain);
		listImageButton = (ImageButton) framelayout.findViewById(R.id.IndList);
		lyricImageButton = (ImageButton) framelayout.findViewById(R.id.IndLyric);
		volumeImageButton = (ImageButton) framelayout.findViewById(R.id.ImgBtnVolume);
		currentMusicTitle = (TextView) framelayout.findViewById(R.id.txtTitle);
		
		playModeButton = (ImageButton) framelayout.findViewById(R.id.IndPlayMode);
		preMusicButton = (ImageButton) framelayout.findViewById(R.id.btnPrev);
		playAndPauseButton = (ImageButton) framelayout.findViewById(R.id.btnPlay);
		nextMusicButton = (ImageButton) framelayout.findViewById(R.id.btnNext);
		settingsPauseButton = (ImageButton) framelayout.findViewById(R.id.IndMenu);
		
		playModeButton.setOnClickListener(l);
		preMusicButton.setOnClickListener(l);
		playAndPauseButton.setOnClickListener(l);
		nextMusicButton.setOnClickListener(l);
		settingsPauseButton.setOnClickListener(l);
		
		mainImageButton.setOnClickListener(l);
		listImageButton.setOnClickListener(l);
		lyricImageButton.setOnClickListener(l);
		volumeImageButton.setOnClickListener(l);
		
		initCurrentData();
	}
	
	private void play(){
		
		playAndPause.setImageDrawable(getResources().getDrawable(R.drawable.img_playback_bt_pause));
		String path = musicList.get(currentPosition).getPath();
//		String duration = durationToString(musicList.get(currentPosition).getDuration());
//		durationTime.setText(duration);
		currentMusicTitle.setText("当前歌曲:"+musicList.get(currentPosition).getTitle());
		Intent i = new Intent();
		i.setAction(MediaService.ACTION);
		i.putExtra("mean", MediaService.PLAY);
		i.putExtra("path", path);
		sendBroadcast(i);
		initLrc();
		currentPlayStatus = PLAY_PLAY;
		listAdapter.notifyDataSetChanged();
	}

	private void pause() {
		
		playAndPause.setImageDrawable(getResources().getDrawable(R.drawable.img_playback_bt_play));
		Intent i = new Intent();
		i.setAction(MediaService.ACTION);
		i.putExtra("mean", MediaService.PAUSE);
		sendBroadcast(i);
		currentPlayStatus = PLAY_PAUSE;
	}

	private void continuing() {

		playAndPause.setImageDrawable(getResources().getDrawable(R.drawable.img_playback_bt_pause));
		Intent i = new Intent();
		i.setAction(MediaService.ACTION);
		i.putExtra("mean", MediaService.CONTIUNING);
		sendBroadcast(i);
		currentPlayStatus = PLAY_PLAY;
	}
	
	private void initLrc() {
		
		String s = musicList.get(currentPosition).getTitle();
		String name = musicList.get(currentPosition).getArtist();

		String path = LRCPATH;
		File file = new File(path);
		File lrc = null;
		
		if(file.isDirectory()){
			
			File f[] = file.listFiles();
			for(int i=0;i<f.length;i++){
				if(f[i].getName().contains(s)){
					
					lrc = f[i];
				}
				if(f[i].getName().substring(0,f[i].getName().indexOf(".")).equals(name+"-"+s)){
					
					lrc = f[i];
					break;
				}
			}
		}
		
		
			
		lycrUtil.ReadLRC(lrc);
		
		lrcShow.SetTimeLrc(lycrUtil.getLrcList());
		
		if(lycrUtil.getLrcList()!=null){
			lycrUtil.RefreshLRC(10);
			lrcShow.SetNowPlayIndex(10);
		}
		
	}
	
	private void initCurrentData() {
		
		initCurrentPosition();
		initPlayMode();
	}

	public void initCurrentPosition(){
		
		if(getLastPath().trim()!=""){
			
			for(int i=0;i<musicList.size();i++){
				
				if(musicList.get(i).getPath().equals(getLastPath())){
					
					currentPosition = i;
				}
			}
			if(musicList.size() >0){
				
//				durationTime.setText(durationToString(musicList.get(currentPosition).getDuration()));
				currentMusicTitle.setText("当前歌曲:"+musicList.get(currentPosition).getTitle());
			}
		}
	}
	
	public void initPlayMode(){
		
		int current = getPlayMode();
		if(current == SINGE){
			
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_shuffle));
			
		}else if(current == SINGE_R){
			
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_repeat_single));
			
		}else if(current == REPEAT){
			
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_repeat));
			
		}else if(current == ORDER){
			
			playMode.setImageDrawable(getResources().getDrawable(R.drawable.icon_playmode_normal));
			
		}
		
	}
	
	public String durationToString(String duration){
		
		String reVal ="";
		int j = Integer.valueOf(duration);
		int i = j/1000;
		int min = (int) i/60;
		int sec = i%60;
		if(min > 9){
			if(sec > 9){
				reVal = min + ":" + sec;
			}
			if(sec <= 9){
				reVal = min + ":0" +sec;
			}
		}else{
			if(sec > 9){
				reVal = "0" + min + ":" + sec;
			}
			if(sec <= 9){
				reVal = "0" +min + ":0" +sec;
			}
		}
		seekBar.setMax(j);
		return reVal;
	}
	
	public void setLRCText(String currentLr,boolean isEnd){
		
		currentLrc.setText(currentLr);
	}
	
	// class
	class InitDataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			//singer
			initMusics();
			lycrUtil = new LyrcUtil(MainActivity.this);
			initViews();
			Intent i = new Intent(MainActivity.this,MediaService.class);
			startService(i);
			registerReceiver(receiver,new IntentFilter(ONPLAYCOMPLETED));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			setContentView(framelayout);
			initLrc();
		}
	}
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
				
			return musicList.size();
		}

		@Override
		public Object getItem(int arg0) {
			
			return musicList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder;
			if(convertView == null){
				
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listitem, null);
				holder.tx1 = (TextView) convertView.findViewById(R.id.ListItemName);
				holder.tx2 = (TextView) convertView.findViewById(R.id.ListItemContent);
				
				convertView.setTag(holder);
			}else{
				
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(position == currentPosition){
				
				holder.tx1.setTextColor(Color.GREEN);
			}else{
				holder.tx1.setTextColor(Color.WHITE);
			}
			holder.tx1.setText((position+1)+"."+musicList.get(position).getTitle());
			holder.tx2.setText((musicList.get(position)).getArtist());
			
			return convertView;
		}
	}
	
	class ViewHolder{
		
		public TextView tx1;
		public TextView tx2;
	}
}