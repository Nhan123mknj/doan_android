package com.example.newsapp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cloudinary.android.MediaManager;
import com.example.newsapp.Adapter.CategoriesAdapter;
import com.example.newsapp.Helper.PermissionUtils;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.Repository.ArticlesRepository;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.CategoriesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateArticlesActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteCategory;
    private EditText etTitle, editSummary, editContent;
    private String selectedCategoryId;
    private CategoriesViewModel categoriesViewModel;
    private ArticlesViewModel articlesViewModel;
    private LinearLayout thumbnailImage;
    private ImageView imageView;
    private ImageButton btnBack;
    private Button btnUpdate;
    private Uri selectedImageUri = null;
    private String articlesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_articles);

        initView();

        Intent intent = getIntent();
        articlesId = intent.getStringExtra("articlesId");
        if (articlesId == null) {
            Toast.makeText(this, "Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCategories();

        loadArticleDetails();

        thumbnailImage.setOnClickListener(v -> PermissionUtils.requestGalleryPermission(this));

        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateArticles());
    }

    private void initView() {
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        etTitle = findViewById(R.id.etTitle);
        editSummary = findViewById(R.id.editSummary);
        editContent = findViewById(R.id.edtContent);
        thumbnailImage = findViewById(R.id.addThumbnail);
        imageView = findViewById(R.id.imgCover);
        btnUpdate = findViewById(R.id.btnPublish);
        btnBack = findViewById(R.id.btnBack);

        articlesViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ArticlesViewModel.class);
        categoriesViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(CategoriesViewModel.class);
    }

    private void loadCategories() {
        categoriesViewModel.getAllCategories().observe(this, categories -> {
            if (categories == null) {
                return;
            }
            List<String> categoryNames = new ArrayList<>();
            Map<String, String> categoryMap = new HashMap<>();
            for (Categories category : categories) {
                categoryNames.add(category.getName());
                categoryMap.put(category.getName(), category.getCategoryId());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
            autoCompleteCategory.setAdapter(adapter);

            autoCompleteCategory.setOnClickListener(v -> autoCompleteCategory.showDropDown());

            autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> {
                String selectedCategoryName = categoryNames.get(position);
                selectedCategoryId = categoryMap.get(selectedCategoryName);
            });
        });
    }

    private void loadArticleDetails() {
        articlesViewModel.getArticleById(articlesId).observe(this, article -> {
            if (article == null) {
                Toast.makeText(this, "Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Hiển thị thông tin bài viết
            etTitle.setText(article.getTitle());
            editSummary.setText(article.getSummary());
            editContent.setText(article.getContent());
            selectedCategoryId = article.getCategoryId();
            autoCompleteCategory.setText(article.getCategoryName(), false);

            // Hiển thị ảnh bài viết
            if (article.getUrlToImage() != null) {
                Picasso.get().load(article.getUrlToImage()).into(imageView);
            }
        });
    }

    private void updateArticles() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để cập nhật bài viết!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String summary = editSummary.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty() || summary.isEmpty() || content.isEmpty() || selectedCategoryId == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Articles updatedArticle = new Articles();
        updatedArticle.setAuthor(user.getUid());
        updatedArticle.setArticleId(articlesId);
        updatedArticle.setTitle(title);
        updatedArticle.setSummary(summary);
        updatedArticle.setContent(content);
        updatedArticle.setCategoryId(selectedCategoryId);

        articlesViewModel.getArticleById(articlesId).observe(this, currentArticle -> {
            if (currentArticle == null) {
                Toast.makeText(this, "Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
                return;
            }

            String oldPublicId = currentArticle.getImagePublicId();
            Uri newImageUri = selectedImageUri;

            if (newImageUri != null) {

                articlesViewModel.deleteImage(oldPublicId).observe(this, isDeleted -> {
                    if (isDeleted) {
                        articlesViewModel.uploadImage(newImageUri, new ArticlesRepository.OnImageUploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl, String publicId) {
                                updatedArticle.setUrlToImage(imageUrl);
                                updatedArticle.setImagePublicId(publicId);
                                saveArticle(updatedArticle);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(UpdateArticlesActivity.this, "Lỗi khi upload ảnh mới: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa ảnh cũ!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {

                updatedArticle.setUrlToImage(currentArticle.getUrlToImage());
                updatedArticle.setImagePublicId(currentArticle.getImagePublicId());
                saveArticle(updatedArticle);
            }
        });
    }

    private void saveArticle(Articles article) {
        articlesViewModel.updateArticle(article);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            thumbnailImage.setVisibility(View.GONE);
        }
    }
}