package com.example.newsapp.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.newsapp.Helper.DialogLogin;
import com.example.newsapp.Helper.DialogReport;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;
import com.example.newsapp.Repository.ArticlesRepository;
import com.example.newsapp.Repository.UserRespository;
import com.example.newsapp.Services.TSSServices;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.UsersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Articles article;
    private ArticlesViewModel viewModel;
    private UsersViewModel userviewModel;
    TextView contentTextView, title, date, likeCount, commentCount,miniPlayerTitle;
    ImageView thumbnail,miniPlayerThumbnail;
    ImageButton btnBack, btnLike, btnSave, btnComment, btnMore, btnRead,miniPlayerPlayPause,miniPlayerStop;
    TextView categoryTextView, authorName;
    private boolean isReading = false;
    private boolean isPaused = false;
    private Button followButton;
    private LinearLayout miniPlayer;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            article = (Articles) bundle.get("articleObj");
            isReading = bundle.getBoolean("isReading");
            int actionReading = bundle.getInt("action_reading");
            handleLayoutReading(actionReading);
        }
    };

    private void handleLayoutReading(int actionReading) {
        switch (actionReading) {
            case TSSServices.ACTION_START:
                miniPlayer.setVisibility(View.VISIBLE);
                miniPlayerPlayPause.setImageResource(R.drawable.pause_icon);
                miniPlayerTitle.setSelected(true);
                miniPlayerTitle.setText(title.getText().toString());

                Picasso.get().load(article.getUrlToImage()).into(miniPlayerThumbnail);
                setStatusPlayPause();
                break;
            case TSSServices.ACTION_PAUSE:
                isPaused = true;
                setStatusPlayPause();
                break;
            case TSSServices.ACTION_RESUME:
                isPaused = false;
                setStatusPlayPause();
                break;
            case TSSServices.ACTION_CLEAR:
                isPaused = false;
                isReading = false;
                miniPlayer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter("send_action_to_activity"));


        initView();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        viewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
        userviewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        Intent intent = getIntent();
        intent.getExtras();
        article = (Articles) intent.getSerializableExtra("articles");

        if (article != null) {
            viewModel.getArticleById(article.getArticleId()).observe(this, articles -> {
                contentTextView.setText(articles.getContent());
                title.setText(articles.getTitle());
                date.setText(articles.getPublishedAt());
                likeCount.setText(String.valueOf(article.getLikesCount()));
                Picasso.get().load(articles.getUrlToImage()).into(thumbnail);
                categoryTextView.setText(articles.getCategoryName());
                authorName.setText(articles.getUsername());
                Log.d("cateid", article.getArticleId());
            });
            

            viewModel.getCommentCount(article.getArticleId()).observe(this, count -> {
                if (count != null) {
                    Log.d("DetailActivity", "Comment count: " + count);
                    commentCount.setText(formatCount(count));
                } else {
                    commentCount.setText("0");
                }
            });
        }



        if (user != null) {
            String userId = user.getUid();
            viewModel.getLikeStatus(article.getArticleId(), userId).observe(this, isLiked -> {
                if (isLiked != null) {
                    btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                } else {
                    btnLike.setImageResource(R.drawable.ic_heart_outline);
                }
            });

            // Observe bookmark status
            viewModel.getBookmarkStatus(article.getArticleId(), userId).observe(this, isBookmarked -> {
                if (isBookmarked != null) {
                    btnSave.setImageResource(isBookmarked ? R.drawable.save : R.drawable.ic_save_outline);
                } else {
                    btnSave.setImageResource(R.drawable.ic_save_outline);
                }
            });
        } else {
            btnLike.setImageResource(R.drawable.ic_heart_outline);
            btnSave.setImageResource(R.drawable.ic_save_outline);
        }

        btnComment.setOnClickListener(v -> {
            Intent intent1 = new Intent(DetailActivity.this, CommentActivity.class);
            intent1.putExtra("articleId", article.getArticleId());
            startActivity(intent1);
        });

        btnLike.setOnClickListener(v -> {
            btnLike.setEnabled(false);
            viewModel.likeArticle(article.getArticleId(), DetailActivity.this,
                    new ArticlesRepository.OnLikeResultListener() {
                        @Override
                        public void onSuccess(boolean isLiked) {
                            btnLike.setEnabled(true);
                            Toast.makeText(DetailActivity.this, isLiked ? "Đã thích bài viết" : "Đã bỏ thích",
                                    Toast.LENGTH_SHORT).show();
                            viewModel.getArticleById(article.getArticleId()).observe(DetailActivity.this, articles -> {
                                if (articles != null) {
                                    likeCount.setText(String.valueOf(articles.getLikesCount()));
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnLike.setEnabled(true);
                            if (errorMessage.equals("Yêu cầu đăng nhập")) {
                                Intent loginIntent = new Intent(DetailActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            } else {
                                Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            viewModel.bookmarkArticle(article.getArticleId(), DetailActivity.this,
                    new ArticlesRepository.OnBookmarkResultListener() {
                        @Override
                        public void onSuccess(boolean isBookmarked) {
                            btnSave.setEnabled(true);
                            Toast.makeText(DetailActivity.this, isBookmarked ? "Đã lưu bài viết" : "Đã bỏ lưu",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            btnSave.setEnabled(true);
                            if (errorMessage.equals("Login required")) {
                                Intent loginIntent = new Intent(DetailActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            } else {
                                Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        followButton.setOnClickListener(v -> {
            if (user == null) {
                mainHandler.post(() -> DialogLogin.openDialogLogin(
                        this,
                        Gravity.CENTER,
                        () -> {
                            Intent loginIntent = new Intent(this, LoginActivity.class);
                            ((Activity) this).startActivityForResult(loginIntent, 100);
                        }
                ));
                return;
            }
            String userId = user.getUid();
            userviewModel.toggleFollowUser(article.getAuthor(), userId, new UserRespository.OnFollowStatusChangedListener() {
                @Override
                public void onChanged(boolean isFollowing) {
                    if (isFollowing) {
                        followButton.setText("Đã theo dõi");
                        Toast.makeText(DetailActivity.this, "Đã theo dõi người dùng", Toast.LENGTH_SHORT).show();
                    } else {
                        followButton.setText("Theo dõi");
                        Toast.makeText(DetailActivity.this, "Đã bỏ theo dõi người dùng", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


        btnMore.setOnClickListener(v -> {
            showPopupMenu(v);
        });
        btnRead.setOnClickListener(v -> {

            if (!isReading) {
                Intent serviceIntent = new Intent(DetailActivity.this, TSSServices.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("article", article);
                bundle.putInt("actionService", TSSServices.ACTION_START);

                serviceIntent.putExtras(bundle);
                startService(serviceIntent);

            }
        });

        miniPlayerPlayPause.setOnClickListener(v -> {

            if (isReading ) {
               sendActionToService(TSSServices.ACTION_PAUSE);
            } else{
               sendActionToService(TSSServices.ACTION_RESUME);
            }
        });

        miniPlayerStop.setOnClickListener(v -> {
            sendActionToService(TSSServices.ACTION_CLEAR);

        });
    }

    private void initView() {
        title = findViewById(R.id.title);
        likeCount = findViewById(R.id.likeCount);
        date = findViewById(R.id.pubDate);
        thumbnail = findViewById(R.id.thumbnail);
        btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnLike);
        btnSave = findViewById(R.id.btn_save);
        contentTextView = findViewById(R.id.content);
        btnComment = findViewById(R.id.btnComment);
        categoryTextView = findViewById(R.id.categoryTextView);
        authorName = findViewById(R.id.authorName);
        btnMore = findViewById(R.id.btnMore);
        commentCount = findViewById(R.id.commentCount);
        btnRead = findViewById(R.id.btn_read);
        miniPlayer = findViewById(R.id.miniPlayer);
        miniPlayerPlayPause = findViewById(R.id.miniPlayerPlayPause);
        miniPlayerTitle = findViewById(R.id.miniPlayerTitle);
        miniPlayerStop = findViewById(R.id.miniPlayerStop);
        miniPlayerThumbnail = findViewById(R.id.miniPlayerThumbnail);
        followButton = findViewById(R.id.followButton);
    }

    private void showPopupMenu(android.view.View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_article_more);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_report:
                        showReportDialog();
                        return true;
                    case R.id.action_share:
                        shareArticle();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showReportDialog() {
        DialogReport.showReportDialog(this, article, new DialogReport.OnReportSubmitListener() {
            @Override
            public void onReportSubmit(String reason, String description) {
                viewModel.reportArticle(article.getArticleId(), article.getTitle(), reason, description,
                        DetailActivity.this, new ArticlesRepository.OnReportResultListener() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    /**
     * Formats count numbers for display (e.g., 1500 -> 1.5K, 1000000 -> 1.0M)
     */
    private String formatCount(int count) {
        if (count >= 1000000) {
            double countM = count / 1000000.0;
            return String.format("%.1fM", countM);
        } else if (count >= 1000) {
            double countK = count / 1000.0;
            return String.format("%.1fK", countK);
        } else {
            return String.valueOf(count);
        }
    }

    private void shareArticle() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.getTitle() + "\n" + article.getLink());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
    }
    private void setStatusPlayPause() {
        if (isReading && !isPaused) {
            miniPlayerPlayPause.setImageResource(R.drawable.pause_icon);
        } else {
            miniPlayerPlayPause.setImageResource(R.drawable.play_ic);
        }
    }
    private void sendActionToService(int action) {
        Intent intent = new Intent(this, TSSServices.class);
        intent.putExtra("actionService", action);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

}
