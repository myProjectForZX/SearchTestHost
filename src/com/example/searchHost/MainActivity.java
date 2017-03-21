package com.example.searchHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
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
	private static final int UI_CONTACT_SHOW  = 104;
	
	private TextView mDeviceNameId = null;
	private TextView mDeviceIpId = null;
	private TextView mDeviceLangId = null;
	private TextView mDeviceTimeId = null;
	private TextView mDeviceAudiId = null;
	private TextView mDeviceContactTitle = null;
	
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
	private Button   mDeviceContactBackButton = null;
	private Button   mDeviceContactChangeButton = null;
	
	private ScrollView mContactScrollView = null;
	
	private String currentLangauge = null;
	
	private String currentAudioType = null;
	private int    currentAudioValue = 0;
	private int    currentMaxAudioValue = 0;
	
	private ArrayList<AudioTypeBean> mAllAudioValue = null;
	
	private String mDeviceIp = null;
	
	private String[] mLangItems = null;
	private String[] mAudioItems = null;
	
	private ContactDataAdapter mContactDataAdapter = null;
	private ContactListView mContactListView = null;
	List<HashMap<String, String>> allContactMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		mAllAudioValue = new ArrayList<AudioTypeBean>();
		allContactMap = new ArrayList<HashMap<String,String>>();
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
		mDeviceContactTitle = (TextView)findViewById(R.id.contact_title);
		mDeviceContactBackButton = (Button) findViewById(R.id.contact_back_button);
		mDeviceContactChangeButton = (Button) findViewById(R.id.contact_change_button);
		
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
		
		mContactScrollView = (ScrollView)findViewById(R.id.contact_scrollView);
		mContactListView = (ContactListView)findViewById(R.id.contact_list);
		
		bt_search.setOnClickListener(this);
		mDeviceIpButton.setOnClickListener(this);
		mDeviceLangButton.setOnClickListener(this);
		mDeviceTimeButton.setOnClickListener(this);
		mDeviceAudioButton.setOnClickListener(this);
		mDeviceContactButton.setOnClickListener(this);
		mDeviceQuitButton.setOnClickListener(this);
		mDeviceContactBackButton.setOnClickListener(this);
		mDeviceContactChangeButton.setOnClickListener(this);
		
		updateUiToConnectStat(UI_SEARCH_START);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.bt_search) {
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
		} else { 
			if(mDeviceIp == null)
				return;
			
			boolean isNoNeedToSendCommand = false;
			byte[] dataType = new byte[1];
			String[] dataContent = new String[1];
			switch (v.getId()) {
			case R.id.DeviceIPChangeButton:
				if(checkIpValue()) {
					dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_ETIP;
					dataContent[0] = mDeviceIpEditText.getText().toString();
				} else {
					isNoNeedToSendCommand = true;
				}
				break;
				
			case R.id.DeviceTimeChangeButton:
				if(checkTimeValue()) {
					dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_TIME;
					dataContent[0] = mDeviceTimeEditText.getText().toString();
				} else {
					isNoNeedToSendCommand = true;
				}
				break;
				
			case R.id.DeviceLangButton:
				dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_LANG;
				dataContent[0] = currentLangauge;
				break;
				
			case R.id.DeviceAudioChangeButton:
				if(checkAudioVaule()) {
					dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_AUDI;
					dataContent[0] = currentAudioType + ":" + mDeviceAudioEditText.getText().toString();
				} else {
					isNoNeedToSendCommand = true;
				}
				
				break;
				
			case R.id.DeviceContact:
				updateUiToConnectStat(UI_CONTACT_SHOW);
				isNoNeedToSendCommand = true;
				break;
				
			case R.id.button_quit:
				dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_QUIT;
				dataContent[0] = "quit";
				break;
				
			case R.id.contact_back_button:
				updateUiToConnectStat(UI_SEARCH_SUCC);
				isNoNeedToSendCommand = true;
				break;
				
			case R.id.contact_change_button:
				dataType[0] = DataPackHost.PACKET_DATA_TYPE_DEVICE_CONT;
				StringBuffer sb = new StringBuffer();
				if(mContactDataAdapter != null) {
					List<HashMap<String, String>> contact = mContactDataAdapter.getChangeNumber();
					
					if(contact != null) {
						for(int i = 0; i < contact.size(); ++i) {
							String contacId = (String)contact.get(i).get("contactId");
							String contactName = (String)contact.get(i).get("contactName");
							String contactPhone  = (String)contact.get(i).get("contactNumber");
							
							sb.append(contacId);
							sb.append(":");
							sb.append(contactName);
							sb.append(":");
							sb.append(contactPhone);
							sb.append("+");
						}
						
						if(sb.length() > 1)
							sb.deleteCharAt(sb.length() - 1);
					}
				}
				
				if(sb.length() > 1) {
					dataContent[0] = sb.toString();
				} else {
					isNoNeedToSendCommand = true;
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_no_change), Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				isNoNeedToSendCommand = true;
				break;
			}
			
			if(!isNoNeedToSendCommand) {
				new HostSendDataThread(mHandler, mDeviceIp, dataType, dataContent)
							.start();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		byte[] dataType = new byte[] {DataPackHost.PACKET_DATA_TYPE_DEVICE_QUIT};
		String[] dataContent = new String[] {"quit"};
		
		new HostSendDataThread(mHandler, mDeviceIp, dataType, dataContent)
		.start();
		
		//super.onBackPressed();
	}

	private void searchDevices_broadcast(final Handler handler, final String password) {
		new DeviceSearcher(handler, password) {
			@Override
			public void onSearchStart() {
				startSearch(); 
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
			case DataPackHost.PACKET_DATA_TYPE_DEVICE_QUIT:
				Log.e(TAG, "----------------> begin to quit");
				finish();
				break;

			case DataPackHost.DEVICE_FIND:
				if(msg.arg1 == DataPackHost.DEVICE_CONNECTED) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_right), Toast.LENGTH_SHORT).show();
					mDeviceIp = (String)msg.obj;

					new Timer().schedule(new TimerTask() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							byte[] dataType = new byte[]{DataPackHost.PACKET_DATA_TYPE_DEVICE_ALL};
							String[] dataContent = new String[]{"empty"};
							new HostSendDataThread(mHandler, mDeviceIp, dataType, dataContent).start();
						}
					}, 2 * 1000);
					
					new  HostRecvDataThread(mHandler, mDeviceIp).start();
					
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
					updateContactListView(result);
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_SETIING_RESULT:
					int resultId = -1;
					
					if(DataPackHost.PACKET_CHK_RESULT_OK.equals(result)) {
						resultId = R.string.device_change_result_ok;
					} else if (DataPackHost.PACKET_CHK_RESULT_BAD.equals(result)) {
						resultId = R.string.device_change_result_fail;
					} else {
						resultId = R.string.device_change_result_fail;
					}
					String resultMes = getResources().getString(resultId);
					
					Toast.makeText(getApplicationContext(), resultMes, Toast.LENGTH_SHORT).show();
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
	
	private boolean checkAudioVaule() {
		boolean result = false;
		if(mDeviceAudioEditText.getText().length() == 0) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_audio_vaule_is_empty), Toast.LENGTH_SHORT).show();
			return result;
		}
		
		int audioValue = Integer.valueOf(mDeviceAudioEditText.getText().toString());
		
		int maxValue = -1;
		int i = 0;
		
		for(i = 0; i < mAllAudioValue.size(); ++i) {
			if(currentAudioType != null) {
				if(currentAudioType.equals(mAllAudioValue.get(i).getAudioType())) {
					maxValue = mAllAudioValue.get(i).getAudioMaxValue();
					break;
				}
			}
		}
		
		if(audioValue <= maxValue && audioValue >= 0) {
			result = true;
			if(mAllAudioValue != null) {
				mAllAudioValue.get(i).setAudioVaule(audioValue);
			}
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_audio_vaule_is_out_of_ranger, maxValue), Toast.LENGTH_SHORT).show();
		}
		
		return result;
	}
	
	private boolean checkIpValue() {
		String ipValue = mDeviceIpEditText.getText().toString();

		String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipValue);
		
		boolean result = matcher.matches();
		
		if(!result) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_ip_address_wrong), Toast.LENGTH_SHORT).show();
		}
		
		return result;
	}
	
	//2014-12-01-03-02-05
	private boolean checkTimeValue() {
		String timeValue = mDeviceTimeEditText.getText().toString();

		String time = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
			   time += "\\-([0-1]\\d{1}|2[0-3])\\-([0-5]\\d{1})\\-([0-5]\\d{1})";
		Pattern pattern = Pattern.compile(time);
		Matcher matcher = pattern.matcher(timeValue);
		
		boolean result = matcher.matches();
		
		if(!result) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.device_time_format_wrong), Toast.LENGTH_SHORT).show();
		}
		
		return result;
	}
	
	private void updateUiToConnectStat(int uiType) {
		if(uiType == UI_SEARCH_START || uiType == UI_SEARCH_FAIL) {
			showSearchUI(true, true);
			showDeviceUI(false);
			showContactUI(false);
		} else if (uiType == UI_SEARCHING) {
			showSearchUI(true, false);
			showDeviceUI(false);
			showContactUI(false);
		} else if (uiType == UI_SEARCH_SUCC) {
			showSearchUI(false, false);
			showDeviceUI(true);
			showContactUI(false);
		} else if (uiType == UI_CONTACT_SHOW) {
			showSearchUI(false, false);
			showDeviceUI(false);
			showContactUI(true);
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
	
	private void showContactUI(boolean show){
		int visible = (show ? View.VISIBLE : View.GONE);
		
		mDeviceContactBackButton.setVisibility(visible);
		mDeviceContactChangeButton.setVisibility(visible);
		mDeviceContactTitle.setVisibility(visible);
		mContactScrollView.setVisibility(visible);
		mContactListView.setVisibility(visible);
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
		
		ArrayAdapter<String> adapter = null;
		
		mLangItems = lang.split(":");
		if(mLangItems != null) {
			currentLangauge = mLangItems[0];
			
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, mLangItems);
			
			if(adapter != null) {
				mDeviceLangSpinner.setAdapter(adapter);
				mDeviceLangSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub
						currentLangauge = mLangItems[position];
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						
					}
				
				});
			}
		}
	}
	
	private void updateDeviceAudio(String audio) {
		if(audio == null || audio.isEmpty())
			return;
		Log.e(TAG, "-----------------> updateDeviceAudio : " + audio);
		String[] audioArrayTemp = audio.split("\\+");
		ArrayAdapter<String> adapter = null;
		
		if(audioArrayTemp != null) {
			if(audioArrayTemp.length >= 2) {
				mAudioItems = new String[audioArrayTemp.length];
					
				for(int i = 0; i < audioArrayTemp.length; ++i) {
					String[] tempAudio = audioArrayTemp[i].split(":");
					if(tempAudio != null && tempAudio.length == 3) {
						mAudioItems[i] = tempAudio[0];
						
						//i == 0 is current audio value
						if(i == 0) {
							currentAudioType = tempAudio[0];
							currentAudioValue = Integer.valueOf(tempAudio[1]);
							currentMaxAudioValue = Integer.valueOf(tempAudio[2]);
						}
						
						AudioTypeBean atb = new AudioTypeBean(tempAudio[0], Integer.valueOf(tempAudio[1]), Integer.valueOf(tempAudio[2]));
						
						if(mAllAudioValue != null) {
							mAllAudioValue.add(atb);
						}
					}
				}
			}
			
			if(mAudioItems != null) {
				adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, mAudioItems);
				
				if(adapter != null) {
					mDeviceAudiSpinner.setAdapter(adapter);
					mDeviceAudiSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int pos, long arg3) {
							// TODO Auto-generated method stub
							currentAudioType = mAudioItems[pos];
							
							if(mAllAudioValue != null) {
								for(int i = 0; i < mAllAudioValue.size(); ++i) {
									if(currentAudioType.equals(mAllAudioValue.get(i).getAudioType())) {
										mDeviceAudioEditText.setText(String.valueOf(mAllAudioValue.get(i).getAudioVaule()));
									}
								}
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub
							
						}
					});
				}
			}
			
			mDeviceAudioEditText.setText(String.valueOf(currentAudioValue));
		}
	}
	
	private List<HashMap<String, String>> getContactListMap(String value){
		if(value == null || value.isEmpty())
			return null;
		
		String[] contactStrings = value.split("\\+");
		
		if(contactStrings != null) {
			for (String string : contactStrings) {
				String[] oneContactStrings = string.split(":");
				if(oneContactStrings == null || oneContactStrings.length != 3) {
					continue;
				} else {
					HashMap<String, String> temp = new HashMap<String, String>();
					temp.put("contactId", oneContactStrings[0]);
					temp.put("contactName", oneContactStrings[1]);
					temp.put("contactNumber", oneContactStrings[2]);
					
					allContactMap.add(temp);
				}
			}
		}
		
		return allContactMap;
	} 
	
	private void updateContactListView(String value) {
		mContactDataAdapter = new ContactDataAdapter(getApplicationContext(), getContactListMap(value));
		
		mContactListView.setAdapter(mContactDataAdapter);
		mContactListView.setListViewHeightBasedOnChildren();
	}
}
