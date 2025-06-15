package com.example.newsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Model.Reply;
import com.example.newsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private final List<Reply> replyList;
    private OnReplyActionListener replyActionListener;

    public interface OnReplyActionListener {
        void onEditReply(Reply reply, int position);
        void onDeleteReply(Reply reply, int position);
    }

    public ReplyAdapter( List<Reply> replyList) {
        this.replyList = replyList;
    }

    public void setOnReplyActionListener(OnReplyActionListener listener) {
        this.replyActionListener = listener;
    }

    @NonNull
    @Override
    public ReplyAdapter.ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.ReplyViewHolder holder, int position) {
        try {
            if (replyList == null || position >= replyList.size()) {
                return;
            }
            Reply reply = replyList.get(position);
            if (reply == null) {
                return;
            }
            
            holder.tvNameReply.setText(reply.getUsername() != null ? reply.getUsername() : "");
            holder.tvReply.setText(reply.getContent() != null ? reply.getContent() : "");
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.tvTimeReply.setText(sdf.format(new Date(reply.getTimestamp())));
            } catch (Exception e) {
                holder.tvTimeReply.setText("");
            }
            
            String profilePicUrl = reply.getProfilePicUrl();
            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Picasso.get().load(profilePicUrl).placeholder(R.drawable.avatar).into(holder.avtReply);
            } else {
                holder.avtReply.setImageResource(R.drawable.avatar);
            }

            // Hiển thị menu button nếu là reply của user hiện tại
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (currentUserId != null && currentUserId.equals(reply.getUserId())) {
                holder.btnReplyMenu.setVisibility(View.VISIBLE);
                holder.btnReplyMenu.setOnClickListener(v -> showReplyMenu(v, reply, position));
            } else {
                holder.btnReplyMenu.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showReplyMenu(View view, Reply reply, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.reply_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_reply) {
                if (replyActionListener != null) {
                    replyActionListener.onEditReply(reply, position);
                }
                return true;
            } else if (item.getItemId() == R.id.action_delete_reply) {
                if (replyActionListener != null) {
                    replyActionListener.onDeleteReply(reply, position);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return replyList != null ? replyList.size() : 0;
    }
    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvReply;
        TextView tvNameReply;
        TextView tvTimeReply;
        CircleImageView avtReply;
        ImageButton btnReplyMenu;
        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameReply = itemView.findViewById(R.id.tvReplyUsername);
            tvReply = itemView.findViewById(R.id.content);
            tvTimeReply = itemView.findViewById(R.id.tvReplyTime);
            avtReply = itemView.findViewById(R.id.ivReplyProfilePic);
            btnReplyMenu = itemView.findViewById(R.id.btnReplyMenu);
        }
    }
}
