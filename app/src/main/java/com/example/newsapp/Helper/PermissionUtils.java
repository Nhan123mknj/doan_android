package com.example.newsapp.Helper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class PermissionUtils {
    public static final int MY_REQUEST_CODE = 1001;

    // Hàm mở thư viện ảnh
    public static void openGallery(Activity activity, int requestCode) {
        Log.d("PermissionUtils", "Mở thư viện ảnh.");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    // Hàm kiểm tra và yêu cầu quyền
    public static void requestGalleryPermission(Activity activity) {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? android.Manifest.permission.READ_MEDIA_IMAGES
                : android.Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d("PermissionUtils", "Quyền đã được cấp, mở thư viện ảnh.");
            openGallery(activity, MY_REQUEST_CODE);
        } else {
            Log.d("PermissionUtils", "Quyền chưa được cấp, yêu cầu quyền.");
            ActivityCompat.requestPermissions(activity, new String[]{permission}, MY_REQUEST_CODE);
        }
    }

    public static void handlePermissionResult(int requestCode, @NonNull int[] grantResults, Activity activity) {
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(activity, MY_REQUEST_CODE);
            }
        }
    }
}
