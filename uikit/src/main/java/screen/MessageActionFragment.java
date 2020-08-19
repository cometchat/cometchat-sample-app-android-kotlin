package screen;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cometchat.pro.uikit.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import screen.messagelist.CometChatMessageListActivity;
import screen.threadconversation.CometChatThreadMessageActivity;

public class MessageActionFragment extends BottomSheetDialogFragment {

    private TextView threadMessage;
    private TextView editMessage;
    private TextView replyMessage;
    private TextView forwardMessage;
    private TextView deleteMessage;
    private TextView copyMessage;
    private TextView shareMessage;

    private boolean isShareVisible;
    private boolean isThreadVisible;
    private boolean isCopyVisible;
    private boolean isEditVisible;
    private boolean isDeleteVisible;
    private boolean isForwardVisible;
    private boolean isReplyVisible;

    private MessageActionListener messageActionListener;

    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchArguments();
    }

    private void fetchArguments() {
        if (getArguments()!=null) {
            isCopyVisible = getArguments().getBoolean("copyVisible");
            isThreadVisible = getArguments().getBoolean("threadVisible");
            isEditVisible = getArguments().getBoolean("editVisible");
            isDeleteVisible = getArguments().getBoolean("deleteVisible");
            isReplyVisible = getArguments().getBoolean("replyVisible");
            isForwardVisible = getArguments().getBoolean("forwardVisible");
            isShareVisible = getArguments().getBoolean("shareVisible");
            type = getArguments().getString("type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_actions, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                // androidx should use: com.google.android.material.R.id.design_bottom_sheet
                FrameLayout bottomSheet = (FrameLayout)
                        dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setPeekHeight(0);
            }
        });
        threadMessage = view.findViewById(R.id.start_thread);
        editMessage = view.findViewById(R.id.edit_message);
        replyMessage = view.findViewById(R.id.reply_message);
        forwardMessage = view.findViewById(R.id.forward_message);
        deleteMessage = view.findViewById(R.id.delete_message);
        copyMessage = view.findViewById(R.id.copy_message);
        shareMessage = view.findViewById(R.id.share_message);

        if (isThreadVisible)
            threadMessage.setVisibility(View.VISIBLE);
        else
            threadMessage.setVisibility(View.GONE);
        if (isCopyVisible)
            copyMessage.setVisibility(View.VISIBLE);
        else
            copyMessage.setVisibility(View.GONE);
        if (isEditVisible)
            editMessage.setVisibility(View.VISIBLE);
        else
            editMessage.setVisibility(View.GONE);
        if (isDeleteVisible)
            deleteMessage.setVisibility(View.VISIBLE);
        else
            deleteMessage.setVisibility(View.GONE);
        if (isReplyVisible)
            replyMessage.setVisibility(View.VISIBLE);
        else
            replyMessage.setVisibility(View.GONE);
        if (isForwardVisible)
            forwardMessage.setVisibility(View.VISIBLE);
        else
            forwardMessage.setVisibility(View.GONE);
        if (isShareVisible)
            shareMessage.setVisibility(View.VISIBLE);
        else
            shareMessage.setVisibility(View.GONE);

        if (type!=null && type.equals(CometChatThreadMessageActivity.class.getName())) {
            threadMessage.setVisibility(View.GONE);
        }

        threadMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onThreadMessageClick();
                dismiss();
            }
        });
        copyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onCopyMessageClick();
                dismiss();
            }
        });
        editMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onEditMessageClick();
                dismiss();
            }
        });
        deleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onDeleteMessageClick();
                dismiss();
            }
        });
        replyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onReplyMessageClick();
                dismiss();
            }
        });
        forwardMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onForwardMessageClick();
                dismiss();
            }
        });
        shareMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener!=null)
                    messageActionListener.onShareMessageClick();
                dismiss();
            }
        });

        return view;
    }


    public void setMessageActionListener(MessageActionListener messageActionListener) {
        this.messageActionListener = messageActionListener;

    }

    public interface MessageActionListener {
        void onThreadMessageClick();
        void onEditMessageClick();
        void onReplyMessageClick();
        void onForwardMessageClick();
        void onDeleteMessageClick();
        void onCopyMessageClick();
        void onShareMessageClick();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity!=null)
            if (type!=null && type== CometChatMessageListActivity.class.getName())
                ((CometChatMessageListActivity)activity).handleDialogClose(dialog);
            else
                ((CometChatThreadMessageActivity)activity).handleDialogClose(dialog);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity!=null)
            if (type!=null && type==CometChatMessageListActivity.class.getName())
                ((CometChatMessageListActivity)activity).handleDialogClose(dialog);
            else
                ((CometChatThreadMessageActivity)activity).handleDialogClose(dialog);
    }
}