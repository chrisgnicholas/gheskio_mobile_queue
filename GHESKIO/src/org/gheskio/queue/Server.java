package org.gheskio.queue;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class Server extends Activity {
	
	public EditText URL_ET = null;
	public static String URL_Val = null;
	
	public EditText passwdET = null;
	public static String passwdVal = null;;
		
	public EditText userET = null;
	public static String userVal = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		refreshVals();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server, menu);
		return true;
	}
	
	
    @Override
    protected void onRestart() {
        super.onStart();
        // The activity is about to become visible.
        refreshVals();
    }
	
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        refreshVals();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        refreshVals();
    }
	
	public void refreshVals(){

		URL_Val = MainActivity.sharedPref.getString("URL", "http://maps.geography.uc.edu/cgi-bin/gheskio_upload.sh");
		userVal = MainActivity.sharedPref.getString("USERVAL", "GHESKIO");
		passwdVal = MainActivity.sharedPref.getString("UPLOAD_PW", "stop_HIV_now");
				
		URL_ET = (EditText)findViewById(R.id.editText2);		
		userET = (EditText)findViewById(R.id.editText1);
		passwdET = (EditText)findViewById(R.id.editText3);
		
		URL_ET.setText(URL_Val);
		userET.setText(userVal);
		passwdET.setText(passwdVal);
		
	}
	
	public void doSave(View view){
		
		URL_ET = (EditText)findViewById(R.id.editText2);		
		userET = (EditText)findViewById(R.id.editText1);
		passwdET = (EditText)findViewById(R.id.editText3);	
		
		MainActivity.editor.putString("URL", URL_ET.getText().toString() );
		MainActivity.editor.putString("USERVAL", userET.getText().toString());
		MainActivity.editor.putString("UPLOAD_PW", passwdET.getText().toString());
		MainActivity.editor.commit();		
	}

}
