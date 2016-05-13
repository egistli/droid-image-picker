package com.egistli.droidimagepicker;

import android.graphics.Bitmap;

import com.soundcloud.android.crop.Crop;

public interface ImagePickerDelegate {
    void imagePickerDidCancel(final ImagePicker imagePicker);

    void imagePickerDidSelectImage(final ImagePicker imagePicker, final Bitmap bitmap);

    void imagePickerDidFailToSelectImage(final ImagePicker imagePicker, final ImagePickerError error);

    boolean imagePickerShouldCrop(final ImagePicker imagePicker);

    /**
     * Set up crop behavior by calling `with*` methods on `crop` object.
     * You don't need to call `crop.startActivity*`, it'll be called by ImagePicker
     * @param imagePicker
     * @param crop
     */
    void imagePickerSetUpCropDetail(final ImagePicker imagePicker, final Crop crop);
}
