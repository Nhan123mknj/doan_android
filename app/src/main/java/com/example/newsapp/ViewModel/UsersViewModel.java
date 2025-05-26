package com.example.newsapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Users;
import com.example.newsapp.Repository.UserRespository;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UsersViewModel extends ViewModel {
    private final UserRespository userRespository;
    private MutableLiveData<List<Users>> usersLiveData;
    private List<Users> usersList;

    public UsersViewModel() {
        this.userRespository = new UserRespository();
        usersLiveData = new MutableLiveData<>();
        usersList = new ArrayList<>();
        usersLiveData.setValue(usersList);
    }
    public LiveData<Users> getUserById(String userId) {
        return userRespository.getUserById(userId);
    }
   public void UpdateUser(String userId, Users user) {
        userRespository.UpdateUser(userId, user);
   }
    public MutableLiveData<List<Users>> getUsersLiveData() {
        return usersLiveData;
    }
    public void changePass(String oldPassword, String newPassword) {
        userRespository.changePass(oldPassword, newPassword);
    }
}
