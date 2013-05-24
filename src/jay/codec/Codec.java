package jay.codec;

import android.util.Log;

public  class Codec {
	private final String TAG ="Codec"; 
	
	protected Speex speex;
	protected ulaw g711u; 
	protected alaw g711a;
	protected G722 g722;
	protected int codeccode;
	protected final int speex_pt=101;   //Dynamic
	protected final int framesize=160;
	protected final int DEFAULT_BITRATE = 64000;
	//choose what codec to use
	public Codec(int codeccode) {
		this.codeccode = codeccode;
	}
	
	public void init() {
		switch(this.codeccode){
		case speex_pt:
			Log.d(TAG, "Codec speex selected");
			speex=new Speex();
			speex.init();
			break;
		case 0:
			Log.d(TAG, "Codec g711u selected");
			g711u = new ulaw();
			g711u.init();
			break;
		case 8:
			Log.d(TAG, "Codec g711a selected");
			g711a = new alaw();
			g711a.init();
			break;
		case 9:
			Log.d(TAG, "Codec g722 selected");
			g722 = new G722();
			g722.init();
			break;
		}
	}
    //I put load() in related codec class
	public int open(int compression){
		switch(this.codeccode){
		case 0:
			return 0;
		case speex_pt:
			return speex.open(compression);
		case 8:
			return 0;
		case 9:
			return g722.open(DEFAULT_BITRATE);
		default:
			return 0;
		}
	}
	public int getFrameSize()
	{
		switch(this.codeccode){
		case 0:
			return 0;
		case speex_pt:
			return speex.getFrameSize();
		case 8:
			return 0;
		case 9:
			return 0;
		default:
			return 0;
		}
	}
	public int decode(byte[] encoded, short[] lin, int size)
	{
		switch(this.codeccode){
		case speex_pt:
			return speex.decode(encoded, lin, size);
		case 0:
			return g711u.decode(encoded, lin, size);
		case 8:
			return g711a.decode(encoded, lin, size);
		case 9:
			return g722.decode(encoded, lin, size);
		default:
			return 0;
		}
	}
	public int encode(short[] lin, int offset, byte[] encoded, int size){
		switch(this.codeccode){
		case speex_pt:
			return speex.encode(lin, offset, encoded, size);
		case 0:
			return g711u.encode(lin, offset, encoded, size);
		case 8:
			return g711a.encode(lin, offset, encoded, size);
		case 9:
			return g722.encode(lin, offset, encoded, size);
		default:
			return 0;
		}
	}
	public void close(){
		switch(this.codeccode){
		case 0:
			break;
		case speex_pt:
			speex.close();
			break;
		case 8:
			break;
		case 9:
			g722.close();
		default:
			break;
		}
	}

}
