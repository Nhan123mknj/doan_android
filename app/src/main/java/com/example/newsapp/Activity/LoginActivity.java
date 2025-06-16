package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.api.exceptions.ApiException;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton,googleLoginButton;
    private TextView registerTextView;
    private GoogleSignInClient googleSignInClient;

    private  TextView messageError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();

        loginButton.setOnClickListener(v -> {
            login();
        });
        googleLoginButton.setOnClickListener(v -> {
           GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 123);
        });
        registerTextView.setOnClickListener(v -> {
            register();
        });
    }

    private void initViews() {
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.btn_login);
        registerTextView = findViewById(R.id.sign_up);
        messageError = findViewById(R.id.messageError);
        googleLoginButton = findViewById(R.id.btn_google);
    }

    private void register() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if(email.isEmpty()||password.isEmpty()){
            messageError.setVisibility(View.VISIBLE);
            messageError.setText("Vui lòng nhập đầy đủ thông tin");
        }else{
           mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isComplete()) {
                       FirebaseMessaging.getInstance().getToken()
                               .addOnCompleteListener(tokenTask -> {
                                   if (tokenTask.isSuccessful()) {
                                       String token = tokenTask.getResult();
                                       String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                       FirebaseFirestore.getInstance().collection("users")
                                               .document(userId)
                                               .update("fcmToken", token);
                                   }
                               });
                       Intent intent1 = new Intent(LoginActivity.this, HomeActivity.class);
                       startActivity(intent1);
                       finish();
                   }else {
                       messageError.setVisibility(View.VISIBLE);
                       messageError.setText("Đăng nhập thất bại");
                   }
               }
           });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        mAuth.signInWithCredential(credential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> authTask) {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();

                                            // Tạo đối tượng user
                                            Users users = new Users();
                                            users.setUserId(user.getUid());
                                            users.setEmail(user.getEmail());
                                            users.setName(user.getDisplayName());
                                            users.setAvatarUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);


                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("users").document(user.getUid())
                                                    .get()
                                                    .addOnSuccessListener(documentSnapshot -> {
                                                        if (!documentSnapshot.exists()) {

                                                            db.collection("users").document(user.getUid())
                                                                    .set(users)
                                                                    .addOnSuccessListener(aVoid ->
                                                                            Log.d("Firestore", "Đã lưu người dùng mới"))
                                                                    .addOnFailureListener(e ->
                                                                            Log.e("Firestore", "Lỗi khi lưu người dùng", e));
                                                        } else {
                                                            Log.d("Firestore", "Người dùng đã tồn tại, không ghi đè");
                                                        }

                                                        // Chuyển sang màn hình chính sau khi xử lý xong
                                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(LoginActivity.this, "Lỗi kiểm tra người dùng", Toast.LENGTH_SHORT).show();
                                                    });

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Google Sign-in thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không thể đăng nhập bằng Google", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
