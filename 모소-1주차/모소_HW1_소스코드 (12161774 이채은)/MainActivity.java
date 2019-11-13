package com.example.android.hw1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_IMAGE_CAPTURE){
            if(resultCode==RESULT_OK){
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap)extras.get("data");
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(imageBitmap);
                String path =
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/Share.png";
                File file = new File(path);
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else{
                Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void shareMessageWithIntent(View view) {
// Create the text message with a string
        String path =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/Share.png";
        File file = new File(path);
        Intent sendIntent = new Intent();
        Uri bmpUri = FileProvider.getUriForFile(MainActivity.this, "com.example.myapplication.fileprovider",file);
        sendIntent.setAction(Intent.ACTION_SEND);
        EditText editText = findViewById(R.id.editText);
        String textMessage = editText.getText().toString();
        sendIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
        sendIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        sendIntent.setType("image/png");
        sendIntent.setType("text/plain");

        String title = "Share this text with";
        Intent chooser = Intent.createChooser(sendIntent, title);
// Verify that the intent will resolve to an activity
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }
    }
}

