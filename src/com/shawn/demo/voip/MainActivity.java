package com.shawn.demo.voip;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import com.shawn.demo.voip.R;

import jay.media.MediaService;

import android.app.Activity;
import android.content.Context;
//import android.net.rtp;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	public final String TAG = "IP_chat_client";

	public static final int MENU_START_ID = Menu.FIRST;
	public static final int MENU_STOP_ID = Menu.FIRST + 1;
	public static final int MENU_EXIT_ID = Menu.FIRST + 2;

	private boolean isStarted = false;
	// private AudioReader AudioReader;

	// private Socket socket;

	private EditText sendIP, sendport, recvPort,et_ec_buffer_size;
	private Button startButton, stopButton, exitButton;
	private TextView infoTV;

	private MediaService mMediaService;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 保持屏幕常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 阻止文本输入框获取焦点
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		String localIp = null;
		try {
			localIp = getLocalIPv4Address();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.setTitle(this.getApplicationInfo().loadLabel(this.getPackageManager()).toString()/* 获取应用名称 */+ ": "
				+ localIp);

		sendIP = (EditText) findViewById(R.id.et_Send_ip);
		sendport = (EditText) findViewById(R.id.et_send_port);
		recvPort = (EditText) findViewById(R.id.et_recv_port);
		et_ec_buffer_size = (EditText)findViewById(R.id.et_ec_buffer);

		infoTV = (TextView) findViewById(R.id.tv_info);

		startButton = (Button) findViewById(R.id.btn_connect);
		stopButton = (Button) findViewById(R.id.btn_stop);
		stopButton.setClickable(false);
		stopButton.setEnabled(false);
		exitButton = (Button) findViewById(R.id.btn_exit);

		mMediaService = new MediaService();
		
		TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(TELEPHONY_SERVICE);
		infoTV.setText("Wifi Mac = " + getLocalMacAddress(MainActivity.this) + "\ngetMac = " + getMac()
				+ "\nDevice ID = " + tm.getDeviceId());

		/* Start Chat */
		startButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String ipAddr = sendIP.getText().toString();
				int remotePort = Integer.parseInt(sendport.getText().toString());
				int localport = Integer.parseInt(recvPort.getText().toString());
				Log.i(TAG, "Dst IP is " + ipAddr + " Wifi Mac:" + getLocalMacAddress(MainActivity.this));
				Log.i(TAG, "getMac() >>> " + getMac());
				
				if (isStarted == true) {
					Toast.makeText(MainActivity.this, "Already connected", Toast.LENGTH_SHORT).show();
					return;
				}
				if (isIpAddr(ipAddr)) {
					int ec_buffer_pkgs = Integer.parseInt(et_ec_buffer_size.getText().toString());
					mMediaService.startAudio(ipAddr, 101, 8000, remotePort, localport,ec_buffer_pkgs);
					isStarted = true;
					startButton.setEnabled(false);
					stopButton.setEnabled(true);

					/* 101:Speex 8:G711a 0:G711u 9:G722 */

					// audioWriter = new AudioWriter();
					// audioWriter.init(ipAddr, 4321);
					// audioWriter.start();
				} else {
					Log.e(TAG, ipAddr + "ip illegal!!");
					Toast.makeText(MainActivity.this, "ip illegal!! @" + ipAddr + " | try again!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		/* Stop chat */
		stopButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (isStarted == true) {
					mMediaService.stopAudio();
					isStarted = false;
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
				}

				// if (audioWriter != null) {
				// audioWriter.free();
				// // AudioReader.free();
				// audioWriter = null;
				// // AudioReader = null;
				// } else {
				// Toast.makeText(MainActivity.this,
				// "Chat service not running,please click the \"Start\" button",
				// Toast.LENGTH_SHORT).show();
				// }
			}
		});

		/* Eixt */
		exitButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int pid = android.os.Process.myPid();
				android.os.Process.killProcess(pid);
			}
		});
	}

	/* Check the string if it is an legal ip address */
	private boolean isIpAddr(String str)
	{
		Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
				+ "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 获取mac地址
	 * 
	 * @param context
	 * @return
	 */
	public String getLocalMacAddress(Context context)
	{
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	private String getLocalIPv4Address() throws SocketException
	{
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
					return inetAddress.getHostAddress().toString();
				}
			}
		}
		return "null";
	}

	// private String getLocalIPv6Address() throws SocketException
	// {
	// for (Enumeration<NetworkInterface> en =
	// NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	// NetworkInterface intf = en.nextElement();
	// for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
	// enumIpAddr.hasMoreElements();) {
	// InetAddress inetAddress = enumIpAddr.nextElement();
	// if (!inetAddress.isLoopbackAddress()) {
	// return inetAddress.getHostAddress().toString();
	// }
	// }
	// }
	// return "null";
	// }

	public boolean onOptionsItemSelected(MenuItem aMenuItem)
	{
		// switch (aMenuItem.getItemId()) {
		// case MENU_START_ID: {
		// audioWriter.init(IP, 4321);
		//
		// }break;
		// case MENU_STOP_ID: {
		// audioWriter.free();
		// // AudioReader.free();
		//
		// audioWriter = null;
		// // AudioReader = null;
		// }
		// break;
		// case MENU_EXIT_ID: {
		// int pid = android.os.Process.myPid();
		// android.os.Process.killProcess(pid);
		// }
		// break;
		// default:
		// break;
		// }
		//
		return super.onOptionsItemSelected(aMenuItem);

	}

	public boolean onCreateOptionsMenu(Menu aMenu)
	{
		boolean res = super.onCreateOptionsMenu(aMenu);

		aMenu.add(0, MENU_START_ID, 0, "START");
		aMenu.add(0, MENU_STOP_ID, 0, "STOP");
		aMenu.add(0, MENU_EXIT_ID, 0, "EXIT");

		return res;
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		// audioWriter.free();
		super.onDestroy();

	}

	// class ConnectThread extends Thread
	// {
	// public void run()
	// {
	// try {
	// // if (socket == null) {
	// // socket = new Socket("192.168.2.189", 4321);
	// audioWriter = new AudioWriter();
	// // AudioReader = new AudioReader();
	//
	// audioWriter.init("IP Addr",4321);
	// // AudioReader.init(socket);
	//
	// audioWriter.start();
	// // AudioReader.start();
	// // }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// }

	String getMac()
	{
		String macSerial = null;
		String str = "";
		try {
			Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);

			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macSerial = str.trim();// 去空格
					break;
				}
			}
		} catch (IOException ex) {
			// 赋予默认值
			ex.printStackTrace();
		}
		return macSerial;
	}

}