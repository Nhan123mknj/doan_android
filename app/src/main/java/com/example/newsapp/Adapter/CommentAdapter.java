package com.example.newsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Reply;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {



    private final List<Comments> commentList = new ArrayList<>();
    public CommentAdapter(List<Comments> commentList) {
        if (commentList != null) this.commentList.addAll(commentList);
    }
    public void setComments(List<Comments> comments) {
        commentList.clear();
        commentList.addAll(comments);
        notifyDataSetChanged();
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
        List<Reply> replies = comment.getReplies();
        if (replies != null && !replies.isEmpty()) {
            holder.tvSeeMore.setVisibility(View.VISIBLE);
            holder.tvSeeMore.setText("See more (" + replies.size() + ")");
            holder.repliesRecyclerView.setVisibility(View.GONE);
            holder.tvSeeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.repliesRecyclerView.getVisibility() == View.VISIBLE) {
                        holder.repliesRecyclerView.setVisibility(View.GONE);
                        holder.tvSeeMore.setText("See more (" + replies.size() + ")");
                    } else {
                        holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                        holder.tvSeeMore.setText("Hide replies");
                    }
                }
            });
            ReplyAdapter replyAdapter = new ReplyAdapter(replies);
            holder.repliesRecyclerView.setAdapter(replyAdapter);
            holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        } else {
            holder.tvSeeMore.setVisibility(View.GONE);
            holder.repliesRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }



    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avtComment;
        TextView tvNameComment, tvContentComment, tvTimeComment,tvReply, tvSeeMore;
        RecyclerView repliesRecyclerView;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avtComment = itemView.findViewById(R.id.ivProfilePic);
            tvNameComment = itemView.findViewById(R.id.tvUsername);
            tvContentComment = itemView.findViewById(R.id.tvComment);
            tvTimeComment = itemView.findViewById(R.id.tvTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            repliesRecyclerView = itemView.findViewById(R.id.rvReplies);
        }
    }
}
