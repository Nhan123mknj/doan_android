package com.example.newsapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.newsapp.Helper.ValidationUltils;
import com.example.newsapp.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassActivity extends AppCompatActivity {
    Button btnChangePass;
    EditText oldPass, newPass;
    ImageButton btnBack;
   TextView passwordError,passwordNewError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
        btnChangePass.setOnClickListener(v -> {
            changePass();
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void changePass() {
        String oldPassword = oldPass.getText().toString().trim();
        String newPassword = newPass.getText().toString().trim();

        String passwordErrorText = ValidationUltils.validatePassword(newPassword);

        if (passwordErrorText != null) {
            passwordNewError.setVisibility(View.VISIBLE);
            passwordNewError.setText(passwordErrorText);
            return;
        } else {
            passwordError.setVisibility(View.GONE);
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Không thể lấy email của người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }


        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi đổi mật khẩu: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                passwordError.setVisibility(View.VISIBLE);
                passwordError.setText("Mật khẩu cũ không chính xác!");
            }
        });
    }

    private void initView() {
        btnChangePass = findViewById(R.id.btnChangePass);
        oldPass = findViewById(R.id.oldPass);
        newPass = findViewById(R.id.newPass);
        passwordError = findViewById(R.id.passwordError);
        passwordNewError = findViewById(R.id.passwordNewError);
        btnBack = findViewById(R.id.btnBack);
    }
}