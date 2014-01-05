package org.gheskio.queue;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Prefs extends Activity {
	
	public static SharedPreferences sharedPref = null;
	public SharedPreferences.Editor editor = null;
	
	private EditText workerET = null;
	public static String workerVal = null;
	
	private EditText stationET = null;
	public static String stationVal = null;
	
	private EditText facilityET = null;
	public static String facilityVal = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prefs);
		refreshPrefs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.prefs, menu);
		return true;
	}

	private void makeToast(String msgId) {		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		String msg = getResources().getString(R.string.token_id_needed);

		Toast toast = Toast.makeText(context, msg, duration);
		toast.show();
	}
	
	public void doServerPrefs(View view) {
		Intent intent = new Intent(this, Server.class);
		startActivity(intent);
	}
	
	private void refreshPrefs() {
		// fetch shared preferences and populate the text fields
		
		sharedPref = getSharedPreferences("gheskioprefs", Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		
		workerET = (EditText)findViewById(R.id.numGivesText);
		stationET = (EditText)findViewById(R.id.editText2);
		facilityET = (EditText)findViewById(R.id.editText3);

		workerVal = sharedPref.getString("WORKER_ID", "");
		stationVal = sharedPref.getString("STATION_ID", "");
		facilityVal = sharedPref.getString("FACILITY_ID", "");
		workerET.setText(workerVal);
		stationET.setText(stationVal);
		facilityET.setText(facilityVal);
	}
	
	public void savePrefs(View view) {
		// save stuff back into SharedPrefs...
		boolean haveSufficientInfo = true;
		workerVal = workerET.getText().toString();
		if (workerVal.length() == 0) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			String msg = getResources().getString(R.string.please_add_worker);
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
			haveSufficientInfo = false;
		}
		stationVal = stationET.getText().toString();
		if (stationVal.length() == 0) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			String msg = getResources().getString(R.string.please_add_station);
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
			haveSufficientInfo = false;
		}
		facilityVal = facilityET.getText().toString();
		if (facilityVal.length() == 0) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			String msg = getResources().getString(R.string.please_add_facility);
			Toast toast = Toast.makeText(context, msg, duration);
			toast.show();
			haveSufficientInfo = false;
		}	

		if (haveSufficientInfo) {
			editor.putString("WORKER_ID", workerVal);
			editor.putString("STATION_ID", stationVal);
			editor.putString("FACILITY_ID", facilityVal);	
			editor.commit();	
		}
	}
	
	@Override
	protected void onRestart() {
	    super.onStart();  // Always call the superclass method first
	    refreshPrefs();
	}


}