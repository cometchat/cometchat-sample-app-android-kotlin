package com.cometchat.chatuikit.groupmembers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatGroupMemberListItemBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.viewholders.GroupMembersViewHolderListeners;
import com.cometchat.chatuikit.shared.views.statusindicator.StatusIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = GroupMembersAdapter.class.getSimpleName();
    private final Context context;
    private final @ColorInt int ownerScopeChipBackgroundColor;
    private final @ColorInt int ownerScopeChipTextColor;
    private final @ColorInt int scopeChipTextColor;
    private final @ColorInt int scopeChipBackgroundColor;
    private final @Dimension int scopeChipStrokeColor;
    private final @Dimension int scopeChipStrokeWidth;
    private List<GroupMember> groupMemberList;
    private HashMap<GroupMember, Boolean> selectedGroupMembers;
    private GroupMembersViewHolderListeners subTitleViewHolder, tailViewHolder, listItemView, titleView, leadingView;
    private Group group;
    private boolean hideUserStatus;
    private @ColorInt int itemTitleTextColor;
    private @StyleRes int itemTitleTextAppearance;
    private @ColorInt int onlineStatusColor;
    private @StyleRes int avatarStyle;
    private @StyleRes int statusIndicatorStyle;
    private Drawable selectionIcon;
    private boolean isSelectionEnabled;
    private @Dimension int checkBoxStrokeWidth;
    private @Dimension int checkBoxCornerRadius;
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;
    private Drawable selectIcon;
    private @ColorInt int selectIconTint;

    public GroupMembersAdapter(Context context) {
        this.context = context;
        groupMemberList = new ArrayList<>();
        selectGroupMember(new HashMap<>());
        ownerScopeChipBackgroundColor = CometChatTheme.getPrimaryColor(context);
        ownerScopeChipTextColor = CometChatTheme.getColorWhite(context);
        scopeChipTextColor = CometChatTheme.getTextColorHighlight(context);
        scopeChipBackgroundColor = CometChatTheme.getExtendedPrimaryColor100(context);
        scopeChipStrokeColor = CometChatTheme.getPrimaryColor(context);
        scopeChipStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.cometchat_1dp);
    }

    public void selectGroupMember(HashMap<GroupMember, Boolean> hashMap) {
        if (hashMap != null) {
            this.selectedGroupMembers = hashMap;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CometchatGroupMemberListItemBinding binding = CometchatGroupMemberListItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                                                                                                  parent,
                                                                                                  false);
        return new MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMember groupMember = groupMemberList.get(position);
        ((MyViewHolder) holder).bindView(groupMember, position);
        holder.itemView.setTag(R.string.cometchat_member, groupMember);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getItemCount() {
        return groupMemberList.size();
    }

    public void setSelectionEnabled(boolean selectionEnabled) {
        isSelectionEnabled = selectionEnabled;
        notifyDataSetChanged();
    }

    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
        notifyDataSetChanged();
    }

    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
        notifyDataSetChanged();
    }

    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
        notifyDataSetChanged();
    }

    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
        notifyDataSetChanged();
    }

    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
        notifyDataSetChanged();
    }

    public void setSelectIcon(Drawable selectIcon) {
        this.selectIcon = selectIcon;
        notifyDataSetChanged();
    }

    public void setSelectIconTint(@ColorInt int selectIconTint) {
        this.selectIconTint = selectIconTint;
        notifyDataSetChanged();
    }

    public void hideUserStatus(boolean hide) {
        this.hideUserStatus = hide;
        notifyDataSetChanged();
    }

    public GroupMember getGroupMember(int pos) {
        return groupMemberList.get(pos);
    }

    public List<GroupMember> getGroupMemberList() {
        return groupMemberList;
    }

    public void setGroupMemberList(List<GroupMember> list) {
        if (list != null) {
            this.groupMemberList = list;
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return context;
    }

    public HashMap<GroupMember, Boolean> getSelectedGroupMembers() {
        return selectedGroupMembers;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
            notifyDataSetChanged();
        }
    }

    public boolean isHideUserStatus() {
        return hideUserStatus;
    }

    public int getOnlineStatusColor() {
        return onlineStatusColor;
    }

    public void setOnlineStatusColor(int onlineStatusColor) {
        if (onlineStatusColor != 0) {
            this.onlineStatusColor = onlineStatusColor;
            notifyDataSetChanged();
        }
    }

    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        notifyDataSetChanged();
    }

    public @StyleRes int getStatusIndicatorStyle() {
        return statusIndicatorStyle;
    }

    public void setStatusIndicatorStyle(@StyleRes int statusIndicatorStyle) {
        this.statusIndicatorStyle = statusIndicatorStyle;
        notifyDataSetChanged();
    }

    public Drawable getSelectionIcon() {
        return selectionIcon;
    }

    public void setSelectionIcon(Drawable selectionIcon) {
        if (selectionIcon != null) {
            this.selectionIcon = selectionIcon;
            notifyDataSetChanged();
        }
    }

    public void setItemView(GroupMembersViewHolderListeners listItemView) {
        if (listItemView != null) {
            this.listItemView = listItemView;
            notifyDataSetChanged();
        }
    }

    public void setSubtitleView(GroupMembersViewHolderListeners subtitleView) {
        if (subtitleView != null) {
            this.subTitleViewHolder = subtitleView;
            notifyDataSetChanged();
        }
    }

    public void setTrailingView(GroupMembersViewHolderListeners tailView) {
        if (tailView != null) {
            this.tailViewHolder = tailView;
            notifyDataSetChanged();
        }
    }

    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
    }

    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
    }

    public void setTitleView(GroupMembersViewHolderListeners titleView) {
        this.titleView = titleView;
        notifyDataSetChanged();
    }

    public void setLeadingView(GroupMembersViewHolderListeners leadingView) {
        this.leadingView = leadingView;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CometchatGroupMemberListItemBinding binding;
        private View customListItemView, customSubtitleView, customTailView, customLeadingView, customTitleView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CometchatGroupMemberListItemBinding.bind(itemView);

            if (listItemView != null) {
                customListItemView = listItemView.createView(context, binding);
                binding.parentLayout.removeAllViews();
                Utils.handleView(binding.parentLayout, customListItemView, true);
            } else {
                if (leadingView != null) {
                    customLeadingView = leadingView.createView(context, binding);
                    Utils.handleView(binding.leadingView, customLeadingView, true);
                }

                if (titleView != null) {
                    customTitleView = titleView.createView(context, binding);
                    Utils.handleView(binding.titleView, customTitleView, true);
                }
                // Handle subtitle view
                if (subTitleViewHolder != null) {
                    customSubtitleView = subTitleViewHolder.createView(context, binding);
                    Utils.handleView(binding.subtitleView, customSubtitleView, true);
                }
                // Handle tail view
                if (tailViewHolder != null) {
                    customTailView = tailViewHolder.createView(context, binding);
                    Utils.handleView(binding.tailView, customTailView, true);
                }
            }
        }

        public void bindView(GroupMember groupMember, int position) {
            if (listItemView != null) {
                listItemView.bindView(context, customListItemView, groupMember, group, this, groupMemberList, position);
            } else {

                if (isSelectionEnabled) {
                    Utils.initMaterialCard(binding.checkboxView);
                    binding.ivCheckbox.setImageDrawable(selectIcon);
                    if (selectIconTint != 0) binding.ivCheckbox.setColorFilter(selectIconTint);
                    binding.checkboxView.setStrokeWidth(checkBoxStrokeWidth);
                    binding.checkboxView.setStrokeColor(ColorStateList.valueOf(checkBoxStrokeColor));
                    binding.checkboxView.setRadius(checkBoxCornerRadius);
                    if (!selectedGroupMembers.isEmpty() && selectedGroupMembers.containsKey(groupMember)) {
                        binding.ivCheckbox.setVisibility(View.VISIBLE);
                        binding.checkboxView.setStrokeWidth(0);
                        binding.checkboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor);
                    } else {
                        binding.ivCheckbox.setVisibility(View.GONE);
                        binding.checkboxView.setCardBackgroundColor(checkBoxBackgroundColor);
                    }
                    binding.checkboxView.setVisibility(View.VISIBLE);
                } else {
                    binding.checkboxView.setVisibility(View.GONE);
                }

                if (leadingView != null)
                    leadingView.bindView(context, customLeadingView, groupMember, group, this, groupMemberList, position);
                else {
                    binding.memberAvatar.setAvatar(groupMember.getName(), groupMember.getAvatar());
                    binding.memberStatusIndicator.setStatusIndicator(StatusIndicator.OFFLINE);
                    if (groupMember.getStatus().equalsIgnoreCase(CometChatConstants.USER_STATUS_ONLINE)) {
                        if (!Utils.isBlocked(groupMember)) {
                            binding.memberStatusIndicator.setStatusIndicator(StatusIndicator.ONLINE);
                            binding.memberStatusIndicator.setStatusIndicator(groupMember
                                    .getStatus()
                                    .equalsIgnoreCase(CometChatConstants.USER_STATUS_ONLINE) && !hideUserStatus ? StatusIndicator.ONLINE : StatusIndicator.OFFLINE);
                        } else binding.memberStatusIndicator.setVisibility(View.GONE);
                    } else {
                        binding.memberStatusIndicator.setVisibility(View.GONE);
                    }

                    binding.memberAvatar.setStyle(avatarStyle);
                }

                if (titleView == null) {
                    binding.tvMemberTitle.setText(groupMember.getUid().equalsIgnoreCase(CometChatUIKit.getLoggedInUser().getUid()) ? binding
                        .getRoot()
                        .getContext()
                        .getString(R.string.cometchat_you) : groupMember.getName());
                } else {
                    titleView.bindView(context, customTitleView, groupMember, group, this, groupMemberList, position);
                }

                if (subTitleViewHolder != null) {
                    subTitleViewHolder.bindView(context, customSubtitleView, groupMember, group, this, groupMemberList, position);
                }

                if (tailViewHolder != null) {
                    tailViewHolder.bindView(context, customTailView, groupMember, group, this, groupMemberList, position);
                }

                binding.scopeCard.setVisibility(View.VISIBLE);
                // Get the current layout parameters
                ViewGroup.LayoutParams layoutParams = binding.scopeCard.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.scopeCard.setLayoutParams(layoutParams);

                if (CometChatConstants.SCOPE_PARTICIPANT.equalsIgnoreCase(groupMember.getScope())) {
                    binding.scopeCard.setVisibility(View.GONE);
                } else if (groupMember.getUid().equals(group.getOwner())) {
                    binding.scopeCard.setCardBackgroundColor(ownerScopeChipBackgroundColor);
                    binding.tvScope.setTextColor(ownerScopeChipTextColor);
                    binding.scopeCard.setStrokeColor(scopeChipStrokeColor);
                    binding.tvScope.setText(context.getResources().getString(R.string.cometchat_owner));
                } else if (UIKitConstants.GroupMemberScope.ADMIN.equals(groupMember.getScope())) {
                    binding.scopeCard.setCardBackgroundColor(scopeChipBackgroundColor);
                    binding.scopeCard.setStrokeWidth(scopeChipStrokeWidth);
                    binding.scopeCard.setStrokeColor(scopeChipStrokeColor);
                    String scopeText = groupMember.getScope().substring(0, 1).toUpperCase() + groupMember.getScope().substring(1);
                    binding.tvScope.setText(scopeText);
                    binding.tvScope.setTextColor(scopeChipTextColor);
                } else if (UIKitConstants.GroupMemberScope.MODERATOR.equals(groupMember.getScope())) {
                    binding.scopeCard.setCardBackgroundColor(scopeChipBackgroundColor);
                    binding.scopeCard.setStrokeWidth(0);
                    String scopeText = groupMember.getScope().substring(0, 1).toUpperCase() + groupMember.getScope().substring(1);
                    binding.tvScope.setText(scopeText);
                    binding.tvScope.setTextColor(scopeChipTextColor);
                }

                binding.tvMemberTitle.setTextAppearance(itemTitleTextAppearance);
                binding.tvMemberTitle.setTextColor(itemTitleTextColor);
                binding.memberAvatar.setStyle(avatarStyle);
                binding.memberStatusIndicator.setStyle(statusIndicatorStyle);
            }

            itemView.setTag(R.string.cometchat_member, groupMember);
        }
    }
}
