/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
*/

package com.almalence.plugins.processing.simple;

/* <!-- +++
import com.almalence.opencam_plus.MainScreen;
import com.almalence.opencam_plus.PluginManager;
import com.almalence.opencam_plus.PluginProcessing;
+++ --> */
// <!-- -+-
import android.util.Log;

import com.almalence.SwapHeap;
import com.almalence.opencam.MainScreen;
import com.almalence.opencam.PluginManager;
import com.almalence.opencam.PluginProcessing;
//-+- -->
import com.almalence.util.ImageConversion;

/***
Implements simple processing plugin - just translate shared memory values 
from captured to result.
***/

public class SimpleProcessingPlugin extends PluginProcessing
{
	private long sessionID=0;
	
	public static boolean DROLocalTMPreference = true;
	public static int prefStrongFilter = 0;
	public static int prefPullYUV = 0;
	
	public SimpleProcessingPlugin()
	{
		super("com.almalence.plugins.simpleprocessing", 0, 0, 0, null);
	}
	
	@Override
	public void onStartProcessing(long SessionID)
	{
		sessionID=SessionID;
		
		int iSaveImageWidth = MainScreen.getSaveImageWidth();
		int iSaveImageHeight = MainScreen.getSaveImageHeight();
		
		int mImageWidth = MainScreen.getImageWidth();
		int mImageHeight = MainScreen.getImageHeight();
		
//		Log.v("!!!!!!!!!!!!", "SessionID " + sessionID + " shared size " + PluginManager.getInstance().sizeOfSharedMemory());
		String num = PluginManager.getInstance().getFromSharedMem("amountofcapturedframes"+Long.toString(sessionID));
		if (num == null)
			return;
		int imagesAmount = Integer.parseInt(num);
		
		if (imagesAmount==0)
			imagesAmount=1;
		
		for (int i=1; i<=imagesAmount; i++)
		{
			int orientation = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("frameorientation" + i+Long.toString(sessionID)));
			String isDRO = PluginManager.getInstance().getFromSharedMem("isdroprocessing"+Long.toString(sessionID));
			if(isDRO != null && isDRO.equals("0"))
			{
				AlmaShotDRO.Initialize();
				
				int compressed_frame[] = new int[1];
		        int compressed_frame_len[] = new int[1];

				compressed_frame[0] = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("frame" + i +Long.toString(sessionID)));
				compressed_frame_len[0] = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("framelen" + i +Long.toString(sessionID)));
				
				AlmaShotDRO.ConvertFromJpeg(
		    			compressed_frame,
		    			compressed_frame_len,
		    			1,
		    			mImageWidth, mImageHeight);
		        
				int yuv = AlmaShotDRO.DroProcess(mImageWidth, mImageHeight, 
						1.5f,
						DROLocalTMPreference,
						0,
						prefStrongFilter,
						prefPullYUV);				
				
				AlmaShotDRO.Release();
				
				if(orientation == 90 || orientation == 270)
				{					
					PluginManager.getInstance().addToSharedMem("saveImageWidth"+String.valueOf(sessionID), String.valueOf(iSaveImageHeight));
			    	PluginManager.getInstance().addToSharedMem("saveImageHeight"+String.valueOf(sessionID), String.valueOf(iSaveImageWidth));
				}
				else
				{
					PluginManager.getInstance().addToSharedMem("saveImageWidth"+String.valueOf(sessionID), String.valueOf(iSaveImageWidth));
			    	PluginManager.getInstance().addToSharedMem("saveImageHeight"+String.valueOf(sessionID), String.valueOf(iSaveImageHeight));
				}
				
				PluginManager.getInstance().addToSharedMem("resultframe"+i+Long.toString(sessionID), String.valueOf(yuv));
			}
			else
			{
				
				int frame = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("frame" + i+Long.toString(sessionID)));
	    		int len = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("framelen" + i+Long.toString(sessionID)));
	    		
	    		PluginManager.getInstance().addToSharedMem("resultframeformat"+i+Long.toString(sessionID), "jpeg");
				PluginManager.getInstance().addToSharedMem("resultframe"+i+Long.toString(sessionID), String.valueOf(frame));
		    	PluginManager.getInstance().addToSharedMem("resultframelen"+i+Long.toString(sessionID), String.valueOf(len));
		    	
				PluginManager.getInstance().addToSharedMem("saveImageWidth"+String.valueOf(sessionID), String.valueOf(iSaveImageWidth));
		    	PluginManager.getInstance().addToSharedMem("saveImageHeight"+String.valueOf(sessionID), String.valueOf(iSaveImageHeight));
			}
			
			
			boolean cameraMirrored = Boolean.parseBoolean(PluginManager.getInstance().getFromSharedMem("framemirrored" + i+Long.toString(sessionID)));
	    	PluginManager.getInstance().addToSharedMem("resultframeorientation" + i + String.valueOf(sessionID), String.valueOf(orientation));
	    	PluginManager.getInstance().addToSharedMem("resultframemirrored" + i + String.valueOf(sessionID), String.valueOf(cameraMirrored));
		}
		
		
		PluginManager.getInstance().addToSharedMem("amountofresultframes"+Long.toString(sessionID), String.valueOf(imagesAmount));
	}

	@Override
	public boolean isPostProcessingNeeded(){return false;}

	@Override
	public void onStartPostProcessing(){}
}
