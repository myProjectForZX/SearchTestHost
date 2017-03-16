package com.example.searchHost;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;

public class HostSendDataThread extends Thread{
	private final String TAG = "HostSendAndRecvDataThread";
	
	private String mDeviceIp;
	private Handler mHandler;
	private Socket socket;
	private DataOutputStream dataOutputStream;
	
	private static final int PORT = 9001;
	private byte[] mDataType;
	private String[] mDataContent;
	
	public HostSendDataThread(Handler handler, String ip, byte[] dataType, String[] dataContent) {
		mHandler = handler;
		mDeviceIp = ip;
		
		if(dataType == null || dataContent == null)
			return;
		
		mDataType = dataType.clone();
		mDataContent = dataContent.clone();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
        try {
            socket = new Socket(mDeviceIp, PORT);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		if(socket == null)
			return;
		
		if(socket.isConnected()) {
			if(!socket.isOutputShutdown()) {
				sendMessage(dataOutputStream, DataPackHost.PACKET_TYPE_SEND_RECV_DATA, 
						mDataType,
						mDataContent);
			}
		}
	}

    
    private void sendMessage(DataOutputStream dOut, byte packType, byte[]dataType, String[]dataContent) {
    	if(dOut == null)
    		return;
    	
    	byte[] data = DataPackHost.packData(packType, dataType, dataContent);
    	
		try {
			dOut.writeInt(data.length);
			dOut.write(data);
			dOut.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (dOut != null)
					dOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
