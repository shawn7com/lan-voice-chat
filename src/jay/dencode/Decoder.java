package jay.dencode;

import java.util.LinkedList;

import android.util.Log;

import jay.codec.Codec;
import jay.codec.EchoCancellation;
import jay.media.MediaService;

public class Decoder implements Runnable
{
	private final String TAG = "Decoder";

	private volatile int leftSize = 0;
	private final Object mutex = new Object();
	private Codec codec;
	private int frameSize = 160;
	private long ts;
	private short[] processedData = new short[frameSize];
	private byte[] rawdata = new byte[frameSize * 2];
	protected LinkedList<short[]> m_out_q = new LinkedList<short[]>(); // store
																		// processed
																		// data
	private EchoCancellation m_ec;
	static public int num_recv = 0;
	
	/** 回音消除缓冲包个数 （sipUA默认20）*/
	protected int ec_buffer_pkgs = 20;
	
	protected int rtp_head = 12;
	private volatile Thread runner;

	public Decoder(int codeccode)
	{
		super();
		codec = new Codec(codeccode);
		codec.init();
		m_ec = MediaService.m_ec;
	}
	
	/**
	 * @param codeccode 
	 * 			编解码器选择
	 * 				0 - g711u
	 * 				8 - g711a
	 * 				9 - g722
	 * 	  	   	   101 - speex
	 * 
	 *@param ec_buffer_pkgs 
	 *			回音消除缓冲包个数 （默认20）延迟{@code ec_buffer_pkgs}个rtp包才 启动回音消除操作
	 * */
	public Decoder(int codeccode,int ec_buffer_pkgs)
	{
		super();
		codec = new Codec(codeccode);
		codec.init();
		m_ec = MediaService.m_ec;
		
		this.ec_buffer_pkgs = ec_buffer_pkgs;
	}

	public void startThread()
	{
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stopThread()
	{
		if (runner != null) {
			Thread moribund = runner;
			runner = null;
			moribund.interrupt();
		}
	}

	public void run()
	{

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (Thread.currentThread() == runner) {
			long ms = System.currentTimeMillis();

			synchronized (mutex) {
				while (isIdle()) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (mutex) {
				byte[] raw_temp = new byte[leftSize + rtp_head];
				System.arraycopy(rawdata, 0, raw_temp, rtp_head, leftSize);
				getSize = codec.decode(raw_temp, processedData, leftSize);
				// Log.i(TAG,
				// ""+leftSize+" size data after decode is "+getSize);
				// Log.i(TAG, "m_ec is "+m_ec);
/****** 回音消除 **********/
				if (m_ec != null) {
					if (num_recv < ec_buffer_pkgs/* 20 */) // 延迟20个rtp包才 启动回音消除操作
						num_recv++;
					else {
						m_ec.putData(false, processedData, processedData.length);
					}
				}

				// while (m_ec.isGetData()) {// 添加
				m_out_q.add(processedData);
				// }

				// m_out_q.add(processedData);
				setIdle();
			}
			Log.i(TAG, "Decoder time is " + (System.currentTimeMillis() - ms));
		}
	}

	public void putData(long ts, byte[] data, int offset, int size)
	{
		synchronized (mutex) {
			this.ts = ts;
			System.arraycopy(data, offset, rawdata, 0, size);
			this.leftSize = size;
			mutex.notify();
		}
	}

	public boolean isGetData()
	{
		return m_out_q.size() == 0 ? false : true;
	}

	public short[] getData()
	{
		return m_out_q.removeFirst();
	}

	public boolean isIdle()
	{
		return leftSize == 0 ? true : false;
	}

	public void setIdle()
	{
		leftSize = 0;
	}

	public void free()
	{
		Log.i(TAG, "Decoder free");
		num_recv = 0;
		codec.close();
	}
}
