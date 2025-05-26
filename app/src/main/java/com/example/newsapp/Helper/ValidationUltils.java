package com.example.newsapp.Helper;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUltils {
    public static String validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "Vui lòng nhập họ tên";
        }
        return null;
    }


    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Vui lòng nhập email";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Email không hợp lệ";
        }
        return null;
    }


    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Vui lòng nhập mật khẩu";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null;
    }


    public static String validateRepeatPassword(String password, String repeatPassword) {
        if (TextUtils.isEmpty(repeatPassword)) {
            return "Vui lòng nhập lại mật khẩu";
        }
        if (!password.equals(repeatPassword)) {
            return "Mật khẩu không khớp";
        }
        return null;
    }


    public static String validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "Vui lòng nhập số điện thoại";
        }
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            return "Số điện thoại không hợp lệ";
        }
        return null;
    }
}
