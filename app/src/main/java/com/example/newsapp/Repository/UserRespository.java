package com.example.newsapp.Repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Model.Follow;
import com.example.newsapp.Model.Users;

import com.google.firebase.auth.AuthCredential;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;


public class UserRespository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<Users> userLiveData = new MutableLiveData<>();
    private static final String USERS_COLLECTION = "users";
    private final Context context;

    public UserRespository(Context context) {
        this.context = context;
    }




    public LiveData<Users> getUserById(String userId) {
        MutableLiveData<Users> userLiveData = new MutableLiveData<>();
        db.collection(USERS_COLLECTION)
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("UserRepository", "Error fetching user: " + error.getMessage());
                        userLiveData.setValue(null);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Users user = snapshot.toObject(Users.class);
                        if (user != null) {
                            user.setUserId(snapshot.getId());
                            Log.d("UserRepository", "Loaded user: " + user.getName());

                            db.collection("articles")
                                    .whereEqualTo("author", userId)
                                    .count()
                                    .get(AggregateSource.SERVER)
                                    .addOnCompleteListener(task -> {
                                        int count = 0;
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            count = (int) task.getResult().getCount();
                                        }
                                        user.setCountViews(count);

                                        // Đếm số follower
                                        db.collection("follows")
                                                .whereEqualTo("authorId", userId)
                                                .get()
                                                .addOnSuccessListener(followerSnapshots -> {
                                                    user.setFollowerCount(followerSnapshots.size());

                                                    // Đếm số following
                                                    db.collection("follows")
                                                            .whereEqualTo("followerId", userId)
                                                            .get()
                                                            .addOnSuccessListener(followingSnapshots -> {
                                                                user.setFollowingCount(followingSnapshots.size());
                                                                userLiveData.setValue(user);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                user.setFollowingCount(0);
                                                                userLiveData.setValue(user);
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    user.setFollowerCount(0);
                                                    user.setFollowingCount(0);
                                                    userLiveData.setValue(user);
                                                });
                                    });
                        } else {
                            userLiveData.setValue(null);
                        }
                    } else {
                        Log.w("UserRepository", "User not found: " + userId);
                        userLiveData.setValue(null);
                    }
                });
        return userLiveData;
    }
    public LiveData<List<Users>> getAllAuthors()  {
        MutableLiveData<List<Users>> authorsLiveData = new MutableLiveData<>();
        db.collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Users> authors = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Users user = doc.toObject(Users.class);
                        if (user != null) {
                            user.setUserId(doc.getId());
                            authors.add(user);
                        }
                    }
                    authorsLiveData.setValue(authors);
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Error fetching authors: " + e.getMessage());
                    authorsLiveData.setValue(null);
                });
        return authorsLiveData;

    }
    public void UpdateUser(String userId, Users user) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    userLiveData.setValue(user);
                    Log.d("UserRespository", "User updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRespository", "Error updating user: " + e.getMessage());
                });
    }

    public void changePass(String oldPassword, String newPassword) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Đổi mật khẩu thất bại: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(context, "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void toggleFollowUser(String followerId, String authorId, OnFollowStatusChangedListener listener) {
        String docId = followerId + "_" + authorId;
        db.collection("follows")
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("follows")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (listener != null) listener.onChanged(false);
                                });
                    } else {
                        db.collection("follows")
                                .document(docId)
                                .set(new Follow(followerId, authorId))
                                .addOnSuccessListener(aVoid -> {
                                    if (listener != null) listener.onChanged(true);
                                });
                    }
                });
    }

    public void checkIsFollowing(String followerId, String authorId, OnFollowStatusChangedListener listener) {
        String docId = followerId + "_" + authorId;
        db.collection("follows")
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (listener != null) listener.onChanged(documentSnapshot.exists());
                });
    }
    public LiveData<List<Users>> getFollowersDetail(String myUserId) {
        MutableLiveData<List<Users>> followersLiveData = new MutableLiveData<>();
        db.collection("follows")
                .whereEqualTo("authorId", myUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followerIds = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        followerIds.add(doc.getString("followerId"));
                    }
                    if (followerIds.isEmpty()) {
                        followersLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    db.collection("users")
                            .whereIn("userId", followerIds)
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                List<Users> users = new ArrayList<>();
                                for (DocumentSnapshot userDoc : userSnapshots) {
                                    String name = userDoc.getString("name");
                                    String avatar = userDoc.getString("avatarUrl");

                                    Users user = new Users();
                                    user.setName(name);
                                    user.setAvatarUrl(avatar);
                                    users.add(user);
                                }
                                followersLiveData.setValue(users);
                            })
                            .addOnFailureListener(e -> followersLiveData.setValue(null));
                })
                .addOnFailureListener(e -> followersLiveData.setValue(null));
        return followersLiveData;
    }
    public LiveData<List<Users>> getFollowingDetail(String myUserId) {
        MutableLiveData<List<Users>> followingLiveData = new MutableLiveData<>();
        if (myUserId == null || myUserId.isEmpty()) {
            followingLiveData.setValue(new ArrayList<>());
            return followingLiveData;
        }

        Log.d("UsersRepository", "Fetching follows for userId: " + myUserId);
        db.collection("follows")
                .whereEqualTo("followerId", myUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> authorIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String authorId = doc.getString("authorId");
                        if (authorId != null) {
                            authorIds.add(authorId);
                            Log.d("UsersRepository", "Found authorId: " + authorId);
                        }
                    }
                    if (authorIds.isEmpty()) {
                        followingLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    db.collection("users")
                            .whereIn("userId", authorIds)
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                List<Users> users = new ArrayList<>();
                                for (DocumentSnapshot userDoc : userSnapshots) {
                                    String userId = userDoc.getId();
                                    String name = userDoc.getString("name");
                                    String avatar = userDoc.getString("avatarUrl");

                                    Users user = new Users();
                                    user.setUserId(userId);
                                    user.setName(name);
                                    user.setAvatarUrl(avatar);
                                    users.add(user);
                                }
                                Log.d("UsersRepository", "Loaded " + users.size() + " followed users");
                                followingLiveData.setValue(users);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UsersRepository", "Error loading users: " + e.getMessage());
                                followingLiveData.setValue(new ArrayList<>());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("UsersRepository", "Error fetching follows: " + e.getMessage());
                    followingLiveData.setValue(new ArrayList<>());
                });
        return followingLiveData;
    }

    public interface OnFollowResultListener {
        void onFollowSuccess();
        void onFollowFailure(String errorMessage);
    }
    public interface OnFollowStatusChangedListener {
        void onChanged(boolean isFollowing);
    }
}