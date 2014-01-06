package org.gheskio.queue;


import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static EditText mEditText;
	public EditText mCommentText;
	public static SharedPreferences sharedPref = null;
	
	public static final String DBNAME = "Q_DB";
	public static final String DBVERSION = "v0.1;";
	
	public static final String DBINITKEY = "IS_DBINITIALIZED";
	
	public static SimpleQdbHelper mySQRDBH;
	public static SQLiteDatabase myDB = null;
	
	public static SharedPreferences.Editor editor = null;
	public static String qrCode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		checkInit();
		updateQlength();
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        updateQlength();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        updateQlength();
    }
	
	
	private void checkInit() {			
		// open our basic KV store and see if we have initialized the
		// SQLlite db yet, or if this is the first time through...
		sharedPref = getSharedPreferences("gheskioprefs", Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		Boolean isInitialized = sharedPref.getBoolean(DBINITKEY, false);
		
		mySQRDBH = new SimpleQdbHelper(getCurrentFocus().getContext());
		myDB = mySQRDBH.getWritableDatabase();

		if (!isInitialized) {
			// XXX - use async task for better UI response...
			String createString = SimpleQRecord.getCreateStatement();		
			myDB.execSQL(createString);
			System.out.println("executing: " + createString);
			createString = SimpleQ.getCreateStatement();
			System.out.println("executing: " + createString);
			myDB.execSQL(createString);
			editor.putBoolean(DBINITKEY, true);
			boolean isCommitted = editor.commit();
		}		
	}
	
	public void doQStats(View view) {	
		checkInit();
		Intent intent = new Intent(this, Qstats.class);
		startActivity(intent);
	}
	
	public void doPrefs(View view) {
		checkInit();
		Intent intent = new Intent(this, Prefs.class);
		startActivity(intent);
	}
	
	public void doQRScan(View view) {
		checkInit();

        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		setContentView(R.layout.activity_main);	
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	    		mEditText = (EditText)findViewById(R.id.editText1);
	    		// TextView qrTV = (TextView)findViewById(R.id.textView7);
	            qrCode = intent.getStringExtra("SCAN_RESULT");
	            // String qrformat = intent.getStringExtra("SCAN_RESULT_FORMAT"); 
	    		mEditText.setText(qrCode);
	    		// qrTV.setText(qrCode);

	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        	mEditText.setText("scan cancelled");
	        }
	    }
	}
	
	private void updateQlength() {
		
		String selection = "Select count(*) from " + SimpleQ.TABLE_NAME + " where " + 
				SimpleQ.COLUMN_DURATION + " = 0";
		
		String selectionArgs[] = {};

		Cursor c =  MainActivity.myDB.rawQuery(selection, selectionArgs);
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			int tokenCount = c.getInt(0);
			TextView tokenCountTV = (TextView)findViewById(R.id.textView2);
			tokenCountTV.setText(Integer.toString(tokenCount));			
		}
		c.close();	
		
	}

	/** give a token */
	public void doGive(View view) {
		checkInit();
		mEditText = ((EditText)findViewById(R.id.editText1));
		TextView commentET = (TextView)findViewById(R.id.editText20);
		TextView startTimeET = (TextView)findViewById(R.id.textView6);

		String commentVal = commentET.getText().toString();
		
		String tokenVal = mEditText.getText().toString().trim();
		if (tokenVal != null) {
			
			if (tokenVal.length() > 0 ) {
				// check to be sure token isn't already given...
				String queryString = "select give_time from simpleq where token_id = '" +
					tokenVal + "' and duration = 0";
				String args[] = {};
			
				Cursor c =  MainActivity.myDB.rawQuery(queryString, args);
				if (c.getCount() > 0) {
					Context context = getApplicationContext();
					String msg = getResources().getString(R.string.token_already_given);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, msg, duration);
					toast.show();
				} else {
					SimpleQRecord sqr = new SimpleQRecord(tokenVal, commentVal, "give");		
					mEditText.setText("");
					commentET.setText("");
					startTimeET.setText("");
				}
				c.close();
			} else {

				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.token_id_needed);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();
			}
		} else {
			// check to be sure it isn't already in the Q			
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.token_id_needed);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		}
		updateQlength();
	}
	
	public void doFind(View view) {
		checkInit();
		mEditText = (EditText)findViewById(R.id.editText1);
		TextView commentET = (TextView)findViewById(R.id.editText20);	
		
		String tokenVal = mEditText.getText().toString();
		if (tokenVal != null) {

			String selection = "Select comments, give_time from simpleq where token_id = " + 
				tokenVal + " and duration = 0";
			
			String selectionArgs[] = {};

			Cursor c =  MainActivity.myDB.rawQuery(
					selection,
					selectionArgs);
			
			if (c.getCount() > 0) {
				c.moveToFirst();
				String commentVal = c.getString(0);
				mCommentText = (EditText)findViewById(R.id.editText20);
				mCommentText.setText(commentVal);
				
				long startTime = c.getLong(1);
				Date startDate = new Date();
				startDate.setTime(startTime);
				TextView startTimeView = (TextView)findViewById(R.id.textView6);
				startTimeView.setText(startDate.toString());
				
			} else {
				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.no_tokens_found);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();		
			}
			c.close();
		} else {
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.token_id_needed);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		}		
	}
	
	/** take a token */
	public void doTake(View view) {
		checkInit();
		mEditText = (EditText)findViewById(R.id.editText1);		
		String tokenVal = mEditText.getText().toString();
		
		mCommentText = (EditText)findViewById(R.id.editText20);
		String commentVal = mCommentText.getText().toString();
		
		if (tokenVal != null) {
			
			// check to be sure token is really in the Q
			String queryString = "select give_time from simpleq where token_id = '" +
					tokenVal + "' and duration = 0";
			
			String args[] = {};
			
			Cursor c =  MainActivity.myDB.rawQuery(queryString, args);
			if (c.getCount() == 0) {
				c.close();
				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.token_id_needed);
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();				
			} else {				
				c.moveToFirst();
				long giveTime = c.getLong(0);
				long nowTime = new java.util.Date().getTime();

				c.close();
				long duration = nowTime - giveTime;
				String updateSQL = "update simpleQ set duration = " + duration + " where token_id = '" +
						tokenVal + "'";
				
				MainActivity.myDB.execSQL(updateSQL);
				
				// add an event row
				SimpleQRecord sqr = new SimpleQRecord(tokenVal, commentVal, "take");	
				
				// clear the fields
				mEditText.setText("");
				mCommentText.setText("");
				TextView timeTV = (TextView)findViewById(R.id.textView6);
				timeTV.setText("");				
				updateQlength();				
			}
			
		} else {
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.token_id_needed);
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		}
		updateQlength();
	}
	
	/** give a token */
	public void doShow(View view) {
		checkInit();
		mEditText = (EditText)findViewById(R.id.editText1);
		TextView commentET = (TextView)findViewById(R.id.editText20);
		String commentVal = commentET.getText().toString();
		TextView timeTV = (TextView)findViewById(R.id.textView6);
		
		String tokenVal = mEditText.getText().toString();
		if (tokenVal != null) {
			SimpleQRecord sqr = new SimpleQRecord(tokenVal, commentVal, "show");
			mEditText.setText("");
			commentET.setText("");
			timeTV.setText("");
		} else {
			// XXX - add popup dialog here!
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.token_id_needed);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		}		
	}
	
	/** look at the next token in the line */
	public void doNext(View view) {
		// make sure there is something in the Queue at all
		updateQlength();

		TextView tokenCountTV = (TextView)findViewById(R.id.textView2);
		String tokenCountText = tokenCountTV.getText().toString();
		int tokenCount = Integer.parseInt(tokenCountText);
		if (tokenCount == 0) {
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.no_tokens_found);
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		} else {
			checkInit();
			String selection = "select min(give_time) from simpleq where duration = 0";
			String selectionArgs[] = {};
			
			Cursor c =  MainActivity.myDB.rawQuery(
				selection,
				selectionArgs);
		
			c.moveToFirst();
			long minGiveTime = c.getLong(0);
			c.close();
			
			selection = "select token_id, comments from simpleq where give_time = " + Long.toString(minGiveTime);
			String selectionArgs2[] = {};
			c =  MainActivity.myDB.rawQuery(
					selection,
					selectionArgs2);
			
			c.moveToFirst();
			String nextToken = c.getString(0);
			String nextComment = c.getString(1);	
			c.close();
			
			mEditText = (EditText)findViewById(R.id.editText1);
			mEditText.setText(nextToken);
			
			mCommentText = (EditText)findViewById(R.id.editText20);
			mCommentText.setText(nextComment);			
			
			SimpleQ.lastSkipTime = minGiveTime;
			TextView timeTV = (TextView)findViewById(R.id.textView6);
			java.util.Date tokenTime = new java.util.Date();
			tokenTime.setTime(minGiveTime);			
			timeTV.setText(tokenTime.toString());
		} 	
	}
	
	/** look at the next token in the line */
	public void doSkip(View view) {
		// make sure there is something in the Queue at all
		updateQlength();
		
		mEditText = (EditText)findViewById(R.id.editText1);
		mCommentText = (EditText)findViewById(R.id.editText20);
		TextView timeTV = (TextView)findViewById(R.id.textView6);
		TextView tokenCountTV = (TextView)findViewById(R.id.textView2);
		
		String selectionArgs[] = {};
		
		String tokenCountText = tokenCountTV.getText().toString();
		int tokenCount = Integer.parseInt(tokenCountText);
		if (tokenCount == 0) {
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.no_tokens_found);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
		} else {
			checkInit();
			if (SimpleQ.lastSkipTime == 0) {
				// need to set it to the first item
				String firstGiveTimeQuery = "min(give_time) from simpleq where duration = 0";
				Cursor c =  MainActivity.myDB.rawQuery(firstGiveTimeQuery, selectionArgs);
				if (c.getCount() > 0) {
					c.moveToFirst();
					SimpleQ.lastSkipTime = c.getLong(0);
					System.out.println("setting mintime to: " + SimpleQ.lastSkipTime);
					System.out.flush();

				}
				c.close();			
			}
			
			String selection = "select give_time from simpleq where give_time > " + SimpleQ.lastSkipTime + " and duration = 0 order by give_time limit 1";
			System.out.println(selection);

			Cursor c =  MainActivity.myDB.rawQuery(selection, selectionArgs);
		
			if (c.getCount() > 0) {
				c.moveToFirst();
				long minGiveTime = c.getLong(0);
				c.close();
				
				System.out.println(selection);
				System.out.flush();
			
				String selection2 = "select token_id, comments from simpleq where give_time = " + Long.toString(minGiveTime);
				String selectionArgs2[] = {};
				System.out.println(selection2);
				System.out.flush();

				c =  MainActivity.myDB.rawQuery(selection2, selectionArgs2);
			
				c.moveToFirst();
				String nextToken = c.getString(0);
				String nextComment = c.getString(1);	
				c.close();
			
				mEditText.setText(nextToken);			
				mCommentText.setText(nextComment);			
			
				SimpleQ.lastSkipTime = minGiveTime;
				java.util.Date tokenTime = new java.util.Date();
				tokenTime.setTime(minGiveTime);
				timeTV.setText(tokenTime.toString());
			} else {
				c.close();
				mEditText.setText("");
				mCommentText.setText("");
				timeTV.setText("");

				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.end_of_queue_reached);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();
			}
		} 	
	}
	
	public void doDelete(View view) {
		checkInit();
		mEditText = (EditText)findViewById(R.id.editText1);
		TextView commentET = (TextView)findViewById(R.id.editText20);
		String commentVal = commentET.getText().toString();
		
		String tokenVal = mEditText.getText().toString();
		if (tokenVal != null) {
			mEditText = (EditText)findViewById(R.id.editText1);			
			
			String selection = "delete from " + SimpleQ.TABLE_NAME + " where token_id = ? and " + 
						SimpleQ.COLUMN_DURATION + " = 0";
				
			String selectionArgs[] = {tokenVal};

			Cursor c =  MainActivity.myDB.rawQuery(
						selection,
						selectionArgs);
				
			c.close();		
			// clear the fields
			mEditText.setText("");
			mCommentText = (EditText)findViewById(R.id.editText20);
			commentET.setText("");
			updateQlength();
		} else {
				Context context = getApplicationContext();
				String msg = getResources().getString(R.string.token_id_needed);
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, msg, duration);
				toast.show();
		}
		updateQlength();
	}
}
