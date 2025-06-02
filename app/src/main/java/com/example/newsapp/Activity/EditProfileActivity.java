package com.example.newsapp.Activity;

import static com.example.newsapp.Activity.MainActivity.MY_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.newsapp.Helper.PermissionUtils;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.UsersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileActivity extends AppCompatActivity {

    ImageButton btnDenied, btnAccept;
    EditText username, email, description;
    CircleImageView profileImage;
    UsersViewModel usersViewModel;
    private String userId;
    private Users currentUser;
    private Uri selectedImageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
      
        initView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        btnDenied.setOnClickListener(v -> finish());

        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        if (user != null) {
             userId = user.getUid();
            usersViewModel.getUserById(userId).observe(this, userObj -> {
                if (userObj != null) {
                    username.setText(userObj.getName());
                    email.setText(userObj.getEmail());
                    description.setText(userObj.getDescription());
                    if (userObj.getAvatarUrl() != null) {
                        Picasso.get()
                                .load(userObj.getAvatarUrl())
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(profileImage);
                    }
                }
            });
        }

        profileImage.setOnClickListener(v -> {
            Toast.makeText(this, "Đã click ảnh", Toast.LENGTH_SHORT).show();
            PermissionUtils.requestGalleryPermission(this);
        });

        btnAccept.setOnClickListener(v -> {
            String newName = username.getText().toString().trim();
            String newEmail = email.getText().toString().trim();
            String newDescription = description.getText().toString().trim();

            Users updatedUser = new Users();
            updatedUser.setUserId(userId);
            updatedUser.setName(newName);
            updatedUser.setEmail(newEmail);
            updatedUser.setDescription(newDescription);
            if (currentUser != null) {
                updatedUser.setAvatarUrl(currentUser.getAvatarUrl());
            }
            if (selectedImageUri != null) {
                MediaManager.get().upload(selectedImageUri)
                        .option("folder", "avatars")
                        .callback(new UploadCallback() {
                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String imageUrl = (String) resultData.get("secure_url");
                                updatedUser.setAvatarUrl(imageUrl);
                                runOnUiThread(() -> {
                                    usersViewModel.UpdateUser(userId, updatedUser);
                                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                            @Override public void onError(String requestId, ErrorInfo error) {
                                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                            }
                            @Override public void onStart(String requestId) {}
                            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                            @Override public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();
            } else {
                usersViewModel.UpdateUser(userId, updatedUser);
                Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.handlePermissionResult(requestCode, grantResults, this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtils.MY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            selectedImageUri = uri;
            profileImage.setImageURI(uri);
        }
    }
    private void initView() {
        btnDenied = findViewById(R.id.btnDenied);
        btnAccept = findViewById(R.id.btnAccept);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        description = findViewById(R.id.description);
        profileImage = findViewById(R.id.profileImage);
    }
}