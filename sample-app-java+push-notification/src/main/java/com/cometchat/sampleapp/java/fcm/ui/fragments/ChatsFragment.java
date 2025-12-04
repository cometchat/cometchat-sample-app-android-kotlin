package com.cometchat.sampleapp.java.fcm.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar;
import com.cometchat.sampleapp.java.fcm.BuildConfig;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.data.interfaces.OnItemClickListener;
import com.cometchat.sampleapp.java.fcm.data.repository.Repository;
import com.cometchat.sampleapp.java.fcm.databinding.FragmentChatsBinding;
import com.cometchat.sampleapp.java.fcm.databinding.UserProfilePopupMenuLayoutBinding;
import com.cometchat.sampleapp.java.fcm.fcm.FCMMessageDTO;
import com.cometchat.sampleapp.java.fcm.ui.activity.MessagesActivity;
import com.cometchat.sampleapp.java.fcm.ui.activity.SearchActivity;
import com.cometchat.sampleapp.java.fcm.ui.activity.SplashActivity;
import com.cometchat.sampleapp.java.fcm.ui.activity.ThreadMessageActivity;
import com.cometchat.sampleapp.java.fcm.utils.AppConstants;
import com.cometchat.sampleapp.java.fcm.utils.MyApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

/**
 * A fragment representing the chat interface where users can see their
 * conversations and interact with them.
 */
public class ChatsFragment extends Fragment {
    private final String TAG = ChatsFragment.class.getSimpleName();

    private FragmentChatsBinding binding;

    private OnItemClickListener listener;

