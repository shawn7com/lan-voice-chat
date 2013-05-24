package jay.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MediaManager
{

	private MediaService mMediaService;
	private Context context;
	boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			mMediaService = ((MediaService.JayBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className)
		{
			mMediaService = null;
		}
	};

	void doBindService(Intent intent)
	{
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService()
	{
		if (mIsBound) {
			context.unbindService(mConnection);
			mIsBound = false;
		}
	}

	public MediaManager(Context context)
	{
		this.context = context;
		initService();
	}

	public void initService()
	{
		// we want to start our service (for handling our time-consuming
		// operation)
		Intent serviceIntent = new Intent(context, MediaService.class);
		// used to bind sipservice ,it could be destroyed when app was destroyed
		doBindService(serviceIntent);
	}

	public void test()
	{
		mMediaService.test();
	}

	public void startAudio()
	{
		mMediaService.startAudio("192.168.1.4", 8, 8000, 8000, 8000, 5);
	}

	public void stopAudio()
	{
		mMediaService.stopAudio();
	}
}