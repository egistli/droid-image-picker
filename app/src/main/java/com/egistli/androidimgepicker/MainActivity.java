package com.egistli.androidimgepicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.egistli.droidimagepicker.ImagePicker;
import com.egistli.droidimagepicker.ImagePickerError;
import com.egistli.droidimagepicker.ImagePickerDelegate;
import com.soundcloud.android.crop.Crop;

public class MainActivity extends AppCompatActivity implements ImagePickerDelegate {

    private ImagePicker imagePicker;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePicker != null && imagePicker.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void imagePickerDidCancel(ImagePicker imagePicker) {
        Toast.makeText(this, "Image Picker is canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void imagePickerDidSelectImage(ImagePicker imagePicker, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void imagePickerDidFailToSelectImage(ImagePicker imagePicker, ImagePickerError error) {
        Toast.makeText(this, "Image Picker failed to select image canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean imagePickerShouldCrop(ImagePicker imagePicker) {
        return true;
    }

    @Override
    public void imagePickerSetUpCropDetail(ImagePicker imagePicker, Crop crop) {
        crop.asSquare();
        crop.withMaxSize(480, 480);
    }

    public void onPickButtonClick(View view) {
        this.imagePicker = new ImagePicker(this, this, 480);
        imagePicker.prompt();
    }
}
