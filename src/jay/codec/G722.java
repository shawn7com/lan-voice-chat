/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jay.codec;

import android.util.Log;



class G722  {
	private final static String TAG = "G722";

	/*
		Acceptable values for bitrate are
		48000, 56000 or 64000
 	 */
	private static final int DEFAULT_BITRATE = 64000;

	G722() {
		load();
	}


	void load() {
		try {
			System.loadLibrary("g722_jni");
		} catch (Throwable e) {
			Log.e("g722", "load error");
		}
    
	}  
 
	public native int open(int brate);
	public native int decode(byte encoded[], short lin[], int size);
	public native int encode(short lin[], int offset, byte encoded[], int size);
	public native void close();

	public void init() {
		open(DEFAULT_BITRATE);
		Log.i(TAG, "open with "+DEFAULT_BITRATE+" brate");
	}
}
