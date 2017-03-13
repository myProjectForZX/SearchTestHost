package com.example.searchHost;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.R.dimen;
import android.os.Handler;
import android.system.OsConstants;

public class HostSendAndRecvDataThread extends Thread{
	private String mDeviceIp;
	private Handler mHandler;
	private Socket socket;
	
	private static final int PORT = 9001;
	
	public HostSendAndRecvDataThread(Handler handler, String ip) {
		mHandler = handler;
		mDeviceIp = ip;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
        try {
            socket = new Socket(mDeviceIp, PORT);
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
				sendMessage(socket, DataPackHost.PACKET_TYPE_SEND_RECV_DATA, 
						new byte[]{DataPackHost.PACKET_DATA_TYPE_DEVICE_ALL},
						new String[]{"123zcily"});
			}
		}
		
		if (socket.isConnected()) {
			if (!socket.isInputShutdown()) {
				receiveData(socket);
			}
		}
	}
	

    @SuppressWarnings("resource")
	public byte[] receiveData(Socket socket) {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(mDeviceIp, 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte[] data = null;
        if (socket != null && socket.isConnected()) {
        	if(!socket.isInputShutdown()) {
        		DataInputStream dIn = null;
				try {
					dIn = new DataInputStream(socket.getInputStream());
					int length = dIn.readInt();
					if(length > 0) {
						byte[] message = new byte[length];
						dIn.readFully(message, 0, message.length);
						
						DataPackHost.parseServiceSocktPackagt(message, mHandler);
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
        } else {
            data = new byte[1];
        }
        return data;
    }
    
    private void sendMessage(Socket socket, byte packType, byte[]dataType, String[]dataContent) {
    	if(socket == null)
    		return;
    	
    	DataOutputStream dOut = null;
    	byte[] data = DataPackHost.packData(packType, dataType, dataContent);
    	
		try {
			dOut = new DataOutputStream(socket.getOutputStream());
			if (dOut != null) {
				dOut.writeInt(data.length);
				dOut.write(data);
			}
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
