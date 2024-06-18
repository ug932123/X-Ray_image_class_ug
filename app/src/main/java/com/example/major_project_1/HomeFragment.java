package com.example.major_project_1;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends Fragment {

    Button b7,b3,vgg,resnet,about,logout;
    private static final String SERVER_URL = "http://national-pleasantly-earwig.ngrok-free.app/logout"; // Change this to your server URL

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        b7 = view.findViewById(R.id.home_efficientnetb7);
        b3 = view.findViewById(R.id.home_efficientnetb3);
        vgg = view.findViewById(R.id.home_vgg16);
        resnet = view.findViewById(R.id.home_resnet50v2);
        about = view.findViewById(R.id.home_about);
        logout = view.findViewById(R.id.home_logout);

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),Efficientnetb7.class);
                startActivity(i);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),Efficientnetb3.class);
                startActivity(i);
            }
        });
        vgg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),Vvg16.class);
                startActivity(i);
            }
        });
        resnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),Resnet50v2.class);
                startActivity(i);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(),AboutUs.class);
                startActivity(i);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Failed to connect to server", Toast.LENGTH_SHORT).show();
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
                            String output = jObject.getString("message");
                            Log.d("myTag", responseData);
                            // Check if the response contains the "Server" key
                            if (jsonResponse.has("message")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Intent to switch to MainActivity
                                Intent intent = new Intent(getActivity(), Login.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Server key not found in response", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });



        return view;
    }
}