    /**
     * Default constructor for the ChatsFragment.
     */
    public ChatsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnItemClickListener) {
            listener = (OnItemClickListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up item click listener for the conversations view
        binding.cometchatConversations.setOnItemClick((view1, position, conversation) -> {
            if (conversation.getConversationType().equals(CometChatConstants.CONVERSATION_TYPE_GROUP)) {
                Group group = (Group) conversation.getConversationWith();
                navigateToMessages(group, null);
            } else {
                User user = (User) conversation.getConversationWith();
                navigateToMessages(null, user);
            }
        });

        binding.cometchatConversations.setOnSearchClickListener(() -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });

        // Set the overflow menu (Logout button) in the Conversations view
        handleDeepLinking();
        binding.cometchatConversations.setOverflowMenu(getLogoutView());
    }

    private void navigateToMessages(Group group, User user) {
        Intent intent = new Intent(getContext(), MessagesActivity.class);
        if (user != null) {
            intent.putExtra(getString(R.string.app_user), user.toJson().toString());
        } else intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Creates a logout view that displays a logout icon and handles logout clicks.
     *
     * @return A View representing the logout option.
     */
    private View getLogoutView() {
        if (!CometChatUIKit.isSDKInitialized()) return null;
        User user = CometChatUIKit.getLoggedInUser();
        if (user != null) {
            CometChatAvatar cometchatAvatar = new CometChatAvatar(requireContext());
            cometchatAvatar.setAvatar(user.getName(), user.getAvatar());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_40dp),
                getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_40dp)
            );
            layoutParams.setLayoutDirection(Gravity.CENTER_VERTICAL);
            cometchatAvatar.setLayoutParams(layoutParams);
            cometchatAvatar.setOnClickListener(v -> showCustomMenu(binding.cometchatConversations.getBinding().toolbar));
            return cometchatAvatar;
        }
        return null;
    }

    // Inside your Activity or Fragment
    private void showCustomMenu(View anchorView) {
        UserProfilePopupMenuLayoutBinding popupMenuBinding = UserProfilePopupMenuLayoutBinding.inflate(LayoutInflater.from(requireContext()));
        final PopupWindow popupWindow = new PopupWindow(
            popupMenuBinding.getRoot(),
            getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_250dp),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        );
        MyApplication.popupWindows.add(popupWindow);
        popupMenuBinding.tvUserName.setText(CometChatUIKit.getLoggedInUser().getName());
        String version = "V" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
        popupMenuBinding.tvVersion.setText(version);

        popupMenuBinding.tvCreateConversation.setOnClickListener(view -> {
            popupWindow.dismiss();
            listener.onItemClick();
        });

        popupMenuBinding.tvUserName.setOnClickListener(view -> popupWindow.dismiss());

        popupMenuBinding.tvLogout.setOnClickListener(view -> {
            Repository.unregisterFCMToken(new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String s) {
                    Repository.logout(new CometChat.CallbackListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            startActivity(new Intent(getContext(), SplashActivity.class));
                            requireActivity().finish();
                            FirebaseAuth.getInstance().signOut();
                        }

                        @Override
                        public void onError(CometChatException e) {
                            binding.cometchatConversations.setOverflowMenu(getLogoutView());
                        }
                    });
                }

                @Override
                public void onError(CometChatException e) {
                    binding.cometchatConversations.setOverflowMenu(getLogoutView());
                }
            });

            popupWindow.dismiss();
        });

        popupMenuBinding.tvUserName.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()));
        popupMenuBinding.tvCreateConversation.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()));
        popupMenuBinding.tvVersion.setTextColor(CometChatTheme.getTextColorPrimary(requireContext()));

        popupWindow.setElevation(5);

        int endMargin = getResources().getDimensionPixelSize(com.cometchat.chatuikit.R.dimen.cometchat_margin_2);
        int anchorWidth = anchorView.getWidth();
        int offsetX = anchorWidth - popupWindow.getWidth() - endMargin;
        int offsetY = 0;
        popupWindow.showAsDropDown(anchorView, offsetX, offsetY);
    }

    private void handleDeepLinking() {
        Bundle args = getArguments();
        if (args == null) return;

        String notificationType = args.getString(AppConstants.FCMConstants.NOTIFICATION_TYPE);
        String notificationPayload = args.getString(AppConstants.FCMConstants.NOTIFICATION_PAYLOAD);

        if (AppConstants.FCMConstants.NOTIFICATION_TYPE_MESSAGE.equals(notificationType) && notificationPayload != null) {
            FCMMessageDTO fcmMessage = parseFcmMessage(notificationPayload);
            if (fcmMessage != null) {
                handleFcmMessage(fcmMessage);
            }
        }
    }

    /**
     * Parse FCM message JSON safely.
     */
    private FCMMessageDTO parseFcmMessage(String payload) {
        try {
            return new Gson().fromJson(payload, FCMMessageDTO.class);
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Failed to parse FCMMessageDTO: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handle routing based on FCM message type.
     */
    private void handleFcmMessage(FCMMessageDTO fcmMessage) {
        boolean isUser = CometChatConstants.RECEIVER_TYPE_USER.equals(fcmMessage.getReceiverType());
        String uid = isUser ? fcmMessage.getSender() : fcmMessage.getReceiver();
        String messageId = fcmMessage.getTag();

        if (uid == null) return;

        if (isUser) {
            handleUserMessage(uid, messageId);
        } else {
            handleGroupMessage(uid, messageId);
        }
    }

    /**
     * Fetch user info and navigate.
     */
    private void handleUserMessage(String uid, @Nullable String messageId) {
        Repository.getUser(uid, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (messageId != null) {
                    fetchMessageAndNavigate(user, null, messageId);
                } else {
                    navigateToUserChat(user, null, null);
                }
            }

            @Override
            public void onError(CometChatException e) {
                showToast(e.getMessage());
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

    /**
     * Fetch group info and navigate.
     */
    private void handleGroupMessage(String uid, String messageId) {
        Repository.getGroup(uid, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (messageId != null) {
                    fetchMessageAndNavigate(null, group, messageId);
                } else {
                    navigateToGroupChat(group, null, null);
                }
            }

            @Override
            public void onError(CometChatException e) {
                showToast(e.getMessage());
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }


    /**
     * Fetch message and parent message if exists, then navigate.
     */
    private void fetchMessageAndNavigate(User user, Group group, String messageId) {
        Repository.fetchMessageInformation(Long.parseLong(messageId), new CometChat.CallbackListener<BaseMessage>() {
            @Override
            public void onSuccess(BaseMessage baseMessage) {
                if (baseMessage != null && baseMessage.getParentMessageId() != 0L) {
                    Repository.fetchMessageInformation(baseMessage.getParentMessageId(), new CometChat.CallbackListener<BaseMessage>() {
                        @Override
                        public void onSuccess(BaseMessage parentMessage) {
                            if (user != null) {
                                navigateToUserChat(user, parentMessage, baseMessage);
                            } else if (group != null) {
                                navigateToGroupChat(group, parentMessage, baseMessage);
                            }
                        }

                        @Override
                        public void onError(CometChatException e) {
                            CometChatLogger.e(TAG, e.toString());
                            navigateToUserChat(user, null, baseMessage);
                        }
                    });
                } else {
                    if (user != null) {
                        navigateToUserChat(user, null, baseMessage);
                    } else if (group != null) {
                        navigateToGroupChat(group, null, baseMessage);
                    }
                }
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, e.toString());
                navigateToUserChat(user, null, null);
            }
        });
    }

    /**
     * Navigation helpers
     */
    private void navigateToUserChat(User user, @Nullable BaseMessage parentMessage, @Nullable BaseMessage goToMessage) {

        Intent intent;
        if (parentMessage != null) {
            intent = new Intent(requireContext(), ThreadMessageActivity.class);
            intent.putExtra(AppConstants.JSONConstants.REPLY_COUNT, parentMessage.getReplyCount());
        } else {
            intent = new Intent(requireContext(), MessagesActivity.class);
        }

        if (user != null) {
            intent.putExtra(getString(R.string.app_user), user.toJson().toString());
        }
        if (goToMessage != null) {
            intent.putExtra(getString(R.string.app_go_to_message), goToMessage.getRawMessage().toString());
        }
        if (parentMessage != null) {
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessage.getRawMessage().toString());
        }

        startActivity(intent);
    }

    private void navigateToGroupChat(Group group, @Nullable BaseMessage parentMessage, @Nullable BaseMessage goToMessage) {
        Intent intent;
        if (parentMessage != null) {
            intent = new Intent(requireContext(), ThreadMessageActivity.class);
            intent.putExtra(AppConstants.JSONConstants.REPLY_COUNT, parentMessage.getReplyCount());
        } else {
            intent = new Intent(requireContext(), MessagesActivity.class);
        }

        intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
        if (parentMessage != null) {
            intent.putExtra(AppConstants.JSONConstants.RAW_JSON, parentMessage.getRawMessage().toString());
        }
        if (goToMessage != null) {
            intent.putExtra(getString(R.string.app_go_to_message), goToMessage.getRawMessage().toString());
        }

        startActivity(intent);
    }

    /**
     * Generic error handler.
     */
    private void showToast(@Nullable String message) {
        Toast.makeText(getContext(), message != null ? message : "Unknown error", Toast.LENGTH_SHORT).show();
    }
}
