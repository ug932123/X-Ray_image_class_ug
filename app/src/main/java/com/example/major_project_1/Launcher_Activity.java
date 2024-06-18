package com.example.major_project_1;
//final change 15/05/24
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Launcher_Activity extends AppCompatActivity {

    private static final String SERVER_URL = "http://national-pleasantly-earwig.ngrok-free.app/serverstat"; // Change this to your server URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Send request to Flask server
        // Set custom timeout values
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Connect timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                .build();
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Launcher_Activity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject jObject = new JSONObject(responseData);
                    String output = jObject.getString("Server");
                    Log.d("myTag", responseData);
                    // Check if the response contains the "Server" key
                    if (jsonResponse.has("Server")) {
                        // Intent to switch to MainActivity
                        Intent intent = new Intent(Launcher_Activity.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Launcher_Activity.this, "Server key not found in response", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Launcher_Activity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    } //on create end
}
