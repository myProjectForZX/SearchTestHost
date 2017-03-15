package com.example.searchHost;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.ActionBarActivity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnClickListener, OnItemClickListener {
	private static final String TAG = "HostMain";
	private Button bt_search;
	private EditText passwordEdit;
	private MyHandler mHandler = new MyHandler();
	
	private static final int UI_SEARCH_START = 100;
	private static final int UI_SEARCH_FAIL  = 101;
	private static final int UI_SEARCHING  = 102;
	private static final int UI_SEARCH_SUCC  = 103;
	
	private TextView mDeviceNameId = null;
	private TextView mDeviceIpId = null;
	private TextView mDeviceLangId = null;
	private TextView mDeviceTimeId = null;
	private TextView mDeviceAudiId = null;
	
	private TextView mDeviceNameContent = null;
	private EditText mDeviceIpEditText = null;
	private EditText mDeviceTimeEditText = null;
	private EditText mDeviceAudioEditText = null;
	
	private Spinner  mDeviceLangSpinner = null;
	private Spinner  mDeviceAudiSpinner = null;
	
	private Button   mDeviceIpButton = null;
	private Button   mDeviceLangButton = null;
	private Button   mDeviceTimeButton = null;
	private Button   mDeviceAudioButton = null;
	private Button   mDeviceContactButton = null;
	private Button   mDeviceQuitButton  = null;
	
	private String currentLangauge = null;
	private int    currentAudioType = 0;
	private int    currentAudioValue = 0;
	private int    maxAudioValue     = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}
	
	private void initView() {
		bt_search = (Button)findViewById(R.id.bt_search);
		passwordEdit = (EditText)findViewById(R.id.passwordEdit);
		
		mDeviceNameId = (TextView)findViewById(R.id.DeviceNameId);
		mDeviceIpId = (TextView)findViewById(R.id.DeviceIP);
		mDeviceLangId = (TextView)findViewById(R.id.DeviceLanauge);
		mDeviceTimeId = (TextView)findViewById(R.id.DeviceTime);
		mDeviceAudiId = (TextView)findViewById(R.id.DeviceAudio);
		mDeviceNameContent = (TextView)findViewById(R.id.DeviceNameString);
		
		mDeviceIpEditText = (EditText)findViewById(R.id.DeviceIPValue);
		mDeviceTimeEditText = (EditText)findViewById(R.id.DeviceTimeValue);
		mDeviceAudioEditText = (EditText)findViewById(R.id.DeviceAudioVaule);
		
		mDeviceLangSpinner = (Spinner)findViewById(R.id.DeviceLangValue);
		mDeviceAudiSpinner = (Spinner)findViewById(R.id.DeviceAudioType);
		
		mDeviceIpButton = (Button)findViewById(R.id.DeviceIPChangeButton);
		mDeviceLangButton = (Button)findViewById(R.id.DeviceLangButton);
		mDeviceTimeButton = (Button)findViewById(R.id.DeviceTimeChangeButton);
		mDeviceAudioButton = (Button)findViewById(R.id.DeviceAudioChangeButton);
		mDeviceContactButton = (Button)findViewById(R.id.DeviceContact);
		mDeviceQuitButton  = (Button)findViewById(R.id.button_quit);
		
		bt_search.setOnClickListener(this);
		mDeviceIpButton.setOnClickListener(this);
		mDeviceLangButton.setOnClickListener(this);
		mDeviceTimeButton.setOnClickListener(this);
		mDeviceAudioButton.setOnClickListener(this);
		mDeviceContactButton.setOnClickListener(this);
		mDeviceQuitButton.setOnClickListener(this);
		
		updateUiToConnectStat(UI_SEARCH_START);
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_search:
			Log.i(TAG, "click-host-searchDevices_broadcast()");
			if (passwordEdit.getText().length() == 0) {
				Toast.makeText(
						this,
						getApplicationContext().getResources().getString(
								R.string.please_entry_password),
						Toast.LENGTH_SHORT).show();
			} else {
				searchDevices_broadcast(mHandler, passwordEdit.getText()
						.toString());
			}
			break;
		case R.id.DeviceIPChangeButton:
			break;
		case R.id.DeviceTimeChangeButton:
			break;
		case R.id.DeviceLangButton:
			break;
		case R.id.DeviceAudioChangeButton:
			break;
		case R.id.DeviceContact:
			break;
		case R.id.button_quit:
			break;
		default:
			break;
		}
	}
	
	private void searchDevices_broadcast(final Handler handler, final String password) {
		new DeviceSearcher(handler, password) {
			@Override
			public void onSearchStart() {
				startSearch(); // 主要用于在UI上展示正在搜索
			}

			@Override
			public void onSearchFinish(Set deviceSet) {
				Log.i(TAG, "onSearchFinish-deviceSet="+deviceSet.size());
			}

		}.start();
	}

	private void startSearch() {
		mHandler.sendEmptyMessage(UI_SEARCH_START);
	}
	
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case UI_SEARCH_START:
				updateUiToConnectStat(UI_SEARCHING);
				break;
			case UI_SEARCH_FAIL:
				updateUiToConnectStat(UI_SEARCH_FAIL);
				break;

			case DataPackHost.DEVICE_FIND:
				if(msg.arg1 == DataPackHost.DEVICE_CONNECTED) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_right), Toast.LENGTH_SHORT).show();
					final String deviceIp = (String)msg.obj;
					
					new Timer().schedule(new TimerTask() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							//连接建立之后发送请求全部数据的请求
							byte[] dataType = new byte[]{DataPackHost.PACKET_DATA_TYPE_DEVICE_ALL};
							String[] dataContent = new String[]{"empty"};
							new HostSendDataThread(mHandler, deviceIp, dataType, dataContent).start();
						}
					}, 2 * 1000);
					
					new  HostRecvDataThread(mHandler, deviceIp).start();
					
					updateUiToConnectStat(UI_SEARCH_SUCC);
				} else if (msg.arg1 == DataPackHost.DEVICE_NOT_CONNECTED) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_bad), Toast.LENGTH_SHORT).show();
					updateUiToConnectStat(UI_SEARCH_FAIL);
				}
				break;
			case DataPackHost.PACKET_TYPE_SEND_RECV_DATA:
				String result  = (String)msg.obj;
				switch (msg.arg1) {
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_NAME:
					updateDeviceName(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_TIME:
					updateDeviceTime(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_LANG:
					updateDeviceLang(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_ETIP:
					updateDeviceIpAddress(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_AUDI:
					updateDeviceAudio(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_CONT:
					break;
				default:
					break;
				}
				break;
			default:
					break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	private void updateUiToConnectStat(int uiType) {
		if(uiType == UI_SEARCH_START || uiType == UI_SEARCH_FAIL) {
			showSearchUI(true, true);
			showDeviceUI(false);
		} else if (uiType == UI_SEARCHING) {
			showSearchUI(true, false);
			showDeviceUI(false);
		} else if (uiType == UI_SEARCH_SUCC) {
			showSearchUI(false, false);
			showDeviceUI(true);
		}
	}
	
	private void showSearchUI(boolean show, boolean enable) {
		int visible = (show ? View.VISIBLE : View.GONE);
		bt_search.setVisibility(visible);
		bt_search.setEnabled(enable);
		passwordEdit.setVisibility(visible);
	}
	
	private void showDeviceUI(boolean show) {
		int visible = (show ? View.VISIBLE : View.GONE);
		
		mDeviceNameId.setVisibility(visible);
		mDeviceIpId.setVisibility(visible);
		mDeviceLangId.setVisibility(visible);
		mDeviceTimeId.setVisibility(visible);
		mDeviceAudiId.setVisibility(visible);
		
		mDeviceNameContent.setVisibility(visible);
		mDeviceIpEditText.setVisibility(visible);
		mDeviceTimeEditText.setVisibility(visible);
		mDeviceAudioEditText.setVisibility(visible);
		
		mDeviceLangSpinner.setVisibility(visible);
		mDeviceAudiSpinner.setVisibility(visible);
		
		mDeviceIpButton.setVisibility(visible);
		mDeviceLangButton.setVisibility(visible);
		mDeviceTimeButton.setVisibility(visible);
		mDeviceAudioButton.setVisibility(visible);
		mDeviceContactButton.setVisibility(visible);
		mDeviceQuitButton.setVisibility(visible);
	}
	
	private void updateDeviceName(String deviceName) {
		if(deviceName == null || deviceName.isEmpty())
			return;
		
		mDeviceNameContent.setText(deviceName);
	}
	
	private void updateDeviceIpAddress(String ip) {
		if(ip == null || ip.isEmpty())
			return;
		Log.e(TAG, "-----------------> updateDeviceIpAddress : " + ip);
		mDeviceIpEditText.setText(ip);
	}
	
	private void updateDeviceTime(String time) {
		if(time == null || time.isEmpty())
			return;
		Log.e(TAG, "-----------------> updateDeviceTime : " + time);
		mDeviceTimeEditText.setText(time);
	}
	
	private void updateDeviceLang(String lang) {
		if(lang == null || lang.isEmpty())
			return;
		Log.e(TAG, "-----------------> updateDeviceLang : " + lang);
		String[] langArray = lang.split("\\+");
		if(langArray != null) {
			currentLangauge = langArray[0];
		}
		String[] mItems = null;
		ArrayAdapter<String> adapter = null;
		if(langArray != null && langArray.length >= 2) {
			mItems = langArray[1].split(":");
			
			if(mItems != null)
			  adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, mItems);
			
			if(adapter != null)
				mDeviceLangSpinner.setAdapter(adapter);
		}
	}
	
	private void updateDeviceAudio(String audio) {
		if(audio == null || audio.isEmpty())
			return;
		Log.e(TAG, "-----------------> updateDeviceAudio : " + audio);
		String[] audioArrayTemp = audio.split("\\+");
		
		if(audioArrayTemp != null) {
			String[] currentAudioArray = audioArrayTemp[0].split(":");
			if(currentAudioArray != null && currentAudioArray.length == 3) {
				currentAudioType = Integer.valueOf(currentAudioArray[0]);
				currentAudioValue = Integer.valueOf(currentAudioArray[1]);
				maxAudioValue     = Integer.valueOf(currentAudioArray[2]);
			}
			
			if(audioArrayTemp.length == 2) {
				String[] allAudioArray = audioArrayTemp[1].split(":");
				
				ArrayAdapter<String> adapter = null;
				String[] mItems = null;
				if(allAudioArray != null) {
					mItems = new String[allAudioArray.length];
					
					for(int i = 0; i < allAudioArray.length; ++i) {
						String[] tempAudio = allAudioArray[i].split("-");
						
						if(tempAudio != null && tempAudio.length == 3) {
							mItems[i] = tempAudio[0];
						}
					}
				}
				
				if(mItems != null) {
					adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, mItems);
				}
				
				if(adapter != null) {
					mDeviceAudiSpinner.setAdapter(adapter);
				}
			}
		}
	}
}
