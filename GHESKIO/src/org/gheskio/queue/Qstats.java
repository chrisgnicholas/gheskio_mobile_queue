package org.gheskio.queue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Qstats extends Activity {
	
	// textView2 - gives
	// textView4 - takes
	// textView8 - startTime
	// textView10 - endTime
	// textView6 - avg wait
	// textView12 - Q size
	// textView14 - avg Q size
	
	public static ProgressDialog mProgress = null;
	
	// not sure why we can't just reuse public statics from 
	// MainActivity, but whatever...
	
	public static SimpleQdbHelper mySQRDBH = null;
	public static SQLiteDatabase myDB = null;
	public static SharedPreferences.Editor editor = null;
	public static SharedPreferences sharedPref = null;
	
	public static boolean uploadProblem = false;

	private void checkInit() {			
		// open our basic KV store and see if we have initialized the
		// SQLlite db yet, or if this is the first time through...
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		mySQRDBH = new SimpleQdbHelper(getCurrentFocus().getContext());
		myDB = mySQRDBH.getWritableDatabase();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qstats);
		checkInit();
	}
	
    @Override
    protected void onRestart() {
        super.onStart();
        // The activity is about to become visible.
        refreshStats();
    }
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        refreshStats();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        refreshStats();
    }
	
	public void doRefresh(View view) {
		refreshStats();
	}
	
	public void doClear(View view) {
		String whereClause = "delete from simpleq ";
		String selectionArgs[] = {};
		
		myDB.execSQL(whereClause);
		
		whereClause = "delete from simpleqrecord ";
		myDB.execSQL(whereClause);

		refreshStats();
	}
	
	public void doRefreshStats(View view) {
		refreshStats();
	}
	
	public void refreshStats() {
		
		EditText numGivesTV = (EditText)findViewById(R.id.numGivesText);
		EditText numTakesTV = (EditText)findViewById(R.id.editText2);
		EditText minTimeTV = (EditText)findViewById(R.id.editText3);
		EditText maxTimeTV = (EditText)findViewById(R.id.editText4);
		EditText avgTimeTV = (EditText)findViewById(R.id.editText6);
		TextView numshowsTV = (TextView)findViewById(R.id.textView6);

		// OK - we need to populate the fields with stuff from the database...
		
		// XXX - handle zero length cases
		String selectionArgs[] = {};
		
		String countSelection = "select count(*) from simpleqrecord";
		Cursor c = myDB.rawQuery(countSelection, selectionArgs);
		c.moveToFirst();
		int numRows = c.getInt(0);
		c.close();
		
		if (numRows > 0) {
			
			String selection = "select count(*) from simpleqrecord where event_type = 'give'";	
			c = myDB.rawQuery(selection, selectionArgs);
		
			c.moveToFirst();
			int numGives = c.getInt(0);
			numGivesTV.setText(Integer.toString(numGives));
			System.out.println("numGives: " + numGives);

			c.close();
		
			selection = "select count(*) from simpleqrecord where event_type = 'take'";		
			c = myDB.rawQuery(selection, selectionArgs);		
			c.moveToFirst();
			int numTakes = c.getInt(0);
			c.close();	
			numTakesTV.setText(Integer.toString(numTakes));
		
			selection = "select count(*) from simpleqrecord where event_type = 'show'";		
			c = myDB.rawQuery(selection, selectionArgs);		
			c.moveToFirst();
			int numShows = c.getInt(0);
			c.close();	
			numshowsTV.setText(Integer.toString(numShows));
			
			selection = "select min(event_time) from simpleqrecord";		
			c = myDB.rawQuery(selection, selectionArgs);		
			c.moveToFirst();
			long minTime = c.getLong(0);
			java.util.Date minDate = new java.util.Date();
			minDate.setTime(minTime);
			// int minHrs = minDate.getHours();
			// int minMins = minDate.getMinutes();
		
			minTimeTV.setText(minDate.toString());
			c.close();
		
			selection = "select max(event_time) from simpleqrecord";	
		
			c = myDB.rawQuery(selection, selectionArgs);
		
			c.moveToFirst();

			long maxTime = c.getLong(0);
			c.close();
		
			java.util.Date maxDate = new java.util.Date();
			maxDate.setTime(maxTime);
			// int maxHrs = maxDate.getHours();
			// int maxMins = maxDate.getMinutes();
		
			maxTimeTV.setText(maxDate.toString());	
		
			// update the textViews
			
			selection = "select count(*) from simpleq where duration > 0";
			c = myDB.rawQuery(selection, selectionArgs);
			c.moveToFirst();
			int numSQRows = c.getInt(0);
			c.close();

			if (numSQRows > 0) {
				selection = "select avg(duration) from simpleq where duration > 0";		
				c = myDB.rawQuery(selection, selectionArgs);	
				c.moveToFirst();
		
				int avgTime = c.getInt(0);
				c.close();
		
				// ok - try to format this into hrs+minutes
				int numSeconds = avgTime / 1000;
				float mins = ((float)numSeconds) / 60;	
		
				avgTimeTV.setText(mins + " mins");	
			} else {
				// check to be sure it isn't already in the Q			
				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.not_enough_info_for_avgs);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();
				avgTimeTV.setText("");
			}
		} else {
			
		}
	}

	   protected class LongOperation extends AsyncTask<Context, Integer, String> {

	        @Override
	        protected String doInBackground(Context... params) {
	        	UploadThread dut = new UploadThread(mProgress);
				Thread uploadThread = new Thread(dut);
				uploadThread.start();
				try {
					uploadThread.join();
					if (uploadProblem) {
						// check to be sure it isn't already in the Q			
						Context context = getApplicationContext();
						String msg = getResources().getString(R.string.problem_uploading);
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, msg, duration);
						toast.show();
					}
				} catch(InterruptedException ioe){
					ioe.printStackTrace();
				}
	            return "Executed";
	        }

	        @Override
	        protected void onPostExecute(String result) {
	        	if (!uploadProblem) {
	        		mProgress.setMessage("done..");
	        		mProgress.dismiss();
	        		EditText numGivesTV = (EditText)findViewById(R.id.numGivesText);
	        		EditText numTakesTV = (EditText)findViewById(R.id.editText2);
	        		EditText minTimeTV = (EditText)findViewById(R.id.editText3);
	        		EditText maxTimeTV = (EditText)findViewById(R.id.editText4);
	        		EditText avgTimeTV = (EditText)findViewById(R.id.editText6);
	        		TextView numshowsTV = (TextView)findViewById(R.id.textView6);
	        		numGivesTV.setText("0");
	        		numTakesTV.setText("0");
	        		minTimeTV.setText("");
	        		maxTimeTV.setText("");
	        		avgTimeTV.setText("");
	        		numshowsTV.setText("");
	        	} else {
	        		
	        	}
	        }

	        @Override
	        protected void onPreExecute() {
					        		        	
	        }

	        @Override
	        protected void onProgressUpdate(Integer... values) {}
	    }
	
	
	/** upload the SimpleQRecord history file to the server
	 * 
	 * @param view
	 */
	
	public void doUpload(View view) {
		mProgress = new ProgressDialog(this);
		mProgress.setCancelable(true);
		mProgress.setMessage("uploading..");
		mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgress.setProgress(0);
		mProgress.show();
		LongOperation fooOp = new LongOperation();
		fooOp.execute(this);			
	  } 
	
	public void doUpdate(View view){
		refreshStats();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.qstats, menu);
		return true;
	}
	
	public void setUploadCount(int numRecords){
		TextView uploadTV = (TextView)findViewById(R.id.textView6);
		uploadTV.setText(Integer.toString(numRecords));
	}

}
