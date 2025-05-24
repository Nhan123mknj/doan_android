package com.example.newsapp.Helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.newsapp.Activity.LoginActivity;
import com.example.newsapp.R;

public class DialogLogin {

    public static void openDialogLogin(Context context,int gravity,Runnable onLoginRedirect){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_login);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        //Vi tri cua dialog
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams winLayoutParams = window.getAttributes();
        winLayoutParams.gravity = gravity;
        window.setAttributes(winLayoutParams);

        if(Gravity.BOTTOM == gravity){
            dialog.setCancelable(true);
        }else {
            dialog.setCancelable(false);
        }

        Button btnNo = dialog.findViewById(R.id.btn_no);
        Button btnAccept = dialog.findViewById(R.id.btn_accept);
        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btnAccept.setOnClickListener(v -> {
            dialog.dismiss();
            onLoginRedirect.run();
        });

        dialog.show();
    }
}
