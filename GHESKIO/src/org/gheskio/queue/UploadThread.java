package org.gheskio.queue;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Handler;


public class UploadThread implements Runnable {

	// provides crude async way to cancel an upload in progress
	public static boolean doCancel = false;
	
	private ProgressDialog mProgress = null;

	public UploadThread(ProgressDialog pProgress) {
		// TODO Auto-generated constructor stub
			mProgress = pProgress;
	}
	
	public void run() {

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
		 
		String uploadURL = MainActivity.sharedPref.getString("URL", "http://maps.geography.uc.edu/cgi-bin/gheskio_upload.sh");
				
	    URL url; 
	    HttpURLConnection urlConn; 
	    
	    try {
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
		 
			while (!isDone) {				
				sb.setLength(0);
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
		 		 
		 // Qstats.mProgress.setProgress(0);

	    } catch (Exception e) {
	    	Qstats.uploadProblem = true;
	    	e.printStackTrace();
	    }
	  } 
			
	}

