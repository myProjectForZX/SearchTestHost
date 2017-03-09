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
				bt.setText("��ʼ����");
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
				startSearch(); // ��Ҫ������UI��չʾ��������
			}

			@Override
			public void onSearchFinish(Set deviceSet) {
				Log.i("TAG", "onSearchFinish-deviceSet="+deviceSet.size());
				if(mDeviceList!=null){
					mDeviceList.clear();
					mDeviceList.addAll(deviceSet);
					mHandler.sendEmptyMessage(SEARCH_END); // ��UI�ϸ����豸�б�
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
