package com.example.newsapp.Helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;

public class DialogReport {
    
    public interface OnReportSubmitListener {
        void onReportSubmit(String reason, String description);
    }
    
    public static void showReportDialog(Context context, Articles article, OnReportSubmitListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_report_article);
        
        // Find views
        Spinner spinnerReasons = dialog.findViewById(R.id.spinnerReasons);
        EditText editTextDescription = dialog.findViewById(R.id.editTextDescription);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        
        // Setup spinner with reasons
        String[] reasons = {
            "Chọn lý do báo cáo",
            "Nội dung không phù hợp",
            "Thông tin sai sự thật",
            "Nội dung spam",
            "Ngôn từ thù địch",
            "Xúi giục bạo lực",
            "Bản quyền",
            "Khác"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, reasons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReasons.setAdapter(adapter);
        
        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Submit button
        btnSubmit.setOnClickListener(v -> {
            int selectedPosition = spinnerReasons.getSelectedItemPosition();
            if (selectedPosition == 0) {
                Toast.makeText(context, "Vui lòng chọn lý do báo cáo", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String selectedReason = reasons[selectedPosition];
            String description = editTextDescription.getText().toString().trim();
            
            listener.onReportSubmit(selectedReason, description);
            dialog.dismiss();
        });
        
        // Show dialog
        dialog.show();
        
        // Set dialog size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
} 