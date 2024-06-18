package com.example.major_project_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity {

    Button logincontinue_btn, signup_btn;
    EditText username_view,password_view;
    ProgressBar signup_progress;

    private static final String SERVER_URL = "http://national-pleasantly-earwig.ngrok-free.app/signup"; // Change this to your server URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        logincontinue_btn = findViewById(R.id.login_continue);
        signup_btn = findViewById(R.id.signupButton);

        username_view = findViewById(R.id.username_signup);
        password_view = findViewById(R.id.password_signup);

        signup_progress = findViewById(R.id.signup_progress);


        logincontinue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to switch to MainActivity
                Intent intent = new Intent(Signup.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signup_progress.setVisibility(View.VISIBLE);
                String username = username_view.getText().toString();
                String password = password_view.getText().toString();
                username = username.trim();
                password = password.trim();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS) // Connect timeout
                        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                        .build();

                // Create JSON object with username and password
                JSONObject json = new JSONObject();
                try {
                    json.put("username", username);
                    json.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Signup.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
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

                            // Check if the response contains the "session" key
                            if (jsonResponse.has("Session")) {
                                String sessionKey = jsonResponse.getString("Session");
                                Log.d("myTag", "Session Key: " + sessionKey);

                                if(sessionKey.equals("True"))
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Signup.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    // Intent to switch to MainActivity
                                    Intent intent = new Intent(Signup.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else if (sessionKey.equals("existing_user")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            signup_progress.setVisibility(View.INVISIBLE);
                                            Toast.makeText(Signup.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else if (sessionKey.equals("missing_value")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            signup_progress.setVisibility(View.INVISIBLE);
                                            Toast.makeText(Signup.this, "Username and password are required", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Signup.this, "No session key", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Signup.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });







            }
        });



    }  // on create end
}