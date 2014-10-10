package org.gheskio.queue;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.Toast;
import android.content.ContextWrapper;


public class UploadThread implements Runnable {

	// provides crude async way to cancel an upload in progress
	public static boolean doCancel = false;
	
	private ProgressDialog mProgress = null;

	public UploadThread(ProgressDialog pProgress) {
		// TODO Auto-generated constructor stub
			mProgress = pProgress;
	}
	
	public void run() {	
		
		try {
			String uploadURL = MainActivity.sharedPref.getString("URL", "http://192.168.10.9:8080/gheskio/upload/?foo=goo");
			Uri uploadUri = Uri.parse(uploadURL);
			String uploadHost = uploadUri.getHost();
			// check if we can get to the host...
			int uploadPort = uploadUri.getPort();
			if (uploadPort == -1) {
				uploadPort = 80;
			}
			// just for fun, try to open a socket there...
			System.out.println("trying: " + uploadHost + ":" + uploadPort);
			Socket testSocket = new Socket(uploadHost, uploadPort);
			java.io.InputStream testIS = testSocket.getInputStream();
			// if no exception, hunky dory and continue...
			testSocket.close();
		
		// get the count of number of rows to update the progress bar
		String selection = "select count(*) from simpleqrecord";
		String selectionArgs[] = {};
		
		// now see if it's already in the SimpleQ...
		Cursor c =  MainActivity.myDB.rawQuery(selection, selectionArgs);
		c.moveToFirst();
		int numRows = c.getInt(0);
		c.close();
		
		mProgress.setMax(numRows);
		
		// update progress bar in 10% increments
		double fraction = 0.1;
		int currentFraction = 10;
		int nextUpdate = (int)(fraction * numRows);	
		 
		boolean isDone = false;
		StringBuilder sb = new StringBuilder();
		numRows = 0;
		 
		// open URL for POSTing	 
		Authenticator.setDefault(new SimpleAuth());
		 				
	    URL url; 
	    HttpURLConnection urlConn; 
	    
	    	System.out.println("connecting to: " + uploadURL);
	    	url = new URL(uploadURL);

	    	urlConn = (HttpURLConnection)url.openConnection();
	    	urlConn.setRequestMethod("POST");
	    	urlConn.setDoOutput(true); 
	    	urlConn.setDoInput(true);
	    	urlConn.setUseCaches(false);
	    	
	    	PrintWriter pw = new PrintWriter(urlConn.getOutputStream()); 
	    
	    	// now actually get and upload the Rows...
	    	selection = "Select " + 
				SimpleQRecord.COLUMN_TOKEN_ID + ", " +
				SimpleQRecord.COLUMN_EVENT_TIME + ", " +
				SimpleQRecord.COLUMN_COMMENTS + ", " +
				SimpleQRecord.COLUMN_EVENT_TYPE + ", " +
				SimpleQRecord.COLUMN_WORKER_ID + ", " +
				SimpleQRecord.COLUMN_STATION_ID + ", " +
				SimpleQRecord.COLUMN_FACILITY_ID + " from SimpleQRecord";
		 
	    	c =  MainActivity.myDB.rawQuery(selection, selectionArgs);
			c.moveToFirst();
		 
			// XXX - should really get, add the device id
			// to the upload string as well...
			
			
			while (!isDone) {				
				sb.setLength(0);
				sb.append("waiting_time_app" + "|");
				sb.append(SimpleQRecord.VERSION + "|");
				String nextToken = c.getString(0);
				sb.append(nextToken + "|");
				long eventTime = c.getLong(1);
				sb.append(Long.toString(eventTime) + "|");
				String nextComments = c.getString(2);
				sb.append(nextComments + "|");
				String nextEventType = c.getString(3);
				sb.append(nextEventType + "|");
				String nextWorkerId = c.getString(4);
				sb.append(nextWorkerId + "|");
				String nextStationID = c.getString(5);
				sb.append(nextStationID + "|");
				String nextFacilityID = c.getString(6);
				sb.append(nextFacilityID + "|");
			 
				// String base64String = Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
				pw.println(sb.toString());
				++numRows;
			    mProgress.setProgress(numRows);			

				if (!c.moveToNext()) {
					isDone = true;
				}
			}
			
			c.close();
			
			pw.flush();
			InputStream is = urlConn.getInputStream();
			is.close();

		 
		urlConn.disconnect();

		 // delete the rows...
		 String deleteString =  "delete from SimpleQRecord";
		 MainActivity.myDB.execSQL(deleteString);
		 
		 deleteString =  "delete from SimpleQ where duration > 0";
		 MainActivity.myDB.execSQL(deleteString);
		 
		 Qstats.uploadProblem = false;
		 		 
		 // Qstats.mProgress.setProgress(0);

	    } catch (Exception e) {
	    	Qstats.uploadProblem = true;
	    	e.printStackTrace();
	    }
	  } 			
	}

