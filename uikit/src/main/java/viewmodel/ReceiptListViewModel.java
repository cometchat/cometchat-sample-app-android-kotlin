package viewmodel;

import android.content.Context;

import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.CometChatReceiptsList;
import com.cometchat.pro.uikit.CometChatUserList;

import java.util.List;

import adapter.ReceiptListAdapter;
import adapter.UserListAdapter;
import listeners.StickyHeaderDecoration;

public class ReceiptListViewModel {

    private static final String TAG = "ReceiptListViewModel";

    private  Context context;

    private ReceiptListAdapter receiptListAdapter;

    private CometChatReceiptsList receiptsList;



    public ReceiptListViewModel(Context context, CometChatReceiptsList receiptsList,
                                boolean showDelivery,boolean showRead){
        this.receiptsList = receiptsList;
        this.context=context;
        setReceiptListAdapter(receiptsList,showDelivery,showRead);
    }

    private ReceiptListViewModel(){

    }

    private ReceiptListAdapter getAdapter() {
        if (receiptListAdapter==null){
            receiptListAdapter=new ReceiptListAdapter(context);
        }
        return receiptListAdapter;
    }

    public void add(MessageReceipt messageReceipt){
        if (receiptListAdapter!=null)
            receiptListAdapter.add(messageReceipt);

    }
    public void add(int index,MessageReceipt messageReceipt){
        if (receiptListAdapter!=null)
            receiptListAdapter.addAtIndex(index,messageReceipt);

    }

    public void update(MessageReceipt messageReceipt){
        if (receiptListAdapter!=null)
            receiptListAdapter.updateReceipts(messageReceipt);

    }

    public void clear()
    {
        if (receiptListAdapter!=null)
            receiptListAdapter.clear();
    }
    private void setReceiptListAdapter(CometChatReceiptsList cometChatReceiptsList,
                                       boolean showDelivery,boolean showRead){
        receiptListAdapter=new ReceiptListAdapter(context);
        cometChatReceiptsList.showDelivery(showDelivery);
        cometChatReceiptsList.showRead(showRead);
        cometChatReceiptsList.setAdapter(receiptListAdapter);
    }

    public void setReceiptList(List<MessageReceipt> messageReceiptsList){
          getAdapter().updateList(messageReceiptsList);
    }

    public void update(int index, MessageReceipt messageReceipt) {
        if (receiptListAdapter!=null)
            receiptListAdapter.updateReceiptsAtIndex(index,messageReceipt);
    }
}

