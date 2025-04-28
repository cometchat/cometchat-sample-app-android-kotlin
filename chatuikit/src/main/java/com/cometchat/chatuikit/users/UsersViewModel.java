package com.cometchat.chatuikit.users;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.core.UsersRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.events.CometChatUserEvents;
import com.cometchat.chatuikit.shared.resources.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing user data in the application.
 */
public class UsersViewModel extends ViewModel {
    private static final String TAG = UsersViewModel.class.getSimpleName();
    public UsersRequest.UsersRequestBuilder usersRequestBuilder;
    public UsersRequest.UsersRequestBuilder searchUsersRequestBuilder;
    public String LISTENERS_TAG;
    public MutableLiveData<List<User>> mutableUsersList;
    public MutableLiveData<Integer> insertAtTop;
    public MutableLiveData<Integer> moveToTop;
    public List<User> userArrayList;
    public MutableLiveData<Integer> updateUser;
    public MutableLiveData<Integer> removeUser;
    public MutableLiveData<CometChatException> cometchatException;
    public MutableLiveData<UIKitConstants.States> states;
    public int limit = 30;
    public boolean connectionListerAttached;
    public boolean hasMore = true;
    private UsersRequest usersRequest;

    /**
     * Initializes the ViewModel and sets up initial values for user requests and
     * LiveData.
     */
    public UsersViewModel() {
        mutableUsersList = new MutableLiveData<>();
        insertAtTop = new MutableLiveData<>();
        moveToTop = new MutableLiveData<>();
        userArrayList = new ArrayList<>();
        updateUser = new MutableLiveData<>();
        removeUser = new MutableLiveData<>();
        cometchatException = new MutableLiveData<>();
        states = new MutableLiveData<>();
        usersRequestBuilder = new UsersRequest.UsersRequestBuilder().setLimit(limit);
        searchUsersRequestBuilder = new UsersRequest.UsersRequestBuilder();
        usersRequest = usersRequestBuilder.build();
    }

    /**
     * Gets the LiveData object for the mutable user list.
     *
     * @return the mutable user list
     */
    public MutableLiveData<List<User>> getMutableUsersList() {
        return mutableUsersList;
    }

    /**
     * Gets the LiveData object indicating the position to insert a user at the top.
     *
     * @return the LiveData for insertAtTop
     */
    public MutableLiveData<Integer> insertAtTop() {
        return insertAtTop;
    }

    /**
     * Gets the LiveData object indicating the position to move a user to the top.
     *
     * @return the LiveData for moveToTop
     */
    public MutableLiveData<Integer> moveToTop() {
        return moveToTop;
    }

    /**
     * Gets the list of users.
     *
     * @return the list of users
     */
    public List<User> getUserArrayList() {
        return userArrayList;
    }

    /**
     * Gets the LiveData object indicating the position of the updated user.
     *
     * @return the LiveData for updateUser
     */
    public MutableLiveData<Integer> updateUser() {
        return updateUser;
    }

    /**
     * Gets the LiveData object for removed users.
     *
     * @return the LiveData for removed users
     */
    public MutableLiveData<Integer> removeUser() {
        return removeUser;
    }

    /**
     * Gets the LiveData object for CometChat exceptions.
     *
     * @return the LiveData for CometChat exceptions
     */
    public MutableLiveData<CometChatException> getCometChatException() {
        return cometchatException;
    }

    /**
     * Gets the LiveData object for the states of the UI.
     *
     * @return the LiveData for UI states
     */
    public MutableLiveData<UIKitConstants.States> getStates() {
        return states;
    }

    /**
     * Adds listeners for user online/offline events.
     */
    public void addListeners() {
        LISTENERS_TAG = System.currentTimeMillis() + "";
        CometChat.addUserListener(LISTENERS_TAG, new CometChat.UserListener() {
            @Override
            public void onUserOnline(User user) {
                if (!Utils.isBlocked(user)) moveToTop(user);
            }

            @Override
            public void onUserOffline(User user) {
                if (!Utils.isBlocked(user)) updateUser(user);
            }
        });
        CometChatUserEvents.addUserListener(LISTENERS_TAG, new CometChatUserEvents() {
            @Override
            public void ccUserBlocked(User user) {
                updateUser(user);
            }

            @Override
            public void ccUserUnblocked(User user) {
                updateUser(user);
            }
        });
    }

    /**
     * Moves a specific user to the top of the user list.
     *
     * @param user the user to move
     */
    public void moveToTop(User user) {
        if (userArrayList.contains(user)) {
            int oldIndex = userArrayList.indexOf(user);
            userArrayList.remove(user);
            userArrayList.add(0, user);
            moveToTop.setValue(oldIndex);
        }
    }

    /**
     * Updates the information of a specific user in the user list.
     *
     * @param user the user to update
     */
    public void updateUser(User user) {
        if (userArrayList.contains(user)) {
            userArrayList.set(userArrayList.indexOf(user), user);
            updateUser.setValue(userArrayList.indexOf(user));
        }
    }

