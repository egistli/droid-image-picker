package com.egistli.droidimagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the entry point of ImagePicker library
 * Created by egistli on 2016/5/11.
 */
public class ImagePicker {
    private static final int REQUEST_PICK_IMAGE = 0;
    private static final int REQUEST_CROP_IMAGE = 1;

    private static final String TEMP_IMAGE_NAME = "image-picker-temp";

    final Activity activity;
    final Context context;
    private final ImagePickerDelegate callback;
    private final int maxLength;
    private Object promptSource;

    public ImagePicker(final Activity activity, final ImagePickerDelegate callback, final int maxLength) {
        this.activity = activity;
        this.context = activity;
        this.callback = callback;
        this.maxLength = maxLength;
    }

    public void promptWithFragment(Fragment fragment) {
        promptSource = fragment;
        final List<Intent> intentList = getIntentListOfPromptSourceChooser();
        if (intentList.size() > 0) {
            final Intent chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));
            fragment.startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
        } else {
            callback.imagePickerDidFailToSelectImage(this, ImagePickerError.NO_INTENTS_AVAILABLE);
        }
    }

    public void promptWithActivity(Activity activity) {
        promptSource = activity;
        final List<Intent> intentList = getIntentListOfPromptSourceChooser();
        if (intentList.size() > 0) {
            final Intent chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));
            activity.startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE);
        } else {
            callback.imagePickerDidFailToSelectImage(this, ImagePickerError.NO_INTENTS_AVAILABLE);
        }
    }

    /**
     * This is to process activity result for activities
     * Just delegate the handling in activity by calling
     * imagePicker.handleActivityResult(), when it's handled
     * the result will be `true`, otherwise it'll be `false`.
     */
    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            callback.imagePickerDidCancel(this);
            return true;
        }

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            final Bitmap bitmap = getImageFromResult(resultCode, data);
            final String outputFilePath = context.getExternalCacheDir() + File.separator + "output.jpg";
            BitmapUtility.writeBitmap(Bitmap.CompressFormat.JPEG, bitmap, outputFilePath);

            if (!callback.imagePickerShouldCrop(this)) {
                notifyFinish(bitmap);
            } else {
                final String croppedOutputFilePath = context.getExternalCacheDir() + File.separator + "cropped-output.jpg";
                final Crop crop = Crop.of(Uri.fromFile(new File(outputFilePath)), Uri.fromFile(new File(croppedOutputFilePath)));
                callback.imagePickerSetUpCropDetail(this, crop);
                if (promptSource instanceof Activity) {
                    crop.start(activity, REQUEST_CROP_IMAGE);
                } else if (promptSource instanceof Fragment) {
                    crop.start(context, (Fragment)promptSource, REQUEST_CROP_IMAGE);
                }
            }
            return true;
        } else if (requestCode == REQUEST_CROP_IMAGE && resultCode == Activity.RESULT_OK) {
            final String croppedOutputFilePath = context.getExternalCacheDir() + File.separator + "cropped-output.jpg";
            final Bitmap croppedBitmap = BitmapUtility.decodeBitmap(context.getContentResolver(), Uri.fromFile(new File(croppedOutputFilePath)), 1);
            notifyFinish(croppedBitmap);
            return true;
        }
        return false;
    }

    private void notifyFinish(Bitmap bitmap) {
        if (bitmap != null) {
            callback.imagePickerDidSelectImage(this, bitmap);
        } else {
            callback.imagePickerDidFailToSelectImage(this, ImagePickerError.FAIL_TO_GENERATE_BITMAP);
        }
    }

    private List<Intent> getIntentListOfPromptSourceChooser() {
        List<Intent> intentList = new ArrayList<>();

        final Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentList = addRelatedIntentToList(intentList, pickIntent);

        final Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
        intentList = addRelatedIntentToList(intentList, takePhotoIntent);

        return intentList;
    }

    /**
     * Internal Helpers
     */

    private Bitmap getImageFromResult(final int resultCode, final Intent imageReturnedIntent) {
        Bitmap bitmap = null;
        File imageFile = getTempFile();
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));

            if (isCamera) {
                selectedImage = Uri.fromFile(imageFile);
            } else {
                selectedImage = imageReturnedIntent.getData();
            }

            bitmap = getImageResized(selectedImage);
            int rotation = getRotation(selectedImage, isCamera);
            bitmap = BitmapUtility.rotate(bitmap, rotation);
        }
        return bitmap;
    }

    private Bitmap getImageResized(final Uri selectedImage) {
        Bitmap bitmap = BitmapUtility.decodeBitmap(context.getContentResolver(), selectedImage, 1);
        int width, height;
        if (bitmap.getWidth() >= maxLength) {
            width = maxLength;
            height = bitmap.getHeight() * width / bitmap.getWidth();
        } else {
            height = maxLength;
            width = bitmap.getWidth() * height / bitmap.getHeight();
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private int getRotation(Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(imageUri);
        } else {
            rotation = getRotationFromGallery(imageUri);
        }
        return rotation;
    }

    private int getRotationFromCamera(final Uri imageFile) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public int getRotationFromGallery(final Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private List<Intent> addRelatedIntentToList(List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    private File getTempFile() {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }
}
