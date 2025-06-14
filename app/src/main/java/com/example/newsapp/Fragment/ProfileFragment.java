package com.example.newsapp.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsapp.Activity.AddArticlesActivity;
import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Activity.EditProfileActivity;
import com.example.newsapp.Activity.LoginActivity;
import com.example.newsapp.Activity.SettingActivity;
import com.example.newsapp.Activity.UpdateArticlesActivity;
import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.UsersViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private TextView name, email, followerCount,followingCount,tvDescription,newsCount;
    private CircleImageView avatar;
    private RecyclerView recyclerView;
    private Button btnEditProfile, btnWebsite, btnSetting;
    private FloatingActionButton btnAddArticles;
    private UsersViewModel usersViewModel;
    private ArticlesViewModel articlesViewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Người dùng chưa đăng nhập → chuyển sang LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish(); // Đóng MainActivity để không quay lại
            return view;
        }
        initView(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArticlesAdapter adapter = new ArticlesAdapter(new ArrayList<>(), new IClickItemArticlesListener() {
            @Override
            public void onItemClicked(Articles articles) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("articles", articles);
                startActivity(intent);
            }

            @Override
            public void onItemClickedCategories(Categories categories) {

            }

            @Override
            public void onItemClickedDelete(Articles articles) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || !user.getUid().equals(articles.getAuthor())) {
                    Toast.makeText(getContext(), "Bạn không có quyền xóa bài viết này", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa bài viết")
                        .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            articlesViewModel.deleteArticle(articles.getArticleId());
                            Toast.makeText(getContext(), "Bài viết đã được xóa", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }

            @Override
            public void onItemClickedUpdate(Articles articles) {
                Intent intent = new Intent(getContext(), UpdateArticlesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("articlesId",articles.getArticleId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        },ArticlesAdapter.TYPE_LASTEST);
        recyclerView.setAdapter(adapter);

        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        showUserInformation(user);

        articlesViewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
        showArticlesByUser(adapter);

        followerCount.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowerActivity.class);
            intent.putExtra("userId", user.getUid());
            startActivity(intent);
        });
        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowerActivity.class);
            intent.putExtra("userId", user.getUid());
            startActivity(intent);
        });

        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnAddArticles.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddArticlesActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void showArticlesByUser(ArticlesAdapter adapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
           Log.d("ProfileFragment", "User is null");
        }
        String uid = user.getUid();
        articlesViewModel.getArticlesByAuthor(uid).observe(getViewLifecycleOwner(), articles -> {
            if (articles != null && !articles.isEmpty()) {
                Log.d("ProfileFragment", "Articles fetched: " + articles.size());
                adapter.setArticlesList(articles);
            } else {
                Log.d("ProfileFragment", "No articles found for user: " + uid);
            }
        });

    }



    private void initView(View view) {
        name = view.findViewById(R.id.username);
        email = view.findViewById(R.id.tvEmail);
        newsCount = view.findViewById(R.id.newsCount);
        avatar = view.findViewById(R.id.profileImage);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnWebsite = view.findViewById(R.id.btnWebsite);
        btnSetting = view.findViewById(R.id.btn_Setting);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnAddArticles = view.findViewById(R.id.floatingActionButton);
        recyclerView = view.findViewById(R.id.rvNews);
        followerCount = view.findViewById(R.id.followerCount);
        followingCount = view.findViewById(R.id.followingCount);
    }


    private void showUserInformation(FirebaseUser user){
        if (user == null) return;

        usersViewModel.getUserById(user.getUid()).observe(getViewLifecycleOwner(), userObj -> {
            if(userObj == null) return;
            name.setText(userObj.getName());
            email.setText(userObj.getEmail());
            tvDescription.setText(userObj.getDescription());
            Picasso.get().load(userObj.getAvatarUrl()).into(avatar);
            newsCount.setText(String.valueOf(userObj.getCountViews()));
            followerCount.setText(String.valueOf(userObj.getFollowerCount()));
            followingCount.setText(String.valueOf(userObj.getFollowingCount()));
        });

    }
}