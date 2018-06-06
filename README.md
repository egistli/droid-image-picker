#Droid Image Picker

## What you can complete with this instantly

![Demo](http://i.giphy.com/UPWnlzfcLsT1m.gif)

## Why
In iOS development, allow user to select an image w/wo crop to square is pretty simple: make an instance of UIImagePickerController and it'll handle the flow then give back the image and editoral information via delegate methods.

But on android this is not the case. You have to:

1. Start activities for (with proper extras) selecting image
2. Parse activity results with some strange rules, load bitmap/EXIF and rotate it accordingly
3. Start activity for cropping
4. Parse activity results to get the final bitmap

It's not super hard but it definitely will cost you some time longer than you want to spend.

So there should be a wrapper around this flow to simplify the developement.

## Installation

put this line in you `build.gradle` `dependency` section:

```gradle
compile 'com.egistli.droid-image-picker:droid-image-picker:0.0.2+'
```

And you're good to go.

## Usage

In the actiivty you need to select image, do the following:

```java
private void onSelectImageButtonClick() {
	imagePicker = new ImagePicker(this, this, 480);
	imagePicker.prompt();
}

@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If image picker processes the result, don't go futher.
		if (imagePicker != null && imagePicker.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
}
```

This is pretty much it can be, but there are some behaviour `ImagePicker` need to know, so you have to make the activity implment the `ImagePickerDelegate` like following:

```java
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
	Toast.makeText(this, "Image Picker failed to select image canceled", 	Toast.LENGTH_SHORT).show();
}

@Override
public boolean imagePickerShouldCrop(ImagePicker imagePicker) {
    return true;
}

@Override
public void imagePickerSetUpCropDetail(ImagePicker imagePicker, Crop crop) {
	// Setup crop as documented in android-crop
    crop.asSquare();
    crop.withMaxSize(480, 480);
}
```

This crop part is depending on [android-crop](https://github.com/jdamcd/android-crop) project.
If you want corpping, jut return `true` in `imagePickerShouldCrop` and set up `Crop` object in `imagePickerSetUpCropDetail` with the methods like `crop.asSquare()`, `crop.with*()`.

## Thanks/Reference 
Without these informative posts I won't be able to create this wrapper and upload it to JCenter.

[Gist of ImagePicker] (https://gist.github.com/Mariovc/f06e70ebe8ca52fbbbe2) from [Mariovc](https://gist.github.com/Mariovc)

[How to distribute your own Android library through jCenter and Maven Central from Android Studio - The Cheese Factory Blog](https://inthecheesefactory.com/blog/how-to-upload-library-to-jcenter-maven-central-as-dependency/en)

## License

Apache 2.0
