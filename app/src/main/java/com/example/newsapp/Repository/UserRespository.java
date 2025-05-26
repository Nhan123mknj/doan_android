package com.example.newsapp.Repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserRespository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<Users> userLiveData = new MutableLiveData<>();
    private static final String USERS_COLLECTION = "users";
    private Context context;

    public MutableLiveData<Users> getUserLiveData() {
        return userLiveData;
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
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            int count = (int) task.getResult().getCount();
                                            user.setCountViews(count);
                                            Log.d("UserRepository", "User " + user.getName() + " has " + count + " articles.");
                                            userLiveData.setValue(user);
                                        } else {
                                            Log.e("UserRepository", "Error fetching articles count: " + task.getException());
                                            user.setCountViews(0);
                                            userLiveData.setValue(user);
                                        }
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
         if(user == null){
            return;
         }
        final String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {

                    Toast.makeText(context, "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
