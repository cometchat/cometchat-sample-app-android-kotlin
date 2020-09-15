package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.R;

import java.util.ArrayList;
import java.util.List;

import utils.Utils;

public class ReceiptListAdapter extends RecyclerView.Adapter<ReceiptListAdapter.ReceiptsHolder> {

    private Context context;

    private List<MessageReceipt> messageReceiptList = new ArrayList<>();
    private String loggedInUserUid = CometChat.getLoggedInUser().getUid();

    public ReceiptListAdapter(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public ReceiptsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ReceiptsHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cometchat_receipts_view,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptsHolder receiptsHolder, int i) {
        MessageReceipt messageReceipt=messageReceiptList.get(i);

        receiptsHolder.tvName.setText(messageReceipt.getSender().getName());
        if (messageReceipt.getReadAt()!=0)
            receiptsHolder.tvRead.setText(Utils.getReceiptDate(messageReceipt.getReadAt()));
        if (messageReceipt.getDeliveredAt()!=0)
            receiptsHolder.tvDelivery.setText(Utils.getReceiptDate(messageReceipt.getDeliveredAt()));
        if (messageReceipt.getSender().getAvatar()!=null)
            receiptsHolder.ivAvatar.setAvatar(messageReceipt.getSender().getAvatar());
        else
            receiptsHolder.ivAvatar.setInitials(messageReceipt.getSender().getName());
    }

    @Override
    public int getItemCount() {
        return messageReceiptList.size();
    }

    public void add(MessageReceipt messageReceipt) {
        messageReceiptList.add(messageReceipt);
        notifyItemChanged(messageReceiptList.size()-1);
    }

    public void addAtIndex(int index, MessageReceipt messageReceipt) {
        messageReceiptList.add(index,messageReceipt);
        notifyItemChanged(index);
    }

    public void updateReceipts(MessageReceipt messageReceipt) {
        if (messageReceiptList.contains(messageReceipt)) {
            int index = messageReceiptList.indexOf(messageReceipt);
            messageReceiptList.remove(index);
            messageReceiptList.add(index,messageReceipt);
            notifyItemChanged(index);
        } else {
            messageReceiptList.add(messageReceipt);
            notifyItemChanged(messageReceiptList.size()-1);
        }
    }

    public void clear() {
        messageReceiptList.clear();
        notifyDataSetChanged();
    }

    public void updateList(List<MessageReceipt> messageReceiptsList) {
        for (MessageReceipt messageReceipt : messageReceiptsList) {
            if (!messageReceipt.getSender().getUid().equals(loggedInUserUid))
                updateReceipts(messageReceipt);
        }
    }

    public void updateReceiptsAtIndex(int index, MessageReceipt messageReceipt) {
        messageReceiptList.remove(index);
        messageReceiptList.add(messageReceipt);
        notifyItemChanged(index);
    }

    class ReceiptsHolder extends RecyclerView.ViewHolder {

        TextView tvDelivery;
        TextView tvRead;
        Avatar ivAvatar;
        TextView tvName;

        ReceiptsHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tvName);
            tvDelivery=itemView.findViewById(R.id.tvDelivery);
            tvRead=itemView.findViewById(R.id.tvRead);
            ivAvatar=itemView.findViewById(R.id.ivAvatar);
        }
    }
}

