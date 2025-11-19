package com.cometchat.sampleapp.java.ui.fragments;

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
import com.cometchat.chatuikit.conversations.CometChatConversations;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.avatar.CometChatAvatar;
import com.cometchat.sampleapp.java.BuildConfig;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.data.interfaces.OnItemClickListener;
import com.cometchat.sampleapp.java.data.repository.Repository;
import com.cometchat.sampleapp.java.databinding.FragmentChatsBinding;
import com.cometchat.sampleapp.java.databinding.UserProfilePopupMenuLayoutBinding;
import com.cometchat.sampleapp.java.ui.activity.MessagesActivity;
import com.cometchat.sampleapp.java.ui.activity.SearchActivity;
import com.cometchat.sampleapp.java.ui.activity.SplashActivity;
import com.cometchat.sampleapp.java.utils.MyApplication;
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
                Intent intent = new Intent(getContext(), MessagesActivity.class);
                intent.putExtra(getString(R.string.app_group), new Gson().toJson(group));
                startActivity(intent);
            } else {
                User user = (User) conversation.getConversationWith();
                Intent intent = new Intent(getContext(), MessagesActivity.class);
                intent.putExtra(getString(R.string.app_user), new Gson().toJson(user));
                startActivity(intent);
            }
        });
        // Set the overflow menu (Logout button) in the Conversations view
        binding.cometchatConversations.setOverflowMenu(getLogoutView());

        binding.cometchatConversations.setOnSearchClickListener(() -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
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

        popupMenuBinding.tvUserName.setOnClickListener(view -> {
            popupWindow.dismiss();
        });

        popupMenuBinding.tvLogout.setOnClickListener(view -> {
            Repository.logout(new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String s) {
                    startActivity(new Intent(getContext(), SplashActivity.class));
                    requireActivity().finish();
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

}
