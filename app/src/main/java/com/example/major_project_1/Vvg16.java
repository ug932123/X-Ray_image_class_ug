package com.example.major_project_1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Vvg16 extends AppCompatActivity {

    Button camera, gallery;
    ImageView imageView;
    TextView result,result_confidence,server_status;
    int imageSize = 224;
    ProgressBar progess;


    // Server IP and endpoint
    final String url_link = "http://national-pleasantly-earwig.ngrok-free.app/vvg16";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vvg16);



        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        result = findViewById(R.id.result);
        result_confidence = findViewById(R.id.result_confidence);
        imageView = findViewById(R.id.imageView);
        progess = findViewById(R.id.progess);



        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        } else {
            // Permission is already granted, proceed with your code
        }



        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    progess.setVisibility(View.VISIBLE);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progess.setVisibility(View.VISIBLE);
                Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryintent, 1);
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your code
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){

            if(requestCode == 3){

                //for camera button
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);
                saveImageToGallery(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                // Step 1: Create a ByteArrayOutputStream to write the bitmap data to
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Step 2: Compress the bitmap into the ByteArrayOutputStream
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                // Step 3: Create a file path for the image (you can choose any file name and extension)
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "title", null);
                // Step 4: Convert the file path to a URI
                Uri imageUri = Uri.parse(path);

                try {
                    uploadImage(imageUri);
                } catch (IOException e) {
                    Toast.makeText(this,"RTE: Image upload not possible.",Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);

                }
            }else{
                //for gallery button
                Uri dat = data.getData();
                Bitmap image = null;

                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    Toast.makeText(this,"RTE: Image cannot be loaded.",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                imageView.setImageBitmap(image);

                try {
                    uploadImage(dat);
                } catch (IOException e) {
                    Toast.makeText(this,"RTE: Image upload not possible.",Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    private void saveImageToGallery(Bitmap image) {

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "IMG_"+timeStamp+".jpg";
        File imageFile = new File(storageDir,filename);

        try {

            FileOutputStream outputStream= new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            outputStream.flush();
            outputStream.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(imageFile));
            sendBroadcast(mediaScanIntent);

            Toast.makeText(this,"Image saved.",Toast.LENGTH_LONG).show();

        }catch (Exception e){
            Toast.makeText(this,"Image cannot be saved.",Toast.LENGTH_LONG).show();
        }
    }





    private void uploadImage(Uri imageUri) throws IOException {

        File file = new File(Objects.requireNonNull(getRealPathFromURI(imageUri)));
        // Set custom timeout values
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Connect timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url(url_link)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = Objects.requireNonNull(response.body()).string();

                    String output,confidence;

                    try {
                        JSONObject jObject = new JSONObject(responseData);
                        output = jObject.getString("output");
                        confidence = jObject.getString("confidence");

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    Log.d("Response", responseData);
                    runOnUiThread(() -> {
                        progess.setVisibility(View.INVISIBLE);
                        Toast.makeText(Vvg16.this, responseData, Toast.LENGTH_SHORT).show();
                        result.setText("Output: "+output);
                        result_confidence.setText("Confidence: "+confidence+"%");
                    });
                }
            }
        });
    }




    private String getRealPathFromURI(Uri contentUri) {

        String[] projection = {MediaStore.Images.Media.DATA};

        @SuppressWarnings("deprecation")
        android.database.Cursor cursor = managedQuery(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        //cursor.close();
        return cursor.getString(column_index);

    }

}