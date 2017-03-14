package com.example.searchHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * ��Ϊhost��apk�������ǵ����ʼ������������ͬһ�������ڵ������豸��Ϣ(ip��ַ�Ͷ˿���Ϣ)
 * ��������Ŀͻ����������ӵ�ϸ����û����ʵ����
 * ----------UDPͨ�Ŵ���ԭ��-------
 * ����device�˴���DatagramSocket��ָ���˿ڣ�DatagramPacket����DatagramSocket�е����ݣ�Ȼ��ȴ���������
 * ֮����host��ִ�п�ʼ������Ȼ���������㲥��ͨ��DatagramPacket.setData������������ģ�����Ϊ��������REQ_10��
 * Ȼ��hostSocket.send�������ݱ�������3��,
 * ֮����device���ܵ�����ִ��socket.send���ȴ���������ȷ�ϣ�ִ�е�socket.receive
 * Ȼ��host����ȷ�ϣ�Ȼ��host����ȷ��ok,device����ȷ�ϣ�У�飬���ȷ��ok��ִ��onDeviceSearched��ͨ����inetSocketAddress�ش����ݣ�Ȼ����������
 * -----------------------------
 * ���ṩ��demo���Լ����Ե�ʱ���������⣬���������ѵ�http://www.2cto.com/kf/201609/545161.html������£��ο����Եģ�����Ҳ�д�ŵ�ԭ�����
 *
 */
public class MainActivity extends ActionBarActivity implements OnClickListener, OnItemClickListener {

	private Button bt;
	private EditText passwordEdit;
	private ListView lvDevice;
	private ArrayList<DeviceBean> deviceList;
	private MyHandler mHandler = new MyHandler();
	
	public static final int SEARCH_START = 100;
	public static final int SEARCH_END = 101;

	StringBuffer sb = new StringBuffer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bt = (Button)findViewById(R.id.bt_search);
		passwordEdit = (EditText)findViewById(R.id.passwordEdit);
		
		lvDevice=(ListView)findViewById(R.id.lv_device_list);
		lvDevice.setOnItemClickListener(this);
		bt.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_search:
			Log.i("TAG", "click-host-searchDevices_broadcast()");
			if(passwordEdit.getText().length() == 0) {
				Toast.makeText(this, getApplicationContext().getResources().getString(R.string.please_entry_password), Toast.LENGTH_SHORT).show();
			} else {
				searchDevices_broadcast(mHandler, passwordEdit.getText().toString());
			}
			break;
		}
	}
	

	
	private void searchDevices_broadcast(final Handler handler, final String password) {
		new DeviceSearcher(handler, password) {
			@Override
			public void onSearchStart() {
				startSearch(); // ��Ҫ������UI��չʾ��������
			}

			@Override
			public void onSearchFinish(Set deviceSet) {
				Log.i("TAG", "onSearchFinish-deviceSet="+deviceSet.size());
			}

		}.start();
	}

	private void startSearch() {
		mHandler.sendEmptyMessage(SEARCH_START);
	}
	
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case SEARCH_START:
				if(bt != null)
					bt.setEnabled(false);
				break;
			
			case SEARCH_END:
				if(bt != null)
					bt.setEnabled(true);
				break;
				
			case DataPackHost.DEVICE_FIND:
				if(msg.arg1 == DataPackHost.DEVICE_CONNECTED) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_right), Toast.LENGTH_SHORT).show();
					final String deviceIp = (String)msg.obj;
					
					new Timer().schedule(new TimerTask() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							//���ӽ���֮��������ȫ�����ݵ�����
							byte[] dataType = new byte[]{DataPackHost.PACKET_DATA_TYPE_DEVICE_ALL};
							String[] dataContent = new String[]{"empty"};
							new HostSendDataThread(mHandler, deviceIp, dataType, dataContent).start();
						}
					}, 2 * 1000);
					
					new  HostRecvDataThread(mHandler, deviceIp).start();
				} else if (msg.arg1 == DataPackHost.DEVICE_NOT_CONNECTED) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_bad), Toast.LENGTH_SHORT).show();
				}
				break;
			case DataPackHost.PACKET_TYPE_SEND_RECV_DATA:
				String mesString  = msg.what + "   " + msg.obj;
				Toast.makeText(getApplicationContext(), mesString, Toast.LENGTH_LONG).show();
				switch (msg.arg1) {
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_NAME:
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_TIME:
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_LANG:
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_ETIP:
					break;
				case DataPackHost.PACKET_DATA_TYPE_DEVICE_AUDI:
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
}
