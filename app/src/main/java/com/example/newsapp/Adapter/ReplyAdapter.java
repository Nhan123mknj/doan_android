package com.example.newsapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Model.Reply;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private final List<Reply> replyList;

    public ReplyAdapter( List<Reply> replyList) {

        this.replyList = replyList;
    }

    @NonNull
    @Override
    public ReplyAdapter.ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.ReplyViewHolder holder, int position) {
        Reply reply = replyList.get(position);
        holder.tvNameReply.setText(reply.getUsername());
        holder.tvReply.setText(reply.getContent());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTimeReply.setText(sdf.format(new Date(reply.getTimestamp())));
        } catch (Exception e) {
            holder.tvTimeReply.setText("");
        }
        Picasso.get().load(reply.getProfilePicUrl()).placeholder(R.drawable.avatar).into(holder.avtReply);
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }
    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvReply;
        TextView tvNameReply;
        TextView tvTimeReply;
        CircleImageView avtReply;
        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameReply = itemView.findViewById(R.id.tvReplyUsername);
            tvReply = itemView.findViewById(R.id.content);
            tvTimeReply = itemView.findViewById(R.id.tvReplyTime);
            avtReply = itemView.findViewById(R.id.ivReplyProfilePic);
        }
    }
}
