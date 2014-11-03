package org.gheskio.queue;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Gedit extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prefs);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_record, menu);
		return true;
	}
	
	public void doUpdate(View view) {
		
		EditText commentText = (EditText)findViewById(R.id.editText1);
		String newComments = commentText.getText().toString();
		String tokenId = this.getIntent().getStringExtra("TOKEN_ID"); 		
		String selection = "update simpleq set comments = '" + newComments + "' where token_id = " + 
				tokenId + " and duration = 0";
			
			String selectionArgs[] = {};

			Cursor c =  MainActivity.myDB.rawQuery(
					selection,
					selectionArgs);
			
			// XXX - add a new log record to denote this has been modified ; 
			// perhaps with duraction -1 ??
			
			Context context = getApplicationContext();
			String msg = getResources().getString(R.string.record_updated);
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();			
	}
	
	public void doDelete(View view) {
		String tokenId = this.getIntent().getStringExtra("TOKEN_ID"); 		
		String selection = "delete from simpleq where token_id = " + 
				tokenId + " and duration = 0";
			
			String selectionArgs[] = {};

			Cursor c =  MainActivity.myDB.rawQuery(
					selection,
					selectionArgs);

			// XXX - add a new log record to denote this has been modified ; 
			// perhaps with duraction -1 ??
			
			TextView mTextView = (EditText)findViewById(R.id.textView1);
			mTextView.setText("");	
			
			EditText commentText = (EditText)findViewById(R.id.editText1);
			commentText.setText("");
			commentText.setEnabled(false);
			
			TextView startTimeText = (TextView)findViewById(R.id.textView5);
			startTimeText.setText("");
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        String tokenId = this.getIntent().getStringExtra("TOKEN_ID"); 
		TextView mTextView = (EditText)findViewById(R.id.textView1);
		mTextView.setText(tokenId);		
		
        String comments = this.getIntent().getStringExtra("COMMENTS"); 
		EditText commentText = (EditText)findViewById(R.id.editText1);
		commentText.setText(comments);
		
		String startTime = this.getIntent().getStringExtra("STARTTIME"); 
		TextView startTimeText = (TextView)findViewById(R.id.textView5);
		startTimeText.setText(startTime);
		
    }
    
}
