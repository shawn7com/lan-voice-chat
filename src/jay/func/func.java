package jay.func;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class func {
	
	 static public short[] byteArray2ShortArray(byte[] data) {
//	    short[] retVal = new short[data.length];
//	    for (int i = 0; i < retVal.length; i++)
//	        retVal[i] = (short) ((data[i * 2]&0xff) | (data[i * 2+1]&0xff) << 8);
//	    return retVal;
		 short[] shorts = new short[data.length/2];
		// to turn bytes to shorts as either big endian or little endian. 
		ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
		return shorts;
	    }
	 
	 public static byte[] shortArray2ByteArray(short shorts[]) {
		// to turn shorts back to bytes.
		 byte[] bytes = new byte[shorts.length * 2];
		 ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
		 return bytes;
	    }
	 /*
	  * static public short[] byteArray2ShortArray(byte[] data) {
	    short[] retVal = new short[data.length/2];
	    for (int i = 0; i < retVal.length; i++)
	        retVal[i] = (short) ((data[i * 2]&0xff) | (data[i * 2+1]&0xff) << 8);
	    return retVal;
	    }
	 
	 public static byte[] shortArray2ByteArray(short shorts[]) {
		byte[] bytes = new byte[shorts.length*2];
		for (int i = 0; i < shorts.length; i++){
			bytes[i*2]=(byte)((shorts[i]&0xff00)>>8);
			bytes[i*2+1]=(byte)(shorts[i]&0x00ff);
		}
		return bytes;
	 }
	  */

}