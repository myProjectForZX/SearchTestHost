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
 * �豸������
 */
public abstract class DeviceSearcher extends Thread {
    private static final String TAG = "DeviceSearcher";
 
    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 10000; // ���ճ�ʱʱ��
    private static final int RESPONSE_DEVICE_MAX = 200; // ��Ӧ�豸������������ֹUDP�㲥����
 
 
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
            // ���ý��ճ�ʱʱ��
            hostSocket.setSoTimeout(RECEIVE_TIME_OUT);
 
            //send req broadcast.
            byte[] sendData = new byte[1024];
            InetAddress broadIP = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, DEVICE_FIND_PORT);
 
            sendPack.setData(DataPack.packData(DataPack.PACKET_TYPE_FIND_HOST_REQ, null, null));
            hostSocket.send(sendPack);
            //end send req.  wait for rsp
            Log.e(TAG, "-------------------> begin send data");
            // ��������
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
						// ����һ��һ��ȷ����Ϣ��ʹ�ý��ձ�����Ϊ���ձ����жԷ���ʵ��IP�����ͱ�ʱ�㲥IP
						
						byte[] sendHostCHKPassword = new byte[] {DataPack.PACKET_DATA_TYPE_DEVICE_PASS};
						String[] password = new String[]{mPassword};
						recePack.setData(DataPack.packData(DataPack.PACKET_TYPE_FIND_HOST_CHK, sendHostCHKPassword, password)); // ע�⣺�������ݵ�ͬʱ����recePack.getLength()Ҳ�ı���
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
     * ������ʼʱִ��
     */
    public abstract void onSearchStart();
 
    /**
     * ����������ִ��
     * @param deviceSet ���������豸����
     */
    public abstract void onSearchFinish(Set deviceSet);
 
   
    /**
     * �豸Bean
     * ֻҪIPһ��������Ϊ��ͬһ���豸
     */
    public static class DeviceBean{
        String ip;      // IP��ַ
        int port;       // �˿�
        String name;    // �豸����
        String room;    // �豸���ڷ���
 
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
