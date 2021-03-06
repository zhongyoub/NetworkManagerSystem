package com.networkmanagersystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import com.example.networkmanagersystem.R;
import com.networkmanagersystem.ClientRSTCP.recieveFile;
import com.networkmanagersystem.ClientRSTCP.sendFile;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class ClientReUDP extends Activity {
	private String SERVER_ADDRESS;
	private Handler handler = new Handler();
	public ListView msgView;
	public ArrayAdapter<String> msgList;
	
	public static Runnable runnable;
	public static Thread thread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_re_udp);
		
		msgView = (ListView) findViewById(R.id.listViewUDP);

		msgList = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		msgView.setAdapter(msgList);
		
		ImageButton back = (ImageButton) findViewById(R.id.UDPBack);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ClientReUDP.this, MainActivity.class);
	            startActivity(intent);      
	            finish();
			}
		});
		//get address server from users
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			SERVER_ADDRESS = extras.getString("AddressServer");
		}	
		
		runnable = new sendSignal();
		thread = new Thread(runnable);
		thread.start();
		
		try {
			thread.join();
			runnable = new recievePacket();
			thread = new Thread(runnable);
			thread.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public class sendSignal implements Runnable {
		private final static int SERVER_PORT = 10001;
		public void run() {
			try {
				// Preparing the socket
				InetAddress serverAddr = InetAddress.getByName(SERVER_ADDRESS);
				DatagramSocket socket = new DatagramSocket();

				// Preparing the packet
				byte[] buf = ("Hello computer").getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						serverAddr, SERVER_PORT);

				// Sending the packet
				Log.d("UDP", String.format("Sending: '%s' to %s:%s",
						new String(buf), SERVER_ADDRESS, SERVER_PORT));
				socket.send(packet);
				Log.d("UDP", "Packet sent.");
			} catch (Exception e) {
				Log.e("UDP", "Client error", e);
			}
		}

	};

	public class recievePacket implements Runnable {
		private final static int CLIENT_PORT_ToReceiveUDP = 10001;
		int totalPacket = 0;
		public void run() {

			try {
				// Opening listening socket
				Log.d("UDP Receiver", "Opening listening socket on port "
						+ CLIENT_PORT_ToReceiveUDP + "...");
				DatagramSocket socket = new DatagramSocket(CLIENT_PORT_ToReceiveUDP);
				//socket.setBroadcast(true);
				socket.setReuseAddress(true);
				socket.setSoTimeout(1000);
				
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				for (int i = 0; i <= 100000; i++) {
					// Listening on socket
					// Log.d("UDP Receiver", "Listening...");
					socket.receive(packet);
					totalPacket++;	
					if (totalPacket%1000 ==0 )
						displayMsg("Receiving: " + totalPacket + " packets...");
				}
				socket.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				displayMsg("Finishing and Receiving: " + totalPacket + " packets...");
				displayMsg("Total packet was lost is:" + (10000 - totalPacket) + "/10000 packets was sended! ");
				//displayMsg("-------Finish testing..........");
				Log.e("UDP", "Receiver error", e);
			}
		}
	}

	
	public void displayMsg(String msg) {
		final String mssg = msg;
		handler.post(new Runnable() {
			@Override
			public void run() {
				msgList.add(mssg);
				msgView.setAdapter(msgList);
				msgView.smoothScrollToPosition(msgList.getCount() - 1);
			}
		});

	}

}
