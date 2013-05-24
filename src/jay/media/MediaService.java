package jay.media;

import java.net.DatagramSocket;
import java.net.SocketException;

import jay.codec.EchoCancellation;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MediaService extends Service
{

	private final static String TAG = "MediaService";

	protected LanAudioPlay m_iPlay;
	protected LanAudioRecord m_iRecord;
	static public EchoCancellation m_ec=null;
	protected DatagramSocket udp_send_socket,udp_recv_socket;
	protected int mAudioRtpPort = 5071;

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class JayBinder extends Binder
	{
		MediaService getService()
		{
			return MediaService.this;
		}
	}

	private final IBinder mBinder = new JayBinder();

	public void startAudio(String Destaddr, int codecType, int SampleRate, int SendRtpPort,int RecvRtpPort,int ec_buffer_pkgs)
	{
		Log.d(TAG, "startAudio()");
		try {//  udp接收socket
			if (udp_recv_socket == null)
				udp_recv_socket = new DatagramSocket(RecvRtpPort/* 5071 */);//   udp recieve socket port
			Log.d(TAG, "new udp recvice socket port is " + udp_recv_socket.getPort());
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		try {
			if (udp_send_socket == null)
				udp_send_socket = new DatagramSocket(SendRtpPort);//  udp send socket port
			Log.d(TAG, "new udp send socket port is " + SendRtpPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		
		/*
		 *    回音消除,采用speex库的    
		 * echo cancellation
		 * public native int echoinit(int framesize,int filterlength);
		 * public native int echoplayback(short[] play);
		 * public native int echocapture(short[] rec ,short[] out);
		 * public native void echoclose();
		 */
		if (m_ec == null) {
			Log.e(TAG, "new echo cancellation");
			m_ec = new EchoCancellation(); 
			m_ec.startThread();
		} else {
			m_ec.stopThread();
			m_ec.startThread();
		}
		if (m_iRecord == null) {
			Log.d(TAG, "LanAudioRecord udp_send_socket port is " + SendRtpPort);
			m_iRecord = new LanAudioRecord(udp_send_socket, Destaddr, codecType, SendRtpPort, SampleRate,ec_buffer_pkgs);
			m_iRecord.startThread();
		} else {
			m_iRecord.stopThread();
			m_iRecord.setDestIP(Destaddr);
			m_iRecord.setDestPort(SendRtpPort);
			m_iRecord.startThread();
		}
		if (m_iPlay == null) {
			Log.d(TAG, "LanAudioPlay udp_socket port is " + udp_recv_socket.getPort());
			m_iPlay = new LanAudioPlay(udp_recv_socket, codecType, SampleRate,ec_buffer_pkgs);
			m_iPlay.startThread();
		} else {
			m_iPlay.stopThread();
			m_iPlay.startThread();
		}

	}

	public void stopAudio()
	{
		if (m_iRecord != null)
			m_iRecord.stopThread();
		if (m_iPlay != null)
			m_iPlay.stopThread();
		if (m_ec != null)
			m_ec.stopThread();
	}

	public void startVideo()
	{

	}

	public void stopVideo()
	{

	}

	public void test()
	{
		Log.d("Media service", "this is only a test");
	}

}