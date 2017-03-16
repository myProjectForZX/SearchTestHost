package com.example.searchHost;

import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts.Data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
/**
 * 设备搜索
 */
public abstract class DeviceSearcher extends Thread {
    private static final String TAG = "DeviceSearcher";
 
    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 10000; // 接收超时时间
 
    private DatagramSocket hostSocket;
    private Set<DeviceBean> mDeviceSet;
 
    private byte mPackType;
    private String mDeviceIP;
    private Handler mHandler;
    private String  mPassword;
 
    DeviceSearcher(Handler handler, String password) {
        mDeviceSet = new HashSet<>();
        mHandler = handler;
        mPassword = password;
    }
 
    @Override
    public void run() {
        try {
            onSearchStart();
            hostSocket = new DatagramSocket();
            hostSocket.setSoTimeout(RECEIVE_TIME_OUT);
 
            //send req broadcast.
            byte[] sendData = new byte[1024];
            InetAddress broadIP = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, DEVICE_FIND_PORT);
 
            sendPack.setData(DataPackHost.packData(DataPackHost.PACKET_TYPE_FIND_HOST_REQ, null, null));
            hostSocket.send(sendPack);
            //end send req.  wait for rsp
            Log.e(TAG, "-------------------> begin send data");
            
            byte[] receData = new byte[4096];
            DatagramPacket recePack = new DatagramPacket(receData, receData.length);

			recePack.setData(receData);
			hostSocket.receive(recePack);
			Log.e(TAG, "-------------------> rece send data");
			if (recePack.getLength() > 0) {
				mDeviceIP = recePack.getAddress().getHostAddress();
				Log.e(TAG, "-------------------> rece send data device ip : " + mDeviceIP);
				if (DataPackHost.parseDatagramPacket(recePack, DataPackHost.PACKET_TYPE_FIND_DEVICE_RSP, mHandler)) {
					Log.i(TAG, "the device ip : " + mDeviceIP);
						
					byte[] sendHostCHKPassword = new byte[] {DataPackHost.PACKET_DATA_TYPE_DEVICE_PASS};
					String[] password = new String[]{mPassword};
					recePack.setData(DataPackHost.packData(DataPackHost.PACKET_TYPE_FIND_HOST_CHK, sendHostCHKPassword, password)); // ע�⣺�������ݵ�ͬʱ����recePack.getLength()Ҳ�ı���
					hostSocket.send(recePack);
						
					Log.e(TAG, "------------------> before receive device chk");
					hostSocket.receive(recePack);
					Log.e(TAG, "------------------> after receive device chk");
						
					Message msg = new Message();
					msg.what = DataPackHost.DEVICE_FIND;
						
					if(DataPackHost.parseDatagramPacket(recePack, DataPackHost.PACKET_TYPE_FIND_DEVICE_CHK, mHandler)) {
						Log.e(TAG, "------------------> password is right");
						msg.arg1 = DataPackHost.DEVICE_CONNECTED;
						msg.obj = (String)mDeviceIP;
					} else {
						Log.e(TAG, "------------------> password is bad");
						msg.arg1 = DataPackHost.DEVICE_NOT_CONNECTED;
					}
					if(mHandler != null) {
						mHandler.sendMessage(msg);
					}
				}
			}
			Log.i(TAG, "------------------>: finis search");
			onSearchFinish(mDeviceSet);
		} catch (IOException e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = 101;//UI_SEARCH_FAILED
			if(mHandler != null) {
				mHandler.sendMessage(msg);
			}
		} finally {
			if (hostSocket != null) {
				hostSocket.close();
			}
		}
 
    }
 
    /**
     * 开始搜索
     */
    public abstract void onSearchStart();
 
    /**
     * 搜索到设备
     */
    public abstract void onSearchFinish(Set deviceSet);
}
