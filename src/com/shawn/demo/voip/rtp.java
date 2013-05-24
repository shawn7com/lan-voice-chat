package com.shawn.demo.voip;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//
//import android.app.Activity;
//import android.util.Log;
//
public class rtp
{
//	private final String TAG = "RTP";
//		final int READ_SIZE = 1024;	//bytes count for every reading from 'inputStreamForReceive'
//        final int BUFFER_SIZE_RECEIVE = READ_SIZE * 128;
//        final int BUFFER_SIZE_RTP = READ_SIZE * 20;
////        stateMachineOfFrameReceiving STM_Recv = stateMachineOfFrameReceiving.FRAMEHEAD_NOTFOUND;
//        int i;
//        byte[] PSC_checkArr = new byte[3];
//        byte[] RTP_pocket_buffer = new byte[BUFFER_SIZE_RTP];
//        byte[] bufferForReceive = new byte[BUFFER_SIZE_RECEIVE]; 	//64K
//        int num = 0;  
//        InputStream inputStreamForReceive = null;
//    public void run() {  
//        
//
//        //udp
//        final int RTP_PORT_SENDING = 8000;
//        final int RTP_PORT_DEST = 8000;
//        DatagramSocket socketSendRPT = null;
//        DatagramPacket RtpPocket = null;
//        InetAddress PC_IpAddress = null;
//        try { 
//        	PC_IpAddress = InetAddress.getByName("192.168.1.100");
//        } 
//        catch (UnknownHostException e){
//        	e.printStackTrace();
//        } 
//        
//        try{
//        	socketSendRPT = new DatagramSocket(RTP_PORT_SENDING);
//        }
//        catch (SocketException e){
//        	e.printStackTrace();
//        }
//
//       
//        
//      //get the data stream from mediaRecorder
//        try {  
//            inputStreamForReceive = receiver.getInputStream();	
//        } catch (IOException e1) {  
//            return;  
//        } 
//        
//        //create a media file to store captured video data
//        try {  
//            File file = new File("/sdcard/stream02.h263");  
//            Log.d(TAG, "File created!"); 
//            if (file.exists())  
//                file.delete();//delete old file
//            raf = new RandomAccessFile(file, "rw");  
//        } catch (Exception ex) {  
//            Log.v("System.out", ex.toString());  
//        }	
//        
//
//        
//        int offsetInBufferForReceive = 0;
//	        m_timer.schedule(m_timerTask, 1000, 1000);	//timer for statistics
//
//	        STM_Recv = stateMachineOfFrameReceiving.FRAMEHEAD_NOTFOUND;
//	        PSC_checkArr[0] = (byte)0xFF;
//		PSC_checkArr[1] = (byte)0xFF;
//		PSC_checkArr[2] = (byte)0xFF;
//	        while (true && !threadCanStop) { 
//	        //1. read data from receive stream until to the end
//	        	offsetInBufferForReceive = 0;
//	        do {  
//	            try {  
//	                num = inputStreamForReceive.read(bufferForReceive, offsetInBufferForReceive, READ_SIZE);//copy data to buffer
//	                if(num < 0){	// there's nothing in there
//	                	//wait for a while
//				        try {  
//				            Thread.currentThread().sleep(5); 
//				        } catch (InterruptedException e1) {  
//				        	e1.printStackTrace();  
//				        }
//				        break;
//	                }
//	                
//	                offsetInBufferForReceive += num; //offsetInBufferForReceive points to next position can be wrote
//	                if (num < READ_SIZE || offsetInBufferForReceive == BUFFER_SIZE_RECEIVE) {  //indicating the end of this reading
//	                    break;  																 // or bufferForReceive is full
//	                }  
//	            } catch (IOException e) {  
//	                break;  
//	            }  
//	        }while(false);//do
//	        
//	        //2. find Picture Start Code (PSC) (22 bits) in bufferForReceive
//	        for(i=0; i< offsetInBufferForReceive; i++){
//	        	PSC_checkArr[0] = PSC_checkArr[1];
//	        	PSC_checkArr[1] = PSC_checkArr[2];
//	        	PSC_checkArr[2] = bufferForReceive[i];
//	        	
//	        	//see if got the PSC
//	        	if(PSC_checkArr[0] == 0 && PSC_checkArr[1] == 0 && (PSC_checkArr[2] & (byte)0xFC) == (byte)0x80)
//	        	{
//	        		//found the PSC
//	        		if(STM_Recv == stateMachineOfFrameReceiving.FRAMEHEAD_NOTFOUND)
//	        		{
//	        			STM_Recv = stateMachineOfFrameReceiving.FRAMEHEAD_FOUND;
//	        			//copy current byte to packet buffer
//	        			intializeRTP_PocketBuffer(RTP_pocket_buffer);
//	        			RTP_pocket_buffer[offsetInRTP_pocket_buffer] = bufferForReceive[i];
//	        			offsetInRTP_pocket_buffer++;
//	        		}
//	        		else if(STM_Recv == stateMachineOfFrameReceiving.FRAMEHEAD_FOUND)
//	        		{
//	        			//delete two zeros in the end of the buffer
//	        			offsetInRTP_pocket_buffer -= 2;
//
//	        			//TODO:3. send the packet, and reset the buffer
//	        			RtpPocket = new DatagramPacket(RTP_pocket_buffer, offsetInRTP_pocket_buffer, PC_IpAddress, RTP_PORT_DEST);  
//	        			try {
//	        				socketSendRPT.send(RtpPocket);
//	        				Log.v(TAG, "Sent bytes: " + String.valueOf(offsetInRTP_pocket_buffer));  
//	        			} catch (IOException e) {
//	        				e.printStackTrace();
//						}
//	        			
//	        			/*
//				        try { 
//				        	raf.write(RTP_pocket_buffer, 0, offsetInRTP_pocket_buffer);  
//				        	//raf.close();
//				        } catch (IOException e1) {  
//				            e1.printStackTrace();  
//				        }
//				        */
//				        
//				        intCapturedFrameCount++;	//for statistics
//	        			//copy current byte to packet buffer
//	        			intializeRTP_PocketBuffer(RTP_pocket_buffer);
//	        			RTP_pocket_buffer[offsetInRTP_pocket_buffer] = bufferForReceive[i];
//	        			offsetInRTP_pocket_buffer++;
//	        			
//	        		}
//
//	        	}
//	        	else//if NOT got the PSC
//	        	{
//	        		if(STM_Recv == stateMachineOfFrameReceiving.FRAMEHEAD_NOTFOUND)
//	        		{
//	        			continue;
//	        		}
//	        		else if(STM_Recv == stateMachineOfFrameReceiving.FRAMEHEAD_FOUND)
//	        		{
//	        			//copy current byte to packet buffer
//	        			RTP_pocket_buffer[offsetInRTP_pocket_buffer] = bufferForReceive[i];
//	        			offsetInRTP_pocket_buffer++;
//	        		}
//	        	}//if(PSC_checkArr[0] == 0 && PSC_checkArr[1] == 0 && (PSC_checkArr[2] & (byte)0xFC) == (byte)0x80)
//	        	
//	        	//4. if RTP_pocket_buffer is full then discard all the bytes in it
//	        	if(offsetInRTP_pocket_buffer >= BUFFER_SIZE_RTP)
//	        	{
//	        		STM_Recv = stateMachineOfFrameReceiving.FRAMEHEAD_NOTFOUND;
//	        		offsetInRTP_pocket_buffer = 0;
//	        	}
//	        	
//	        }//for(i=0; i< offsetInBufferForReceive; i++)
//	        
//	        
//        }// while (true && !threadCanStop)
//	        
//	        m_timer.cancel();
//        
//        releaseMediaRecorder();
//        
//    }  
//})
}
