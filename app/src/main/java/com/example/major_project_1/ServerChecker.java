package com.example.major_project_1;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerChecker extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "ServerChecker";

    private ServerCheckListener listener;

    public interface ServerCheckListener {
        void onServerStatusChecked(boolean isServerUp);
    }

    public void setServerCheckListener(ServerCheckListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        if (urls == null || urls.length == 0) {
            return false;
        }

        String urlString = urls[0];
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            int responseCode = connection.getResponseCode();
            return (responseCode == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            Log.e(TAG, "Error checking server status", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isServerUp) {
        if (listener != null) {
            listener.onServerStatusChecked(isServerUp);
        }
    }
}
