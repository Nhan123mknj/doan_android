package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.CommentAdapter;
import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Reply;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.CommentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    ImageButton btnBack, btnSend;
    EditText etComment;
    RecyclerView rvComments;

    CommentAdapter commentAdapter;
    CommentViewModel commentViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvComments = findViewById(R.id.rvComments);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        etComment = findViewById(R.id.etComment);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        String articleId = intent.getStringExtra("articleId");
        btnBack.setOnClickListener(v -> finish());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);

        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);

        commentAdapter = new CommentAdapter( new ArrayList<>());
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        commentAdapter.setOnReplyClickListener(comment -> {
            showReplyDialog(articleId, comment.getCommentId());
        });
        
        commentAdapter.setOnCommentActionListener(new CommentAdapter.OnCommentActionListener() {
            @Override
            public void onEditComment(Comments comment) {
                showEditCommentDialog(articleId, comment);
            }

            @Override
            public void onDeleteComment(Comments comment) {
                showDeleteCommentDialog(articleId, comment);
            }

            @Override
            public void onEditReply(Comments comment, Reply reply, int replyPosition) {
                showEditReplyDialog(articleId, comment.getCommentId(), reply);
            }

            @Override
            public void onDeleteReply(Comments comment, Reply reply, int replyPosition) {
                showDeleteReplyDialog(articleId, comment.getCommentId(), reply);
            }
        });
        commentViewModel.getCommentsByArticleId(articleId).observe(this, comments -> {
            if (comments != null) {
                commentAdapter.setComments(comments);
                Log.d("CommentActivity", "Đã tải bình luận: " + comments.size());
            } else {
                commentAdapter.setComments(new ArrayList<>());
                Log.w("CommentActivity", "Không nhận được bình luận");
            }
        });

        btnSend.setOnClickListener(v -> {


            if (user == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Comments comment = new Comments();
            comment.setArticles_id(articleId);
            try {
                comment.setUserId(user.getUid());
                
                // Thiết lập username với fallback
                String username = user.getDisplayName();
                if (username == null || username.trim().isEmpty()) {
                    username = user.getEmail();
                    if (username != null && username.contains("@")) {
                        username = username.substring(0, username.indexOf("@"));
                    }
                }
                comment.setUsername(username != null ? username : "Người dùng");
                
                comment.setProfilePicUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                Log.d("CommentActivity", "Tạo comment với username: " + comment.getUsername() + ", userId: " + comment.getUserId());
            } catch (Exception e) {
                Log.e("CommentActivity", "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }
            comment.setContent(content);
            comment.setTimestamp(System.currentTimeMillis());
            Log.d("CommentActivity", "Gửi bình luận: articleId=" + articleId + ", content=" + content);
            commentViewModel.addComment(articleId, comment);
            etComment.setText("");
            Toast.makeText(this, "Đang gửi bình luận", Toast.LENGTH_SHORT).show();
        });
    }

    private void showReplyDialog(String articleId, String commentId) {
        try {
            Log.d("CommentActivity", "Mở dialog reply cho commentId: " + commentId);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_reply_comment, null);
            EditText etReplyContent = view.findViewById(R.id.etReplyContent);
            Button btnSendReply = view.findViewById(R.id.btnSendReply);
            Button btnCancelReply = view.findViewById(R.id.btnCancelReply);
            
            builder.setView(view);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            
            btnCancelReply.setOnClickListener(v -> dialog.dismiss());
            
            btnSendReply.setOnClickListener(v -> {
                try {
                    String replyContent = etReplyContent.getText().toString().trim();
                    if (replyContent.isEmpty()) {
                        Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Vui lòng đăng nhập để trả lời", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        startActivity(new Intent(this, LoginActivity.class));
                        return;
                    }
                    Reply reply = new Reply();
                    reply.setUserId(user.getUid());
                    
                    // Thiết lập username với fallback
                    String username = user.getDisplayName();
                    if (username == null || username.trim().isEmpty()) {
                        username = user.getEmail();
                        if (username != null && username.contains("@")) {
                            username = username.substring(0, username.indexOf("@"));
                        }
                    }
                    reply.setUsername(username != null ? username : "Người dùng");
                    
                    reply.setProfilePicUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    reply.setContent(replyContent);
                    reply.setTimestamp(System.currentTimeMillis());
                    
                    Log.d("CommentActivity", "Tạo reply với username: " + reply.getUsername() + ", userId: " + reply.getUserId());
                    
                    Log.d("CommentActivity", "Gửi reply: " + replyContent);
                    commentViewModel.addReply(articleId, commentId, reply);
                    dialog.dismiss();
                    Toast.makeText(this, "Đã gửi trả lời", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CommentActivity", "Lỗi khi gửi reply: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi gửi trả lời", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        } catch (Exception e) {
            Log.e("CommentActivity", "Lỗi khi mở dialog reply: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở dialog trả lời: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showEditCommentDialog(String articleId, Comments comment) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_comment, null);
            EditText etEditContent = view.findViewById(R.id.etEditCommentContent);
            Button btnSaveEdit = view.findViewById(R.id.btnSaveEdit);
            Button btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
            
            // Thiết lập nội dung hiện tại
            etEditContent.setText(comment.getContent());
            etEditContent.setSelection(etEditContent.getText().length()); // Đặt cursor ở cuối
            
            builder.setView(view);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            
            btnCancelEdit.setOnClickListener(v -> dialog.dismiss());
            
            btnSaveEdit.setOnClickListener(v -> {
                try {
                    String newContent = etEditContent.getText().toString().trim();
                    if (newContent.isEmpty()) {
                        Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (newContent.equals(comment.getContent())) {
                        Toast.makeText(this, "Nội dung không thay đổi", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    
                    commentViewModel.updateComment(articleId, comment.getCommentId(), newContent);
                    dialog.dismiss();
                    Toast.makeText(this, "Đã cập nhật bình luận", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CommentActivity", "Lỗi khi cập nhật bình luận: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi cập nhật bình luận", Toast.LENGTH_SHORT).show();
                }
            });
            
            dialog.show();
        } catch (Exception e) {
            Log.e("CommentActivity", "Lỗi khi mở dialog edit: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở dialog sửa bình luận", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteCommentDialog(String articleId, Comments comment) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xóa bình luận");
            builder.setMessage("Bạn có chắc chắn muốn xóa bình luận này không? Hành động này không thể hoàn tác.");
            builder.setIcon(R.drawable.ic_delete);
            
            builder.setPositiveButton("Xóa", (dialog, which) -> {
                try {
                    commentViewModel.deleteComment(articleId, comment.getCommentId());
                    Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CommentActivity", "Lỗi khi xóa bình luận: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi xóa bình luận", Toast.LENGTH_SHORT).show();
                }
            });
            
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("CommentActivity", "Lỗi khi mở dialog delete: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở dialog xóa bình luận", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditReplyDialog(String articleId, String commentId, Reply reply) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_reply, null);
            EditText etEditContent = view.findViewById(R.id.etEditReplyContent);
            Button btnSaveEdit = view.findViewById(R.id.btnSaveEditReply);
            Button btnCancelEdit = view.findViewById(R.id.btnCancelEditReply);
            
            // Thiết lập nội dung hiện tại
            etEditContent.setText(reply.getContent());
            etEditContent.setSelection(etEditContent.getText().length()); // Đặt cursor ở cuối
            
            builder.setView(view);
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            
            btnCancelEdit.setOnClickListener(v -> dialog.dismiss());
            
            btnSaveEdit.setOnClickListener(v -> {
                try {
                    String newContent = etEditContent.getText().toString().trim();
                    if (newContent.isEmpty()) {
                        Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (newContent.equals(reply.getContent())) {
                        Toast.makeText(this, "Nội dung không thay đổi", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    
                    commentViewModel.updateReply(articleId, commentId, reply.getReplyId(), newContent);
                    dialog.dismiss();
                    Toast.makeText(this, "Đã cập nhật phản hồi", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CommentActivity", "Lỗi khi cập nhật phản hồi: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi cập nhật phản hồi", Toast.LENGTH_SHORT).show();
                }
            });
            
            dialog.show();
        } catch (Exception e) {
            Log.e("CommentActivity", "Lỗi khi mở dialog edit reply: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở dialog sửa phản hồi", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteReplyDialog(String articleId, String commentId, Reply reply) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xóa phản hồi");
            builder.setMessage("Bạn có chắc chắn muốn xóa phản hồi này không? Hành động này không thể hoàn tác.");
            builder.setIcon(R.drawable.ic_delete);
            
            builder.setPositiveButton("Xóa", (dialog, which) -> {
                try {
                    commentViewModel.deleteReply(articleId, commentId, reply.getReplyId());
                    Toast.makeText(this, "Đã xóa phản hồi", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("CommentActivity", "Lỗi khi xóa phản hồi: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi xóa phản hồi", Toast.LENGTH_SHORT).show();
                }
            });
            
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("CommentActivity", "Lỗi khi mở dialog delete reply: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi mở dialog xóa phản hồi", Toast.LENGTH_SHORT).show();
        }
    }
}