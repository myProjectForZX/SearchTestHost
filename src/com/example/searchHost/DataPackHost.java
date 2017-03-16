package com.example.searchHost;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

public class DataPackHost {
	private static final String TAG = "DataPack";
	public static final byte DATA_HEAD = 0x51;
	
    public static final byte PACKET_TYPE_FIND_HOST_REQ = 0x10; 
    public static final byte PACKET_TYPE_FIND_DEVICE_RSP = 0x11; 
    public static final byte PACKET_TYPE_FIND_HOST_CHK = 0x12; 
    public static final byte PACKET_TYPE_FIND_DEVICE_CHK = 0x13; 
    public static final byte PACKET_TYPE_FIND_HOST_CHK_2 = 0x14; 
    
    public static final byte PACKET_TYPE_SEND_RECV_DATA  = 0x15; 
 
    public static final byte PACKET_DATA_TYPE_DEVICE_PASS = 0x20;
    public static final byte PACKET_DATA_TYPE_DEVICE_RESULT = 0x21;
    public static final byte PACKET_DATA_TYPE_DEVICE_NAME = 0x22;
    public static final byte PACKET_DATA_TYPE_DEVICE_TIME = 0x23;
    public static final byte PACKET_DATA_TYPE_DEVICE_LANG = 0x24;
    public static final byte PACKET_DATA_TYPE_DEVICE_ETIP = 0x25;
    public static final byte PACKET_DATA_TYPE_DEVICE_AUDI = 0x26;
    public static final byte PACKET_DATA_TYPE_DEVICE_CONT = 0x27;
    public static final byte PACKET_DATA_TYPE_DEVICE_ALL = 0x28;
    public static final byte PACKET_DATA_TYPE_DEVICE_SETIING_RESULT  = 0x29;
    public static final byte PACKET_DATA_TYPE_DEVICE_QUIT  = 0x30;
    
    public static final String PACKET_CHK_RESULT_OK = "OK";
    public static final String PACKET_CHK_RESULT_BAD = "FAIL";
    
    public static final int DEVICE_FIND = 0;
    public static final int DEVICE_CONNECTED = 1;
    public static final int DEVICE_NOT_CONNECTED = 2;
    
	/**
     * 数据组成
     * 数据头DATA_HEAD + packType(1) + data(n)
     *  data: dataType(1) + length(4) + data(length)
     *  dataType 和 dataContent要一一对应
     */
    public static byte[] packData(byte packType, byte[] dataType, String[] dataContent) {
        byte[] data = new byte[4096];
        int offset = 0;
        data[offset++] = DATA_HEAD;
        data[offset++] = packType;
 
        if(dataType != null && dataContent != null) {
        	if(dataType.length != dataContent.length) {
        		Log.i(TAG, "datatype mismatch with datacontent");
        		return new byte[0];
        	}
        
        	for (int i = 0; i < dataType.length;  ++i) {
        		byte[] temp = getBytesFromType(dataType[i], dataContent[i]);
        		System.arraycopy(temp, 0, data, offset, temp.length);
        		offset += temp.length;
        	}
        }

        byte[] retVal = new byte[offset];
        System.arraycopy(data, 0, retVal, 0, offset);
 
        return retVal;
    }
    