    /**
     * Adds a connection listener to refresh the user list upon connection.
     */
    public void addConnectionListener() {
        CometChat.addConnectionListener(LISTENERS_TAG, new CometChat.ConnectionListener() {
            @Override
            public void onConnected() {
                refreshList();
            }

            @Override
            public void onConnecting() {
                // Handle connecting state if needed
            }

            @Override
            public void onDisconnected() {
                // Handle disconnected state if needed
            }

            @Override
            public void onFeatureThrottled() {
                // Handle feature throttled state if needed
            }

            @Override
            public void onConnectionError(CometChatException e) {
                // Handle connection error if needed
            }
        });
    }

    /**
     * Removes all listeners associated with user and connection events.
     */
    public void removeListeners() {
        CometChat.removeUserListener(LISTENERS_TAG);
        CometChatUserEvents.removeListener(LISTENERS_TAG);
        CometChat.removeConnectionListener(LISTENERS_TAG);
    }

    /**
     * Adds a user to the top of the user list.
     *
     * @param user the user to add
     */
    public void addToTop(User user) {
        if (user != null && !userArrayList.contains(user)) {
            userArrayList.add(0, user);
            insertAtTop.setValue(0);
        }
    }

    /**
     * Removes a specific user from the user list.
     *
     * @param user the user to remove
     */
    public void removeUser(User user) {
        if (userArrayList.contains(user)) {
            int index = userArrayList.indexOf(user);
            this.userArrayList.remove(user);
            removeUser.setValue(index);
            states.setValue(checkIsEmpty(userArrayList));
        }
    }

    /**
     * Checks if the provided list of users is empty.
     *
     * @param users the list of users to check
     * @return the state indicating whether the list is empty or not
     */
    private UIKitConstants.States checkIsEmpty(List<User> users) {
        if (users.isEmpty()) return UIKitConstants.States.EMPTY;
        return UIKitConstants.States.NON_EMPTY;
    }

    /**
     * Fetches the user list if it is empty and there are more users to load.
     */
    public void fetchUsers() {
        if (userArrayList.isEmpty()) {
            states.setValue(UIKitConstants.States.LOADING);
        }
        if (hasMore) {
            fetchUsersList(false);
        }
    }

    /**
     * Fetches the user list from the server.
     *
     * @param cleanAndLoad whether to clear the current list before loading new data
     */
    private void fetchUsersList(boolean cleanAndLoad) {

        usersRequest.fetchNext(new CometChat.CallbackListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                setupFetchedData(cleanAndLoad, users);
            }

            @Override
            public void onError(CometChatException e) {
                cometchatException.setValue(e);
                if (userArrayList.isEmpty()) {
                    states.setValue(UIKitConstants.States.ERROR);
                }
            }
        });

    }

    private synchronized void setupFetchedData(boolean cleanAndLoad, List<User> users) {
        if (cleanAndLoad) clear();

        hasMore = !users.isEmpty();
        if (hasMore) {
            addList(users);
        }

        // Update state to either LOADED or EMPTY based on the list size
        states.setValue(checkIsEmpty(userArrayList));

        // Attach the connection listener if not already attached
        if (!connectionListerAttached) {
            addConnectionListener();
            connectionListerAttached = true;
        }
    }

    /**
     * Refreshes the user list from the server.
     */
    public void refreshList() {
        if (usersRequestBuilder != null) {
            clear();
            usersRequest = usersRequestBuilder.build();
            hasMore = true;
            fetchUsersList(true);
        }
    }

    /**
     * Searches for users based on the specified keyword.
     *
     * @param search the search keyword
     */
    public void searchUsers(String search) {
        clear();
        hasMore = true;
        if (search != null) usersRequest = searchUsersRequestBuilder.setSearchKeyword(search).build();
        else usersRequest = usersRequestBuilder.build();
        fetchUsers();
    }

    /**
     * Adds a list of users to the user list.
     *
     * @param userList the list of users to add
     */
    public void addList(List<User> userList) {
        for (User user : userList) {
            if (userArrayList.contains(user)) {
                int index = userArrayList.indexOf(user);
                userArrayList.remove(index);
                userArrayList.add(index, user);
            } else {
                userArrayList.add(user);
            }
        }
        mutableUsersList.setValue(userArrayList);
    }

    /**
     * Sets the UsersRequestBuilder for fetching users.
     *
     * @param usersRequest the UsersRequestBuilder to set
     */
    public void setUsersRequestBuilder(UsersRequest.UsersRequestBuilder usersRequest) {
        if (usersRequest != null) {
            this.usersRequestBuilder = usersRequest;
            this.usersRequest = usersRequestBuilder.build();
        }
    }

    /**
     * Sets the UsersRequestBuilder for searching users.
     *
     * @param usersRequestBuilder the UsersRequestBuilder to set
     */
    public void setSearchRequestBuilder(@Nullable UsersRequest.UsersRequestBuilder usersRequestBuilder) {
        if (usersRequestBuilder != null) this.searchUsersRequestBuilder = usersRequestBuilder;
    }

    /**
     * Clears the current user list.
     */
    public void clear() {
        userArrayList.clear();
        mutableUsersList.setValue(userArrayList);
    }
}
