package com.cometchat.chatuikit.shared.utils;

import static android.view.View.VISIBLE;

import android.content.Context;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu;

import java.util.ArrayList;
import java.util.List;

public class MembersUtils {
    private static final String TAG = MembersUtils.class.getSimpleName();

    public static List<CometChatPopupMenu.MenuItem> getDefaultGroupMemberOptions(Context context,
                                                                                 GroupMember groupMember,
                                                                                 Group group,
                                                                                 OnClick click,
                                                                                 int kickMemberOptionVisibility,
                                                                                 int banMemberOptionVisibility,
                                                                                 int scopeChangeOptionVisibility) {
        List<CometChatPopupMenu.MenuItem> menuItems = new ArrayList<>();
        if (group != null) {
            if (CometChatConstants.SCOPE_MODERATOR.equalsIgnoreCase(group.getScope())) {
                if (CometChatConstants.SCOPE_PARTICIPANT.equalsIgnoreCase(groupMember.getScope())) {
                    menuItems.addAll(getDefaultOptions(context,
                                                       click,
                                                       kickMemberOptionVisibility,
                                                       banMemberOptionVisibility,
                                                       scopeChangeOptionVisibility));
                }
            } else if (CometChatConstants.SCOPE_ADMIN.equalsIgnoreCase(group.getScope())) {
                if (!group
                    .getOwner()
                    .equalsIgnoreCase(groupMember.getUid()) && !CometChatConstants.SCOPE_ADMIN.equalsIgnoreCase(groupMember.getScope())) {
                    menuItems.addAll(getDefaultOptions(context,
                                                       click,
                                                       kickMemberOptionVisibility,
                                                       banMemberOptionVisibility,
                                                       scopeChangeOptionVisibility));
                } else if (group.getOwner() != null && group.getOwner().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) && !groupMember
                    .getUid()
                    .equals(group.getOwner())) {
                    menuItems.addAll(getDefaultOptions(context,
                                                       click,
                                                       kickMemberOptionVisibility,
                                                       banMemberOptionVisibility,
                                                       scopeChangeOptionVisibility));
                }
            }
        }
        return menuItems;
    }

    private static List<CometChatPopupMenu.MenuItem> getDefaultOptions(Context context, OnClick click,
                                                                       int kickMemberOptionVisibility,
                                                                       int banMemberOptionVisibility,
                                                                       int scopeChangeOptionVisibility) {

        List<CometChatPopupMenu.MenuItem> optionsList = new ArrayList<>();
        if (scopeChangeOptionVisibility == VISIBLE)
            optionsList.add(new CometChatPopupMenu.MenuItem(UIKitConstants.GroupMemberOption.CHANGE_SCOPE,
                                                            context.getResources().getString(R.string.cometchat_scope_change),
                                                            click));
        if (banMemberOptionVisibility == VISIBLE)
            optionsList.add(new CometChatPopupMenu.MenuItem(UIKitConstants.GroupMemberOption.BAN,
                                                            context.getResources().getString(R.string.cometchat_ban),
                                                            click));
        if (kickMemberOptionVisibility == VISIBLE)
            optionsList.add(new CometChatPopupMenu.MenuItem(UIKitConstants.GroupMemberOption.KICK,
                                                            context.getResources().getString(R.string.cometchat_kick),
                                                            click));
        return optionsList;
    }

    public static GroupMember userToGroupMember(User user, boolean isScopeUpdate, String newScope) {
        GroupMember groupMember;
        if (isScopeUpdate) groupMember = new GroupMember(user.getUid(), newScope);
        else groupMember = new GroupMember(user.getUid(), CometChatConstants.SCOPE_PARTICIPANT);

        groupMember.setAvatar(user.getAvatar());
        groupMember.setName(user.getName());
        groupMember.setStatus(user.getStatus());
        return groupMember;
    }
}