    /**
     * 数据组成
     * 数据头DATA_HEAD + packType(1) + data(n)
     *  data: dataType(1) + length(4) + data(length)
     */
    private static boolean parsePack(byte data[], int dataOffSet, byte needCheckPackType, Handler handler) {
    	boolean result = false;
    	Log.e(TAG, "-------------------> parsePack begin");
        if (data == null) {
        	Log.e(TAG, "----------------> parsePack : data = null");
            return result;
        }

        int dataLen = data.length;
        int offset = 0;
        byte packType = 0x00;
        byte dataType;
        int len;
 
        if (dataLen < 2) {
        	Log.e(TAG, "----------------> parsePack : datalen < 2");
            return result;
        }

        System.arraycopy(data, dataOffSet, data, 0, dataLen);
 
        //to check packType is right.
        
        if (data[offset++] != DATA_HEAD || (packType = data[offset++]) != needCheckPackType) {
        	Log.e(TAG, "----------------> datatyep mismatch");
            return result;
        }
        
        Log.e(TAG, "---------------------->  packType : " + packType);
		switch (packType) {
		case PACKET_TYPE_FIND_HOST_REQ:
			// host
			result = true;
			break;
		case PACKET_TYPE_FIND_DEVICE_RSP:
			// device
			// send rsp package PACKET_TYPE_FIND_DEVICE_RSP
			result = true;
			break;
		case PACKET_TYPE_FIND_HOST_CHK:
			// host
			// need send password to device. then send PACKET_TYPE_FIND_HOST_CHK
		case PACKET_TYPE_FIND_DEVICE_CHK:
			// device
			// check password if true send PACKET_TYPE_FIND_DEVICE_CHK
			// else close
		case PACKET_TYPE_SEND_RECV_DATA:
			// host update UI
			// to get Data.
			while (offset + 5 < dataLen) {
				dataType = data[offset++];
				len = data[offset++] & 0xFF;
				len |= (data[offset++] << 8);
				len |= (data[offset++] << 16);
				len |= (data[offset++] << 24);

				if (offset + len > dataLen) {
					break;
				}
				
				String resultString = new String(data, offset, len, Charset.forName("UTF-8"));
				Log.e(TAG, "---------------------> dataType : " + dataType + "   resultString : " + resultString);
				
				if(dataType == PACKET_DATA_TYPE_DEVICE_RESULT) {
					if(DataPackHost.PACKET_CHK_RESULT_OK.equals(resultString)) {
						result = true;
					} else {
						result = false;
					}
				} else {
					boolean isDefault = false;
					Message msg = new Message();
					msg.what = DataPackHost.PACKET_TYPE_SEND_RECV_DATA;
					msg.arg1 = dataType;
					msg.obj  = resultString;
					switch (dataType) {
					case PACKET_DATA_TYPE_DEVICE_NAME:
						break;
					case PACKET_DATA_TYPE_DEVICE_TIME:
						break;
					case PACKET_DATA_TYPE_DEVICE_LANG:
						break;
					case PACKET_DATA_TYPE_DEVICE_ETIP:
						break;
					case PACKET_DATA_TYPE_DEVICE_AUDI:
						break;
					case PACKET_DATA_TYPE_DEVICE_CONT:
						break;
					case PACKET_DATA_TYPE_DEVICE_SETIING_RESULT:
						break;
					case PACKET_DATA_TYPE_DEVICE_QUIT:
						break;
					default:
						isDefault = true;
						break;
					}
					if(handler != null && !isDefault)
						handler.sendMessage(msg);
				}
				
				offset += len;
			}
			break;

		default:
			break;
		}
        return result;
    }
    
 
    private static byte[] getBytesFromType(byte type, String val) {
        byte[] retVal = new byte[0];
        if (val != null) {
            byte[] valBytes = val.getBytes(Charset.forName("UTF-8"));
            retVal = new byte[5 + valBytes.length];
            retVal[0] = type;
            retVal[1] = (byte) valBytes.length;
            retVal[2] = (byte) (valBytes.length >> 8 );
            retVal[3] = (byte) (valBytes.length >> 16);
            retVal[4] = (byte) (valBytes.length >> 24);
            System.arraycopy(valBytes, 0, retVal, 5, valBytes.length);
        }
        return retVal;
    }
 
    //解析udp数据
    public static boolean parseDatagramPacket(DatagramPacket dataPacket, byte needCheckPackType, Handler handler) {
    	return parsePack(dataPacket.getData(), dataPacket.getOffset(), needCheckPackType, handler);
    }
    
  //解析TCP数据
    public static boolean parseServiceSocktPackagt(byte[] data, Handler handler) {
    	return parsePack(data, 0, PACKET_TYPE_SEND_RECV_DATA, handler);
    }

	private static final byte[] input2byte(InputStream inStream) {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		try {
			while ((rc = inStream.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
}
