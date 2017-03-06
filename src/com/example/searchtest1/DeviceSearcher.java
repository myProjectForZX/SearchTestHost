package com.example.searchtest1;
import android.util.Log;
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
 * Created by zjun on 2016/9/3.
 */
public abstract class DeviceSearcher extends Thread {
    private static final String TAG = "TAG";
 
    private static final int DEVICE_FIND_PORT = 9000;
    private static final int RECEIVE_TIME_OUT = 1500; // ���ճ�ʱʱ��
    private static final int RESPONSE_DEVICE_MAX = 200; // ��Ӧ�豸������������ֹUDP�㲥����
 
    private static final byte PACKET_TYPE_FIND_DEVICE_REQ_10 = 0x10; // ��������
    private static final byte PACKET_TYPE_FIND_DEVICE_RSP_11 = 0x11; // ������Ӧ
    private static final byte PACKET_TYPE_FIND_DEVICE_CHK_12 = 0x12; // ����ȷ��
 
    private static final byte PACKET_DATA_TYPE_DEVICE_NAME_20 = 0x20;
    private static final byte PACKET_DATA_TYPE_DEVICE_ROOM_21 = 0x21;
 
    private DatagramSocket hostSocket;
    private Set<DeviceBean> mDeviceSet;
 
    private byte mPackType;
    private String mDeviceIP;
 
    DeviceSearcher() {
        mDeviceSet = new HashSet<>();
    }
 
    @Override
    public void run() {
        try {
            onSearchStart();
            hostSocket = new DatagramSocket();
            // ���ý��ճ�ʱʱ��
            hostSocket.setSoTimeout(RECEIVE_TIME_OUT);
 
            byte[] sendData = new byte[1024];
            InetAddress broadIP = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, broadIP, DEVICE_FIND_PORT);
 
            for (int i = 0; i < 3; i++) {
                // ���������㲥
                mPackType = PACKET_TYPE_FIND_DEVICE_REQ_10;
                sendPack.setData(packData(i + 1));
                hostSocket.send(sendPack);
 
                // ��������
                byte[] receData = new byte[1024];
                DatagramPacket recePack = new DatagramPacket(receData, receData.length);
                try {
                    // ������200������ʱ����ѭ��
                    int rspCount = RESPONSE_DEVICE_MAX;
                    while (rspCount-- > 0) {
                        recePack.setData(receData);
                        hostSocket.receive(recePack);
                        if (recePack.getLength() > 0) {
                            mDeviceIP = recePack.getAddress().getHostAddress();
                            if (parsePack(recePack)) {
                                Log.i(TAG, "host-@@@zjun: �豸���ߣ�" + mDeviceIP);
                                // ����һ��һ��ȷ����Ϣ��ʹ�ý��ձ�����Ϊ���ձ����жԷ���ʵ��IP�����ͱ�ʱ�㲥IP
                                mPackType = PACKET_TYPE_FIND_DEVICE_CHK_12;
                                recePack.setData(packData(rspCount)); // ע�⣺�������ݵ�ͬʱ����recePack.getLength()Ҳ�ı���
                                hostSocket.send(recePack);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                }
                Log.i(TAG, "host-@@@zjun: ��������" + i);
            }
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
     * ��������
     * Э�飺$ + packType(1) + data(n)
     *  data: ��n�����ݣ�ÿ�����ɽṹtype(1) + length(4) + data(length)
     *  type�����а���name��room���ͣ���name��������ǰ��
     */
    private boolean parsePack(DatagramPacket pack) {
        if (pack == null || pack.getAddress() == null) {
            return false;
        }
 
        String ip = pack.getAddress().getHostAddress();
        int port = pack.getPort();
        for (DeviceBean d : mDeviceSet) {
            if (d.getIp().equals(ip)) {
                return false;
            }
        }
        int dataLen = pack.getLength();
        int offset = 0;
        byte packType;
        byte type;
        int len;
        DeviceBean device = null;
 
        if (dataLen < 2) {
            return false;
        }
        byte[] data = new byte[dataLen];
        System.arraycopy(pack.getData(), pack.getOffset(), data, 0, dataLen);
 
        if (data[offset++] != '$') {
            return false;
        }
 
        packType = data[offset++];
        if (packType != PACKET_TYPE_FIND_DEVICE_RSP_11) {
            return false;
        }
 
        while (offset + 5 < dataLen) {
            type = data[offset++];
            len = data[offset++] & 0xFF;
            len |= (data[offset++] << 8);
            len |= (data[offset++] << 16);
            len |= (data[offset++] << 24);
 
            if (offset + len > dataLen) {
                break;
            }
            switch (type) {
                case PACKET_DATA_TYPE_DEVICE_NAME_20:
                    String name = new String(data, offset, len, Charset.forName("UTF-8"));
                    device = new DeviceBean();
                    device.setName(name);
                    device.setIp(ip);
                    device.setPort(port);
                    break;
                case PACKET_DATA_TYPE_DEVICE_ROOM_21:
                    String room = new String(data, offset, len, Charset.forName("UTF-8"));
                    if (device != null) {
                        device.setRoom(room);
                    }
                    break;
                default: break;
            }
            offset += len;
        }
        if (device != null) {
            mDeviceSet.add(device);
            return true;
        }
        return false;
    }
 
    /**
     * �����������
     * Э�飺$ + packType(1) + sendSeq(4) + [deviceIP(n<=15)]
     *  packType - ��������
     *  sendSeq - ��������
     *  deviceIP - �豸IP����ȷ��ʱЯ��
     */
    private byte[] packData(int seq) {
        byte[] data = new byte[1024];
        int offset = 0;
 
        data[offset++] = '$';
 
        data[offset++] = mPackType;
 
        seq = seq == 3 ? 1 : ++seq; // can't use findSeq++
        data[offset++] = (byte) seq;
        data[offset++] = (byte) (seq >> 8 );
        data[offset++] = (byte) (seq >> 16);
        data[offset++] = (byte) (seq >> 24);
 
        if (mPackType == PACKET_TYPE_FIND_DEVICE_CHK_12) {
            byte[] ips = mDeviceIP.getBytes(Charset.forName("UTF-8"));
            System.arraycopy(ips, 0, data, offset, ips.length);
            offset += ips.length;
        }
 
        byte[] result = new byte[offset];
        System.arraycopy(data, 0, result, 0, offset);
        return result;
    }
 
 
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
