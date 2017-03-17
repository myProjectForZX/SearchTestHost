package com.example.searchHost;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;


public class HostRecvDataThread extends Thread{
	private final static String TAG = HostRecvDataThread.class.getSimpleName();
	private ServerSocket serverSocket;
	private final int HOST_SERVER_SOCKET_PORT = 9002;
	private Handler mHandler;
	private String  mDeviceIp;
	private boolean isRunning = true;
	
	public HostRecvDataThread(Handler handler, String ip) {
		mHandler = handler;
		mDeviceIp = ip;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			serverSocket = new ServerSocket(HOST_SERVER_SOCKET_PORT);
			while(isRunning) {
				serverSocket.setSoTimeout(DataPackHost.RECEIVE_TIME_OUT);
				Socket deviceSocket = serverSocket.accept();
				
				String remoteIp = deviceSocket.getInetAddress().getHostAddress();
				Log.i(TAG, "------------------->  remoteIp : " + remoteIp + "   deviceIp : " + mDeviceIp + "   isEqual : " + remoteIp.equals(mDeviceIp));
				
				//ip 不相等的不处理
				if(!remoteIp.equals(mDeviceIp)) {
					deviceSocket.close();
					continue;
				}
					
				if(true == receiveData(deviceSocket)) {
					isRunning = false;
				}
				
				deviceSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "---------------------> wrong : e " + e);
		} finally {
			Log.i(TAG, "---------------------> hostReceive end");
			try {
				if(serverSocket != null) {
					serverSocket.close();
				}
				serverSocket = null;
			} catch (Exception e2) {
				// TODO: handle exception
			}
			if(mHandler!= null) {
				mHandler.sendEmptyMessage(DataPackHost.PACKET_DATA_TYPE_DEVICE_QUIT);
			}
		}
	}
	
	
    @SuppressWarnings("resource")
	public boolean receiveData(Socket socket) {
        boolean result = false;
        DataInputStream dIn = null;
        if (socket != null && socket.isConnected()) {
        	if(!socket.isInputShutdown()) {
				try {
					Log.e(TAG, "--------------------> wait for input");
					dIn = new DataInputStream(socket.getInputStream());
					int length = dIn.readInt();
					Log.e(TAG, "--------------------> length : " + length);
					if(length > 0) {
						byte[] message = new byte[length];
						dIn.readFully(message, 0, message.length);
						Log.e(TAG, "--------------------> before parse --");
						result = DataPackHost.parseServiceSocktPackagt(message, mHandler);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if(dIn != null)
							dIn.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
        }
        return result;
    }
}
