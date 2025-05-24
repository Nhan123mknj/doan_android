package com.example.newsapp.Activity;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.CategoriesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddArticlesActivity extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteCategory;
    private EditText etTitle, editSummary,editContent;
    private String selectedCategoryId;
    private CategoriesViewModel categoriesViewModel;
    private ArticlesViewModel articlesViewModel;
    private LinearLayout thumbnailImage;
    private ImageView imageView;
    private Button btnPublish;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_articles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        categoriesViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(CategoriesViewModel.class);

        articlesViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ArticlesViewModel.class);
        categoriesViewModel.getAllCategories().observe(this, categories -> {
            if(categories == null){
                Log.d("AddArticlesActivity", "Categories is null");
                return;
            }
            List<String> categoryNames = new ArrayList<>();
            Map<String,String> categoryMap = new HashMap<>();
            for(Categories categorie : categories){
                 categoryNames.add(categorie.getName());
                 categoryMap.put(categorie.getName(),categorie.getCategoryId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
            autoCompleteCategory.setAdapter(adapter);

            autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> {
                String selectedCategoryName = categoryNames.get(position);
                selectedCategoryId = categoryMap.get(selectedCategoryName);
            });
        });



        FirebaseAuth user = FirebaseAuth.getInstance();

        String userId = user.getCurrentUser().getUid();


        thumbnailImage.setOnClickListener(v -> {
            PermissionUtils.requestGalleryPermission(this);
        });

        imageView.setImageURI(selectedImageUri);

        btnPublish.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String summary = editSummary.getText().toString();
            String content = editContent.getText().toString();
            if (title.isEmpty() || summary.isEmpty() || content.isEmpty() || selectedCategoryId == null) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            Articles articles = new Articles();
            articles.setTitle(title);
            articles.setSummary(summary);
            articles.setContent(content);
            articles.setCategoryId(selectedCategoryId);
            articles.setAuthor(userId);
            articles.setFeatured(false);
            if(selectedImageUri != null){
                MediaManager.get().upload(selectedImageUri)
                        .option("folder", "articles_thumb")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {

                            }
                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {

                            }
                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String imageUrl = (String) resultData.get("secure_url");
                                articles.setUrlToImage(imageUrl);
                                runOnUiThread(() -> {
                                    articlesViewModel.addArticle(articles);
                                    Toast.makeText(AddArticlesActivity.this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                runOnUiThread(()-> Toast.makeText(AddArticlesActivity.this, "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {

                            }
                        }).dispatch();
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
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(uri);
        }
    }

    private void initView() {
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        etTitle = findViewById(R.id.etTitle);
        editSummary = findViewById(R.id.editSummary);
        editContent = findViewById(R.id.edtContent);
        thumbnailImage = findViewById(R.id.addThumbnail);
        imageView = findViewById(R.id.imgCover);
        btnPublish = findViewById(R.id.btnPublish);
    }
}