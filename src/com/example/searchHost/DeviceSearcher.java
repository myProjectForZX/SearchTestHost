package com.example.searchHost;

import android.os.Handler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
/**
 * 设备搜索类
 */
public abstract class DeviceSearcher extends Thread {
    private static final String TAG = "DeviceSearcher";
 
    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 10000; // 接收超时时间
    private static final int RESPONSE_DEVICE_MAX = 200; // 响应设备的最大个数，防止UDP广播攻击
 
 
    private DatagramSocket hostSocket;
    private Set<DeviceBean> mDeviceSet;
 
    private byte mPackType;
    private String mDeviceIP;
    private Handler mHandler;
 
    DeviceSearcher(Handler handler) {
        mDeviceSet = new HashSet<>();
        mHandler = handler;
    }
 
    @Override
    public void run() {
        try {
            onSearchStart();
            hostSocket = new DatagramSocket();
            // 设置接收超时时间
            hostSocket.setSoTimeout(RECEIVE_TIME_OUT);
 
            //send req broadcast.
            byte[] sendData = new byte[1024];
            InetAddress broadIP = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, DEVICE_FIND_PORT);
 
            sendPack.setData(DataPack.packData(DataPack.PACKET_TYPE_FIND_HOST_REQ, null, null));
            hostSocket.send(sendPack);
            //end send req.  wait for rsp
            Log.e(TAG, "-------------------> begin send data");
            // 监听来信
            byte[] receData = new byte[4096];
            DatagramPacket recePack = new DatagramPacket(receData, receData.length);
			try {
				recePack.setData(receData);
				hostSocket.receive(recePack);
				Log.e(TAG, "-------------------> rece send data");
				if (recePack.getLength() > 0) {
					mDeviceIP = recePack.getAddress().getHostAddress();
					Log.e(TAG, "-------------------> rece send data device ip : " + mDeviceIP);
					if (DataPack.parseDatagramPacket(recePack, DataPack.PACKET_TYPE_FIND_DEVICE_RSP, mHandler)) {
						Log.i(TAG, "the device ip : " + mDeviceIP);
						// 发送一对一的确认信息。使用接收报，因为接收报中有对方的实际IP，发送报时广播IP
						
						byte[] sendHostCHKPassword = new byte[] {DataPack.PACKET_DATA_TYPE_DEVICE_PASS};
						String[] password = new String[]{"123456"};
						recePack.setData(DataPack.packData(DataPack.PACKET_TYPE_FIND_HOST_CHK, sendHostCHKPassword, password)); // 注意：设置数据的同时，把recePack.getLength()也改变了
						hostSocket.send(recePack);
						
						Log.e(TAG, "------------------> before receive device chk");
						hostSocket.receive(recePack);
						Log.e(TAG, "------------------> after receive device chk");
						
						if(DataPack.parseDatagramPacket(recePack, DataPack.PACKET_TYPE_FIND_DEVICE_CHK, mHandler)) {
							Log.e(TAG, "------------------> password is right");
						} else {
							Log.e(TAG, "------------------> password is bad");
						}
					}
				}
			} catch (SocketTimeoutException e) {
			}
			Log.i(TAG, "------------------>: finis search");
			onSearchFinish(mDeviceSet);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (hostSocket != null) {
				hostSocket.close();
			}
		}
 
    }
 
    /**
     * 搜索开始时执行
     */
    public abstract void onSearchStart();
 
    /**
     * 搜索结束后执行
     * @param deviceSet 搜索到的设备集合
     */
    public abstract void onSearchFinish(Set deviceSet);
 
   
    /**
     * 设备Bean
     * 只要IP一样，则认为是同一个设备
     */
    public static class DeviceBean{
        String ip;      // IP地址
        int port;       // 端口
        String name;    // 设备名称
        String room;    // 设备所在房间
 
        @Override
        public int hashCode() {
            return ip.hashCode();
        }
 
        @Override
        public boolean equals(Object o) {
            if (o instanceof DeviceBean) {
                return this.ip.equals(((DeviceBean)o).getIp());
            }
            return super.equals(o);
        }
 
        public String getIp() {
            return ip;
        }
 
        public void setIp(String ip) {
            this.ip = ip;
        }
 
        public int getPort() {
            return port;
        }
 
        public void setPort(int port) {
            this.port = port;
        }
 
        public String getName() {
            return name;
        }
 
        public void setName(String name) {
            this.name = name;
        }
 
        public String getRoom() {
            return room;
        }
 
        public void setRoom(String room) {
            this.room = room;
        }
    }
}
