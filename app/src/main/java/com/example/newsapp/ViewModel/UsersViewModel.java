package com.example.newsapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Model.Users;
import com.example.newsapp.Repository.UserRespository;


import java.util.ArrayList;
import java.util.List;

public class UsersViewModel extends AndroidViewModel {
    private final UserRespository userRespository;
    private MutableLiveData<List<Users>> usersLiveData;
    private List<Users> usersList;

    public UsersViewModel(@NonNull Application application) {
        super(application);
        this.userRespository = new UserRespository(application);
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
