package com.example.newsapp.Helper;

import android.app.Activity;
import android.content.Intent;

public class GalleryUtils {
    public static void openGallery(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
        
    }
}
