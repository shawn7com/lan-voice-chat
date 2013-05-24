package jay.dencode;

import java.util.LinkedList;

import android.os.SystemClock;
import android.util.Log;

import jay.codec.Codec;
import jay.codec.EchoCancellation;
import jay.media.MediaService;

public class Encoder implements Runnable
{
	private final String TAG = "Encoder";
	private EchoCancellation m_ec;
	private Codec codec;
	private volatile Thread runner;
	private final Object mutex = new Object();
	protected LinkedList<byte[]> m_in_q = new LinkedList<byte[]>(); // store
																	// processed
																	// data
	private int frameSize = 160;
	private volatile int leftSize = 0;
	public static int num_send;
	private int dataLen = 0;
	private int Rtp_head = 12;
	private long ts;
	private byte[] processedData = new byte[frameSize + 12];
	private short[] rawdata = new short[frameSize];
	private short[] output = new short[frameSize];
	
	/** 回音消除缓冲包个数 （sipUA默认20）*/
	protected int ec_buffer_pkgs = 20;

	public Encoder(int codeccode)
	{
		codec = new Codec(codeccode);
		codec.init();
		frameSize = codec.getFrameSize();
		this.m_ec = MediaService.m_ec;
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
	public Encoder(int codeccode,int ec_buffer_pkgs)
	{
		codec = new Codec(codeccode);
		codec.init();
		frameSize = codec.getFrameSize();
		
		this.m_ec = MediaService.m_ec;
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

		// 设置线程优先级
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
				output = rawdata.clone();
				if (m_ec != null) {// 回声消除
					if (Decoder.num_recv > 0) {// 收到对方数据才开始回音消除
						if (num_send <  ec_buffer_pkgs/* 20 */) // 延迟20个rtp包才 启动回音消除操作
						{
							num_send++;
						} else {
							m_ec.putData(true, output, output.length);// 回身消除数据写入
							if (m_ec.isGetData()) {
								output = m_ec.getshortData();// 获取处理后的数据
							}
						}
					}
				}
				long time = 0;
				time = System.currentTimeMillis();
				getSize = codec.encode(output, 0, processedData, leftSize);// 对回声消除处理后的数据进行编码
				Log.e(TAG, "Encoder time = " + (System.currentTimeMillis() - time));

				// Log.i(TAG, ""+leftSize
				// +" size data after encode is "+getSize);
				this.dataLen = getSize;
				byte tempdata[] = new byte[getSize + Rtp_head];// 存储加上rtp头后的数据
				System.arraycopy(processedData, Rtp_head, tempdata, Rtp_head, getSize);
				m_in_q.add(tempdata);// 将rtp格式的音频数据加入链表
				setIdle();

			}

			Log.e(TAG, "One time encode time is " + (System.currentTimeMillis() - ms));
		}
		free();
	}

	public void putData(long ts, short[] data, int off, int size)
	{
		synchronized (mutex) {
			this.ts = ts;
			System.arraycopy(data, off, rawdata, 0, size);
			this.leftSize = size;
			mutex.notify();// 收到音频数据后，唤醒编码线程
		}
	}

	public byte[] getData()
	{
		if (m_in_q.size() > 0) {
			return m_in_q.removeFirst();
		} else {
			return null;
		}
	}

	public int getdataLen()
	{
		return this.dataLen;
	}

	public boolean isGetData()
	{
		return m_in_q.size() == 0 ? false : true; // 判断是否有处理后的数据可获取，当编码后的数据链表为0时，返回false，表示没有数据可以获取
	}

	public boolean isIdle()
	{
		return leftSize == 0 ? true : false;// 编码器是否空闲，当剩余的待处理数据为0时，则空闲
	}

	public void setIdle()
	{
		leftSize = 0;
	}

	private void free()
	{
		Log.i(TAG, "Encoder free");
		num_send = 0;
		codec.close();
	}
}
