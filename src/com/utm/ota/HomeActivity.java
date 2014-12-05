package com.utm.ota;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.utbm.ota.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends Activity{

	private static final String TAG = "UTBM OTA";
	private String server;
	
	private TextView tvInfo;
	private Button bUpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity);	
		
		tvInfo = (TextView) findViewById(R.id.home_release);
		String info = "\nRELEASE:"+Build.VERSION.RELEASE+
				"\n CODENAME:"+Build.VERSION.CODENAME+
				"\n INCREMENTAL:"+Build.VERSION.INCREMENTAL+
				"\n SDK_INT"+Build.VERSION.SDK_INT;
		tvInfo.setText(info);
		
		bUpdate = (Button) findViewById(R.id.button_ota_update);
		bUpdate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Check fo update!");
			}
			
		});
		
		SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
		String hostname = sharedPrefs.getString("preference_server_hostname", "NULL");
		String port  = sharedPrefs.getString("preference_server_port", "NULL");
		server = hostname+":"+port;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case(R.id.settings):
			return startSettings(); 
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
	public boolean startSettings(){
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		return true;
	}

	private class HttpRequest extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(server));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        } catch (IOException e) {
	        }
	        return responseString;
		}
		
		protected void onPostExecute(String result){
			String info = tvInfo.getText().toString();
			info+="\n new RELEASE:"+result;
			tvInfo.setText(info);
		}
	}
}
