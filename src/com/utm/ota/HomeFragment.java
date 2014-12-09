package com.utbm.ota;

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

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    public TextView tvInfo;
    public Button bUpdate;
    private String server;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_activity, container, false);

        tvInfo = (TextView) view.findViewById(R.id.home_release);
        String info = "System information:" +
            "\n -> RELEASE:"+Build.VERSION.RELEASE+
            "\n -> CODENAME:"+Build.VERSION.CODENAME+
            "\n -> INCREMENTAL:"+Build.VERSION.INCREMENTAL+
            "\n -> SDK_INT"+Build.VERSION.SDK_INT;
        tvInfo.setText(info);

        bUpdate = (Button) view.findViewById(R.id.button_ota_update);
        bUpdate.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    new HttpRequest(v.getContext()).execute();
                }
            }
        });

        return view;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void reboot(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    public String getServerURI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String hostname = sharedPrefs.getString("preference_server_hostname", "NULL");
        String port  = sharedPrefs.getString("preference_server_port", "NULL");
        return "http://" + hostname + ":" + port;
    }

    private class HttpRequest extends AsyncTask<String, Void, String>{

        private String responseString = null;
        private Context mContext;

        public HttpRequest (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                // execute an HTTP request to get last build number
                response = httpclient.execute(new HttpGet(getServerURI()));
                StatusLine statusLine = response.getStatusLine();

                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    // request successful, then extract response
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (Exception e) {
            }
            return responseString;
        }

        protected void onPostExecute(String result){
            if(responseString != null) {
                if(responseString != Build.VERSION.INCREMENTAL) {
                    tvInfo.setText("New Release available ("+responseString+"). Rebooting...");
                    reboot(mContext);
                } else {
                    tvInfo.setText("Your system is up-to-date.");
                }
            } else {
                tvInfo.setText("Cannot fetch information from the server.");
            }
        }
    }
}
