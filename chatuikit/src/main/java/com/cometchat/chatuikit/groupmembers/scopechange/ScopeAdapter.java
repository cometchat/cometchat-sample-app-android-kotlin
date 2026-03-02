package com.cometchat.chatuikit.groupmembers.scopechange;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.GroupMember;
import com.cometchat.chatuikit.R;

import java.util.List;

public class ScopeAdapter extends RecyclerView.Adapter<ScopeAdapter.RoleViewHolder> {
    private static final String TAG = ScopeAdapter.class.getSimpleName();
    private final Context context;
    private List<String> roles;
    private int selectedPosition = -1; // To keep track of the selected position
    private int initialPosition = -1;
    private Group group;

    private @ColorInt int itemTextColor;
    private @ColorInt int disableItemTextColor;
    private @StyleRes int itemTextAppearance;
    private @ColorInt int disableRadioButtonTint;
    private @ColorInt int radioButtonTint;
    private OnScopeSelectionChangeListener onScopeSelectionChangeListener;

    public ScopeAdapter(Context context, List<String> roles) {
        this.context = context;
        this.roles = roles;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cometchat_list_scope_change_item_radio, parent, false);
        return new RoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String role = roles.get(position);
        holder.textViewRole.setText(role);

        holder.radioButton.setChecked(holder.getAbsoluteAdapterPosition() == selectedPosition);
        holder.textViewRole.setTextAppearance(itemTextAppearance);
        if ((CometChatConstants.SCOPE_MODERATOR.equalsIgnoreCase(group.getScope()) && role.equalsIgnoreCase(CometChatConstants.SCOPE_ADMIN))) {
            holder.radioButton.setEnabled(false);
            holder.radioButton.setButtonTintList(ColorStateList.valueOf(disableRadioButtonTint));
            holder.textViewRole.setTextColor(disableItemTextColor);
        } else {
            holder.textViewRole.setTextColor(itemTextColor);
            holder.radioButton.setButtonTintList(ColorStateList.valueOf(radioButtonTint));
            holder.radioButton.setEnabled(true);
            // Handle the item click
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = holder.getAbsoluteAdapterPosition();
                notifyDataSetChanged();
                notifySelectionChangeListener();
            });

            // Handle the RadioButton click
            holder.radioButton.setOnClickListener(v -> {
                selectedPosition = holder.getAbsoluteAdapterPosition();
                notifyDataSetChanged();
                notifySelectionChangeListener();
            });
        }
    }

    public void setItemTextColor(@ColorInt int itemTextColor) {
        this.itemTextColor = itemTextColor;
    }

    public void setDisableItemTextColor(@ColorInt int disableItemTextColor) {
        this.disableItemTextColor = disableItemTextColor;
    }

    public void setItemTextAppearance(@StyleRes int itemTextAppearance) {
        this.itemTextAppearance = itemTextAppearance;
    }

    public void setDisableRadioButtonTint(@ColorInt int disableRadioButtonTint) {
        this.disableRadioButtonTint = disableRadioButtonTint;
    }

    public void setRadioButtonTint(@ColorInt int radioButtonTint) {
        this.radioButtonTint = radioButtonTint;
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    public String getSelectedRole() {
        if (selectedPosition != -1) {
            return roles.get(selectedPosition);
        }
        return null;
    }

    public void setGroupData(List<String> roles, @Nullable GroupMember member, Group group) {
        if (roles != null && member != null && group != null) {
            this.group = group;
            this.roles = roles;
            if (member.getScope().equalsIgnoreCase(CometChatConstants.SCOPE_ADMIN)) {
                selectedPosition = 0;
            } else if (member.getScope().equalsIgnoreCase(CometChatConstants.SCOPE_MODERATOR)) {
                selectedPosition = 1;
            } else {
                selectedPosition = 2;
            }
            initialPosition = selectedPosition;
            notifyDataSetChanged();
            notifySelectionChangeListener();
        }
    }

    public void setOnScopeSelectionChangeListener(OnScopeSelectionChangeListener listener) {
        this.onScopeSelectionChangeListener = listener;
    }

    private void notifySelectionChangeListener() {
        if (onScopeSelectionChangeListener != null) {
            onScopeSelectionChangeListener.onSelectionChanged(selectedPosition == initialPosition);
        }
    }

    public interface OnScopeSelectionChangeListener {
        void onSelectionChanged(boolean isSameAsOriginal);
    }

    public static class RoleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRole;
        AppCompatRadioButton radioButton;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
