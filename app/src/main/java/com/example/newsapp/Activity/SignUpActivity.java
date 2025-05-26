package com.example.newsapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.newsapp.Helper.ValidationUltils;
import com.example.newsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private EditText fullnameEditText;
    private EditText emailEditText;
    private EditText phonenumberEditText;
    private EditText passwordEditText;
    private EditText passwordRepeatEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private TextView messageError;
    private TextView fullnameError;
    private TextView emailError;
    private TextView passwordError;
    private TextView passwordRepeatError;
    private ImageButton btnVisible;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        firestore  = FirebaseFirestore.getInstance();

        initView();

        btnVisible.setOnClickListener(v -> {
            if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnVisible.setImageResource(R.drawable.ic_visiblity_off);
            } else {

                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnVisible.setImageResource(R.drawable.ic_visiblity_on);
            }
            // Giữ nguyên vị trí con trỏ
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
        signUpButton.setOnClickListener(v -> {
            signUp();
        });
        loginTextView.setOnClickListener(v -> {
            login();
    });
}

    private void initView() {
        fullnameEditText = findViewById(R.id.fullname);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        phonenumberEditText = findViewById(R.id.phone_number);
        signUpButton = findViewById(R.id.btn_sign_up);
        passwordRepeatEditText = findViewById(R.id.password_repeat);
        loginTextView = findViewById(R.id.login);
        messageError = findViewById(R.id.messageError);
        fullnameError = findViewById(R.id.fullnameError);
        emailError = findViewById(R.id.emailError);
        passwordError = findViewById(R.id.passwordError);
        passwordRepeatError = findViewById(R.id.passwordRepeatError);
        btnVisible = findViewById(R.id.btn_visible);
    }

    private void login() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String fullname = fullnameEditText.getText().toString().trim();
        String phonenumber = phonenumberEditText.getText().toString().trim();
        String repeatPassword = passwordRepeatEditText.getText().toString().trim();

        String defaultAvatarUrl = "https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default-avatar-icon-of-social-media-user-vector.jpg";

        fullnameError.setVisibility(View.GONE);
        fullnameError.setText("");
        emailError.setVisibility(View.GONE);
        emailError.setText("");
        passwordError.setVisibility(View.GONE);
        passwordError.setText("");
        passwordRepeatError.setVisibility(View.GONE);


        String fullNameError = ValidationUltils.validateFullName(fullname);
        if (fullNameError != null) {
            fullnameError.setVisibility(View.VISIBLE);
            fullnameError.setText(fullNameError);
            return;
        }


        String emailValidationError = ValidationUltils.validateEmail(email);
        if (emailValidationError != null) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText(emailValidationError);
            return;
        }


        String passwordValidationError = ValidationUltils.validatePassword(password);
        if (passwordValidationError != null) {
            passwordError.setVisibility(View.VISIBLE);
            passwordError.setText(passwordValidationError);
            return;
        }

        // Kiểm tra mật khẩu xác nhận
        String repeatPasswordError = ValidationUltils.validateRepeatPassword(password, repeatPassword);
        if (repeatPasswordError != null) {
            passwordRepeatError.setVisibility(View.VISIBLE);
            passwordRepeatError.setText(repeatPasswordError);
            return;
        }

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", fullname);
                    userMap.put("email", email);
                    userMap.put("phone", phonenumber);
                    userMap.put("avatarUrl ",defaultAvatarUrl);

                    firestore.collection("users").document(uid).set(userMap);

                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    messageError.setVisibility(View.VISIBLE);
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthUserCollisionException e) {

                        messageError.setText("Email này đã được sử dụng");
                    } catch (FirebaseAuthWeakPasswordException e) {

                        messageError.setText("Mật khẩu quá yếu (ít nhất 6 ký tự)");
                    } catch (FirebaseAuthInvalidCredentialsException e) {

                        messageError.setText("Email không hợp lệ");
                    } catch (Exception e) {
                        messageError.setText("Đăng ký thất bại: " + e.getMessage());
                    }
                }
            }
        });

    }

}