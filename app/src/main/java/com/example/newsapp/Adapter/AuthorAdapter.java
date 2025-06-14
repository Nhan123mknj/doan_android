package com.example.newsapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Interface.IClickItemUsersListener;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder> {
    private List<Users> authorList;
    private IClickItemUsersListener iClickItemAuthorListener;
    private String layoutType;

    public static final String FOLLOWER = "follower";
    public static final String FOLLOWING = "following";
    public AuthorAdapter(List<Users> authorList, IClickItemUsersListener iClickItemUsersListener, String layoutType) {
        this.authorList = authorList;
        this.iClickItemAuthorListener = iClickItemUsersListener;
        this.layoutType = layoutType;
    }

    public void setAuthorList(List<Users> authorList) {
        this.authorList = authorList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public AuthorAdapter.AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_author, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorAdapter.AuthorViewHolder holder, int position) {
        Users author = authorList.get(position);
        holder.authorName.setText(author.getName());
        Picasso.get().load(author.getAvatarUrl()).into(holder.authorImage);
        holder.followButton.setOnClickListener(v -> {
            if (iClickItemAuthorListener != null) {
                iClickItemAuthorListener.onItemClickedFollow(author);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (iClickItemAuthorListener != null) {
                iClickItemAuthorListener.onItemClickedUsers(author);
            }
        });
    }

    @Override
    public int getItemCount() {
        return authorList.size();
    }
    public class AuthorViewHolder extends RecyclerView.ViewHolder {
        TextView authorName;
        CircleImageView authorImage;
        Button followButton;
        public AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.authorName);
            authorImage = itemView.findViewById(R.id.authorImage);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}
