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
 * 此为host的apk，作用是点击开始搜索，便搜索同一局域网内的所有设备信息(ip地址和端口信息)
 * 具体的它的客户需求那样子的细节我没具体实现了
 * ----------UDP通信大致原理-------
 * 先是device端创建DatagramSocket并指定端口，DatagramPacket接收DatagramSocket中的数据，然后等待被搜索；
 * 之后是host端执行开始搜索，然后发送搜索广播，通过DatagramPacket.setData，打包搜索报文，类型为搜索请求REQ_10，
 * 然后hostSocket.send发送数据报，发送3次,
 * 之后是device接受到请求，执行socket.send，等待主机接受确认，执行到socket.receive
 * 然后host接收确认，然后host发送确认ok,device接收确认，校验，如果确认ok则执行onDeviceSearched，通过传inetSocketAddress回传数据，然后后搜索完成
 * -----------------------------
 * 他提供的demo我自己调试的时候发现有问题，后面网上搜的http://www.2cto.com/kf/201609/545161.html这个文章，参考调试的，里面也有大概的原理介绍
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
				startSearch(); // 主要用于在UI上展示正在搜索
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
							//连接建立之后发送请求全部数据的请求
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
