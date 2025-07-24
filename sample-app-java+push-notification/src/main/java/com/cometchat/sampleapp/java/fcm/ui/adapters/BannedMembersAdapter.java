package com.cometchat.sampleapp.java.fcm.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.sampleapp.java.fcm.databinding.BannedMemberRowBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of banned group members in a
 * RecyclerView.
 */
public class BannedMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<GroupMember> groupMemberList;
    private Group group;

    /**
     * Constructor for BannedMembersAdapter.
     *
     * @param context The context of the calling activity or fragment.
     */
    public BannedMembersAdapter(Context context) {
        this.context = context;
        groupMemberList = new ArrayList<>(); // Initialize the list of group members
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BannedMemberRowBinding binding = BannedMemberRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMember groupMember = groupMemberList.get(position); // Get the group member at the specified position
        ((MyViewHolder) holder).bindView(groupMember); // Bind the group member data to the view holder
    }

    @Override
    public int getItemCount() {
        return groupMemberList.size(); // Return the total number of banned members
    }

    /**
     * Gets the list of banned group members.
     *
     * @return The current list of group members.
     */
    public List<GroupMember> getGroupMemberList() {
        return groupMemberList; // Return the list of group members
    }

    /**
     * Sets the list of banned group members.
     *
     * @param list The list of group members to be displayed.
     */
    public void setGroupMemberList(List<GroupMember> list) {
        if (list != null) {
            this.groupMemberList = list; // Update the group member list
            notifyDataSetChanged(); // Notify the adapter to refresh the views
        }
    }

    /**
     * Gets the context of the adapter.
     *
     * @return The context associated with the adapter.
     */
    public Context getContext() {
        return context; // Return the context
    }

    /**
     * Gets the group associated with the banned members.
     *
     * @return The group instance.
     */
    public Group getGroup() {
        return group; // Return the group reference
    }

    /**
     * Sets the group associated with the banned members.
     *
     * @param group The group instance.
     */
    public void setGroup(Group group) {
        this.group = group; // Update the group reference
        notifyDataSetChanged(); // Notify the adapter to refresh the views
    }

    /**
     * ViewHolder class for representing a single banned member in the RecyclerView.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        BannedMemberRowBinding binding; // Binding for the banned member row layout

        /**
         * Constructor for MyViewHolder.
         *
         * @param itemView The view representing a single banned member row.
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = BannedMemberRowBinding.bind(itemView); // Bind the layout to this view holder
        }

        /**
         * Binds the data of a group member to the views in the ViewHolder.
         *
         * @param groupMember The group member data to bind.
         */
        public void bindView(GroupMember groupMember) {
            binding.tvMemberTitle.setTextColor(CometChatTheme.getTextColorPrimary(itemView.getContext()));// Set the text color for the member's name
            binding.ivUnbanMember.setColorFilter(CometChatTheme.getIconTintSecondary(itemView.getContext())); // Set the color filter for the unban icon
            binding.memberAvatar.setAvatar(groupMember.getName(), groupMember.getAvatar()); // Set the member's avatar
            binding.tvMemberTitle.setText(groupMember.getName()); // Set the member's name
            itemView.setTag(R.string.cometchat_member, groupMember); // Set a tag for the view
        }
    }
}
