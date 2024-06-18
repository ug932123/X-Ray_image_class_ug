package com.example.major_project_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;


    Toolbar toolbar;
    Fragment f =null; //used to keep track for the current fragment
    private static final String SERVER_URL = "http://national-pleasantly-earwig.ngrok-free.app/logout"; // Change this to your server URL


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);



        //setup toolbar
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,
                toolbar,R.string.open,R.string.close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white)); //change color of toogle
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setTitle("X-Cam");
        drawerLayout.openDrawer(GravityCompat.START);
        navigationView.setItemIconTintList(null);
        replaceloadFragment(new HomeFragment());



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id ==R.id.home)
                {
                    f = new HomeFragment(); //update current fragment
                    replaceloadFragment(new HomeFragment());
                }
                else if(id == R.id.efficientnetb7)
                {
                    Intent i = new Intent(getApplicationContext(),Efficientnetb7.class);
                    startActivity(i);
                }

                else if(id == R.id.vvg)
                {
                    Intent x = new Intent(getApplicationContext(),Vvg16.class);
                    startActivity(x);
                }
                else if(id == R.id.resnet)
                {
                    Intent x = new Intent(getApplicationContext(),Resnet50v2.class);
                    startActivity(x);
                }
                else if(id == R.id.efficientnetb3)
                {
                    Intent x = new Intent(getApplicationContext(),Efficientnetb3.class);
                    startActivity(x);
                }
                else if (id == R.id.about) {
                    Intent j = new Intent(getApplicationContext(),AboutUs.class);
                    startActivity(j);
                }
                else if (id == R.id.Logout_menu) {
//                    Intent j = new Intent(getApplicationContext(),Login.class);
//                    startActivity(j);
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
                                    Toast.makeText(MainActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    // Intent to switch to MainActivity
                                    Intent intent = new Intent(MainActivity.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Server key not found in response", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });




                } //else if ends here

                drawerLayout.closeDrawer(GravityCompat.START);


                return true;
            }
        });






    }

    private void replaceloadFragment( Fragment fragment)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft  = fm.beginTransaction();
        ft.replace(R.id.container,fragment );//container is place where fragment is replaced
        ft.commit();
    }
}