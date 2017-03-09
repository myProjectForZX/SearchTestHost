package com.example.searchHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
	private ListView lvDevice;
	private ArrayList<DeviceBean> deviceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt=(Button)findViewById(R.id.bt_search);
		lvDevice=(ListView)findViewById(R.id.lv_device_list);
		lvDevice.setOnItemClickListener(this);
		bt.setOnClickListener(this);
		deviceList=new ArrayList<DeviceBean>();
		if(mDeviceAdapter ==null){
			mDeviceAdapter=new DeviceAdapter(getApplicationContext(), deviceList);
		}else{
			mDeviceAdapter.notifyDataSetChanged();
		}
		lvDevice.setAdapter(mDeviceAdapter);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_search:
			Log.i("TAG", "click-host-searchDevices_broadcast()");
			searchDevices_broadcast();
			break;
		}
	}
	StringBuffer sb = new StringBuffer();
	public static final int SEARCH_START = 0;
	public static final int SEARCH_END = 1;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SEARCH_START:
				bt.setText("开始搜索");
				break;
			case SEARCH_END:
				for (DeviceSearcher.DeviceBean d : mDeviceList) {
					String show = d.getIp() + " : " + d.getPort();
					DeviceBean deviceBeab = new DeviceBean();
					deviceBeab.setIp(d.getIp());
					deviceBeab.setPort(d.getPort());
					deviceList.add(deviceBeab);
				}
				Log.i("TAG", "deviceList="+deviceList.toString());
				mDeviceAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	private List<DeviceSearcher.DeviceBean> mDeviceList = new ArrayList<DeviceSearcher.DeviceBean>();
	private DeviceAdapter mDeviceAdapter;
	private void searchDevices_broadcast() {
		new DeviceSearcher(null) {
			@Override
			public void onSearchStart() {
				startSearch(); // 主要用于在UI上展示正在搜索
			}

			@Override
			public void onSearchFinish(Set deviceSet) {
				Log.i("TAG", "onSearchFinish-deviceSet="+deviceSet.size());
				if(mDeviceList!=null){
					mDeviceList.clear();
					mDeviceList.addAll(deviceSet);
					mHandler.sendEmptyMessage(SEARCH_END); // 在UI上更新设备列表
				}
			}

		}.start();
	}

	private void startSearch() {
		mHandler.sendEmptyMessage(SEARCH_START);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
}
