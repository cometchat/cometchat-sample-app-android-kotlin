package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.databinding.ActivitySearchBinding;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private ActivitySearchBinding binding;
    private User user;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isFromMessages = getIntent().getBooleanExtra("isFromMessageScreen", false);
        String userJson = getIntent().getStringExtra(getString(R.string.app_user));
        String groupJson = getIntent().getStringExtra(getString(R.string.app_group));

        try {
            if (userJson != null) {
                user = User.fromJson(new JSONObject(userJson).toString());
            }
            if (groupJson != null) {
                group = new Gson().fromJson(groupJson, Group.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.cometchatSearch.setOnBackPressListener(this::finish);

        binding.cometchatSearch.setOnMessageClicked((view, position, baseMessage) -> {
            handleMessageClick(baseMessage);
        });

        binding.cometchatSearch.setOnConversationClicked((view, position, conversation) -> {
            if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_GROUP)) {
                Group group = (Group) conversation.getConversationWith();
                navigateToActivity(null, group, null, null);
            } else {
                User user = (User) conversation.getConversationWith();
                navigateToActivity(user, null, null, null);
            }
        });

        if (isFromMessages) {
            List<UIKitConstants.SearchFilter> searchFilters = Arrays.asList(
                UIKitConstants.SearchFilter.PHOTOS,
                UIKitConstants.SearchFilter.VIDEOS,
                UIKitConstants.SearchFilter.DOCUMENTS,
                UIKitConstants.SearchFilter.LINKS,
                UIKitConstants.SearchFilter.AUDIO
            );
            binding.cometchatSearch.setSearchFilters(searchFilters);

            if (user != null) {
                binding.cometchatSearch.setUid(user.getUid());
            }

            if (group != null) {
                binding.cometchatSearch.setGuid(group.getGuid());
            }
        }
    }

    private void handleMessageClick(BaseMessage goToMessage) {
        if (UIKitConstants.ReceiverType.USER.equals(goToMessage.getReceiverType())) {
            String uid;
            if (goToMessage.getSender().getUid().equals(CometChat.getLoggedInUser().getUid())) {
                uid = goToMessage.getReceiverUid();
            } else {
                uid = goToMessage.getSender().getUid();
            }

            Repository.getUser(uid, new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    getParentMessage(goToMessage.getParentMessageId(), parentMessage -> {
                        navigateToActivity(user, null, goToMessage, parentMessage);
                    }, () -> {
                        navigateToActivity(user, null, goToMessage, null);
                    });
                }

                @Override
                public void onError(CometChatException p0) {
                    Log.e(TAG, "onError: ", p0);
                }
            });
        } else if (UIKitConstants.ReceiverType.GROUP.equals(goToMessage.getReceiverType())) {
            Repository.getGroup(goToMessage.getReceiverUid(), new CometChat.CallbackListener<Group>() {
                @Override
                public void onSuccess(Group group) {
                    getParentMessage(goToMessage.getParentMessageId(), parentMessage -> {
                        navigateToActivity(null, group, goToMessage, parentMessage);
                    }, () -> {
                        navigateToActivity(null, group, goToMessage, null);
                    });
                }

                @Override
                public void onError(CometChatException p0) {
                    Log.e(TAG, "Test: onError: ", p0);
                }
            });
        }
    }

    private void getParentMessage(
            long parentMessageId,
            final OnSuccess<BaseMessage> onSuccess,
            final OnError onError
    ) {
        Repository.fetchMessageInformation(
                parentMessageId,
                new CometChat.CallbackListener<BaseMessage>() {
                    @Override
                    public void onSuccess(BaseMessage parentMessage) {
                        onSuccess.onSuccess(parentMessage);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        onError.onError();
                    }
                }
        );
    }


    private void navigateToActivity(User user, Group group, BaseMessage baseMessage, BaseMessage parentMessage) {
        Intent intent;
        if (parentMessage != null) {
            intent = new Intent(SearchActivity.this, ThreadMessageActivity.class);
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessage.getRawMessage().toString());
        } else {
            intent = new Intent(SearchActivity.this, MessagesActivity.class);
        }

        if (user != null) {
            intent.putExtra(getString(R.string.app_user), user.toJson().toString());
        }
        if (group != null) {
            intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
        }
        if (baseMessage != null) {
            intent.putExtra(getString(R.string.app_go_to_message), baseMessage.getRawMessage().toString());
        }

        startActivity(intent);
        finish();
    }


    public interface OnSuccess<T> {
        void onSuccess(T result);
    }

    public interface OnError {
        void onError();
    }
}