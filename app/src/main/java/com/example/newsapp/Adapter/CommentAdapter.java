package com.example.newsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Reply;
import com.example.newsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comments> commentList = new ArrayList<>();
    private OnReplyClickListener replyClickListener;
    private OnCommentActionListener commentActionListener;
    private String articleId;

    public interface OnReplyClickListener {
        void onReplyClick(Comments comment);
    }

    public interface OnCommentActionListener {
        void onEditComment(Comments comment);
        void onDeleteComment(Comments comment);
        void onEditReply(Comments comment, Reply reply, int replyPosition);
        void onDeleteReply(Comments comment, Reply reply, int replyPosition);
    }

    public CommentAdapter(List<Comments> commentList) {
        if (commentList != null) this.commentList.addAll(commentList);
    }

    public void setComments(List<Comments> comments) {
        commentList.clear();
        if (comments != null) {
        commentList.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void updateComment(Comments updatedComment) {
        if (updatedComment == null || updatedComment.getCommentId() == null) {
            return;
        }
        
        for (int i = 0; i < commentList.size(); i++) {
            Comments comment = commentList.get(i);
            if (comment != null && updatedComment.getCommentId().equals(comment.getCommentId())) {
                commentList.set(i, updatedComment);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void setOnReplyClickListener(OnReplyClickListener listener) {
        this.replyClickListener = listener;
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.commentActionListener = listener;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        Comments comment = commentList.get(position);
        holder.tvNameComment.setText(comment.getUsername());
        holder.tvContentComment.setText(comment.getContent());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTimeComment.setText(sdf.format(new Date(comment.getTimestamp())));
        } catch (Exception e) {
            holder.tvTimeComment.setText("");
        }
        String profilePicUrl = comment.getProfilePicUrl();
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Picasso.get().load(profilePicUrl).placeholder(R.drawable.avatar).into(holder.avtComment);
        } else {
            holder.avtComment.setImageResource(R.drawable.avatar);
        }

        // Hiển thị menu button nếu là comment của user hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
            holder.btnCommentMenu.setVisibility(View.VISIBLE);
            holder.btnCommentMenu.setOnClickListener(v -> showCommentMenu(v, comment));
        } else {
            holder.btnCommentMenu.setVisibility(View.GONE);
        }
        List<Reply> replies = comment.getReplies();
        if (replies != null && !replies.isEmpty()) {
            holder.tvSeeMore.setVisibility(View.VISIBLE);
            holder.tvSeeMore.setText("Xem thêm (" + replies.size() + " phản hồi)");
            
            // Check if replies are currently visible
            boolean isRepliesVisible = holder.repliesRecyclerView.getVisibility() == View.VISIBLE;
            
            // Always update the adapter with new replies data
            ReplyAdapter replyAdapter = new ReplyAdapter(replies);
            replyAdapter.setOnReplyActionListener(new ReplyAdapter.OnReplyActionListener() {
                @Override
                public void onEditReply(Reply reply, int replyPosition) {
                    if (commentActionListener != null) {
                        commentActionListener.onEditReply(comment, reply, replyPosition);
                    }
                }

                @Override
                public void onDeleteReply(Reply reply, int replyPosition) {
                    if (commentActionListener != null) {
                        commentActionListener.onDeleteReply(comment, reply, replyPosition);
                    }
                }
            });
            holder.repliesRecyclerView.setAdapter(replyAdapter);
            holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            
            // Maintain visibility state
            if (isRepliesVisible) {
                holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                holder.tvSeeMore.setText("Ẩn phản hồi");
            } else {
            holder.repliesRecyclerView.setVisibility(View.GONE);
            }
            
            // Clear previous click listener to avoid memory leaks
            holder.tvSeeMore.setOnClickListener(null);
            holder.tvSeeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                    if (holder.repliesRecyclerView.getVisibility() == View.VISIBLE) {
                        holder.repliesRecyclerView.setVisibility(View.GONE);
                            holder.tvSeeMore.setText("Xem thêm (" + replies.size() + " phản hồi)");
                    } else {
                        holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                            holder.tvSeeMore.setText("Ẩn phản hồi");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            holder.tvSeeMore.setVisibility(View.GONE);
            holder.repliesRecyclerView.setVisibility(View.GONE);
            holder.repliesRecyclerView.setAdapter(null);
        }
        holder.tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (replyClickListener != null && comment != null) {
                        replyClickListener.onReplyClick(comment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showCommentMenu(View view, Comments comment) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_comment) {
                if (commentActionListener != null) {
                    commentActionListener.onEditComment(comment);
                }
                return true;
            } else if (item.getItemId() == R.id.action_delete_comment) {
                if (commentActionListener != null) {
                    commentActionListener.onDeleteComment(comment);
                }
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avtComment;
        TextView tvNameComment, tvContentComment, tvTimeComment,tvReply, tvSeeMore;
        RecyclerView repliesRecyclerView;
        ImageButton btnCommentMenu;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avtComment = itemView.findViewById(R.id.ivProfilePic);
            tvNameComment = itemView.findViewById(R.id.tvUsername);
            tvContentComment = itemView.findViewById(R.id.tvComment);
            tvTimeComment = itemView.findViewById(R.id.tvTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            repliesRecyclerView = itemView.findViewById(R.id.rvReplies);
            btnCommentMenu = itemView.findViewById(R.id.btnCommentMenu);
        }
    }
}
