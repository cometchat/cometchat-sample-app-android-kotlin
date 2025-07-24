package com.cometchat.sampleapp.java.fcm.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<CometChatException> onError = new MutableLiveData<>();

    public MutableLiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }

    public MutableLiveData<List<User>> getUsers() {
        return usersLiveData;
    }

    public MutableLiveData<User> getSelectedUser() {
        return selectedUser;
    }

    public MutableLiveData<CometChatException> onError() {
        return onError;
    }

    public void checkUserIsNotLoggedIn() {
        if (CometChatUIKit.getLoggedInUser() != null) {
            loginStatus.setValue(true); // User is logged in
        } else {
            loginStatus.setValue(false); // User is not logged in
        }
    }

    public void getSampleUsers() {
        Repository.fetchSampleUsers(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                usersLiveData.setValue(users);
            }

            @Override
            public void onError(CometChatException e) {
                usersLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void selectedUser(User user) {
        selectedUser.setValue(user);
    }

    public void login(String uid) {
        Repository.loginUser(uid, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                Repository.registerFCMToken(new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        CometChat.connect(new CometChat.CallbackListener<String>() {
                            @Override
                            public void onSuccess(String s) {

                            }

                            @Override
                            public void onError(CometChatException e) {

                            }
                        });
                        loginStatus.setValue(true);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        loginStatus.setValue(false);
                        onError.setValue(e);
                    }
                });
            }

            @Override
            public void onError(CometChatException e) {
                loginStatus.setValue(false);
                onError.setValue(e);
            }
        });
    }
}
