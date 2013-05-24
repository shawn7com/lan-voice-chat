package jay.codec;

import java.util.LinkedList;

import android.util.Log;
import jay.func.func;

public class EchoCancellation implements Runnable{

	private Speex speex_echo = new Speex();
	private volatile Thread runner;
	private final Object mutex = new Object();
	final static int framesize = 160;
	final static int filterlength = 1024;
	private volatile int CaptureSize = 0;
	private volatile int PlaySize = 0;
	private volatile boolean isCancelling;
	protected LinkedList<short[]> m_cap_q=new LinkedList<short[]>();    //store processed data
	protected LinkedList<short[]> m_play_q=new LinkedList<short[]>();    //store processed data
	protected LinkedList<short[]> m_out_q=new LinkedList<short[]>();    //store processed data
	
	public EchoCancellation(){
		speex_echo.echoinit(framesize, filterlength);
	}
	
	public short[] echo_capture(short[] capture)
	{
		short[] buffer = new short[framesize];	
		speex_echo.echocapture(capture,buffer);
		return buffer;
	}
	public void echo_playback(short[] play)
	{
//		log.debug("start echo playback");
		speex_echo.echoplayback(play);
//		log.debug("echo playback finish");
		
	}
	
	public void startThread(){
		if(runner == null){
		    runner = new Thread(this);
		    runner.start();
		  }
	}
	
	public void stopThread(){
		 if(runner != null){
			    Thread moribund = runner;
			    runner = null;
			    moribund.interrupt();
		  }
	}
	
	public void free()
	{
		if(speex_echo!=null)
		speex_echo.echoclose();
	}

	public void run() {
		android.os.Process
		.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		while (Thread.currentThread() == runner) {
			long ms = System.currentTimeMillis();
//			Log.i("EchoCancel", "currentTime "+ms);
			
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
				if(CaptureSize!=0)
				{
					m_out_q.add(echo_capture(m_cap_q.removeFirst()));
				}
				if(PlaySize!=0)
				{
					echo_playback(m_play_q.removeFirst());
				}
				setIdle();
			}
			Log.i("EchoCancel", "Echo Cancel time is "+(System.currentTimeMillis()-ms));
		}
	}
	
	public void putData(boolean type,short[] data, int size) {
		synchronized (mutex) {
			short[] temp = new short[size];
			System.arraycopy(data, 0, temp, 0, size);
			if(type ==true)  //cap
				{
					m_cap_q.add(temp);
					this.CaptureSize = size;
				}
			else
				{
					m_play_q.add(temp);
					this.PlaySize = size;
				}
			mutex.notify();
		}
	}
	
	public void putData(boolean type,byte[] data, int size) {
		synchronized (mutex) {
			int shortsize=size/2;
			short buffer[] = new short [shortsize]; 
			buffer=func.byteArray2ShortArray(data);
			short[] temp = new short[shortsize];
			System.arraycopy(buffer, 0, temp, 0, shortsize);
			if(type ==true)  //cap
				{
					m_cap_q.add(temp);
					this.CaptureSize = shortsize;
				}
			else
				{
					m_play_q.add(temp);
					this.PlaySize = shortsize;
				}
			mutex.notify();
		}
	}
	
	public boolean isGetData()
	{
		return m_out_q.size() == 0 ?false : true; 
	}
	
	public short[] getshortData(){
		if (m_out_q.size()>0) {
			try {
				return m_out_q.removeFirst();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
			return null;

	}
	
	public byte[] getbyteData(){
		return func.shortArray2ByteArray(m_out_q.removeFirst());
	}
	
	public boolean isIdle() {
		return (CaptureSize == 0 && PlaySize==0) ? true : false;
	}
	
	public void setCancelling(boolean isCancelling) {
		synchronized (mutex) {
			this.isCancelling = isCancelling;
			if (this.isCancelling) {
				mutex.notify();
			}
		}
	}

	public void setIdle()
	{
		if(CaptureSize!=0)CaptureSize=0;
		if(PlaySize!=0)PlaySize=0;
	}
}