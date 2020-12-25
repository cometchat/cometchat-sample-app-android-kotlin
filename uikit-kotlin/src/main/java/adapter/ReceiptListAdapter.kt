package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.Avatar
import com.cometchat.pro.uikit.R
import utils.Utils
import java.util.*


class ReceiptListAdapter(context: Context?) : RecyclerView.Adapter<ReceiptListAdapter.ReceiptsHolder>() {

    private var context: Context? = null

    private var messageReceiptList: MutableList<MessageReceipt> = ArrayList()
    private var loggedInUserUid = CometChat.getLoggedInUser().uid

//    constructor(context: Context?){
//        this.context = context
//    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ReceiptsHolder {
        return ReceiptsHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cometchat_receipts_view, viewGroup, false))
    }

    override fun onBindViewHolder(receiptsHolder: ReceiptsHolder, position: Int) {
        val messageReceipt: MessageReceipt = messageReceiptList[position]
        receiptsHolder.tvName!!.setText(messageReceipt.sender.name)
        if (messageReceipt.readAt != 0L) receiptsHolder.tvRead!!.setText(Utils.getReceiptDate(messageReceipt.readAt))
        if (messageReceipt.deliveredAt != 0L) receiptsHolder.tvDelivery!!.setText(Utils.getReceiptDate(messageReceipt.deliveredAt))
        if (messageReceipt.sender.avatar != null) receiptsHolder.ivAvatar!!.setAvatar(messageReceipt.sender.avatar) else receiptsHolder.ivAvatar!!.setInitials(messageReceipt.sender.name)
    }

    override fun getItemCount(): Int {
        return messageReceiptList.size
    }

    fun add(messageReceipt: MessageReceipt?) {
        messageReceiptList.add(messageReceipt!!)
        notifyItemChanged(messageReceiptList.size - 1)
    }
    fun addAtIndex(index: Int, messageReceipt: MessageReceipt?) {
        messageReceiptList.add(index, messageReceipt!!)
        notifyItemChanged(index)
    }
    fun updateReceipts(messageReceipt: MessageReceipt?) {
        if (messageReceiptList.contains(messageReceipt)) {
            val index = messageReceiptList.indexOf(messageReceipt)
            messageReceiptList.removeAt(index)
            messageReceiptList.add(index, messageReceipt!!)
            notifyItemChanged(index)
        } else {
            messageReceiptList.add(messageReceipt!!)
            notifyItemChanged(messageReceiptList.size - 1)
        }
    }
    fun clear() {
        messageReceiptList.clear()
        notifyDataSetChanged()
    }
    fun updateList(messageReceiptsList: List<MessageReceipt>) {
        for (messageReceipt in messageReceiptsList) {
            if (messageReceipt.sender.uid != loggedInUserUid) updateReceipts(messageReceipt)
        }
    }
    fun updateReceiptsAtIndex(index: Int, messageReceipt: MessageReceipt?) {
        messageReceiptList.removeAt(index)
        messageReceiptList.add(messageReceipt!!)
        notifyItemChanged(index)
    }


    class ReceiptsHolder : RecyclerView.ViewHolder {
        var tvDelivery: TextView? = null
        var tvRead: TextView? = null
        var ivAvatar: Avatar? = null
        var tvName: TextView? = null

        constructor(itemView: View) : super(itemView){
            tvName = itemView.findViewById(R.id.tvName)
            tvDelivery = itemView.findViewById(R.id.tvDelivery)
            tvRead = itemView.findViewById(R.id.tvRead)
            ivAvatar = itemView.findViewById(R.id.ivAvatar)
        }
    }
}