package com.inscripts.cometchatpulse.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.models.MessageReceipt
import com.inscripts.cometchatpulse.Adapter.ReceiptListAdapter
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel

class GroupMessageInfoActivity : AppCompatActivity() {

    lateinit var recieptList : RecyclerView

    var recieptArray : MutableMap<String,MessageReceipt> = HashMap()

    var id : Int = 0

    var message : String? = null

    var imageUrl : String? = null

    var receiptListAdapter : ReceiptListAdapter?=null

    var textmessage : TextView? = null

    var mediaMessage : ImageView? = null

    var messageRepository: MessageRepository = MessageRepository()

    lateinit var groupChatViewModel : GroupChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_message_info)
        mediaMessage = findViewById(R.id.mediaMessage)
        textmessage = findViewById(R.id.textMessage)
        recieptList = findViewById(R.id.recieptList)
        groupChatViewModel = ViewModelProviders.of(this).get(GroupChatViewModel::class.java)

        runOnUiThread(object : Runnable{
            override fun run() {
                receiptListAdapter = ReceiptListAdapter(this@GroupMessageInfoActivity,recieptArray)
                recieptList.adapter = receiptListAdapter
            }
        })
        handleIntent()
        getReceipt();
        groupChatViewModel.liveDeliveryReceipts.observe(this, Observer {
            delivery -> delivery.let {
            Log.e("RECEIPTDELIVERY",it.toString())
            receiptListAdapter?.updateReciept(it) }
        })
        groupChatViewModel.liveReadReceipts.observe(this, Observer {
            read-> read.let {
            Log.e("RECEIPTREAD",it.toString())
            receiptListAdapter?.updateReciept(it)} })
    }

    fun handleIntent()
    {
        if (intent.hasExtra(StringContract.IntentString.ID) && intent.hasExtra(StringContract.IntentString.IMAGE_TYPE))
        {
            id = intent.getIntExtra(StringContract.IntentString.ID,0)
            if (intent.getBooleanExtra(StringContract.IntentString.IMAGE_TYPE,false))
            {
                imageUrl = intent.getStringExtra(StringContract.IntentString.MESSAGE)
                Glide.with(this).load(imageUrl).into(mediaMessage!!)
                mediaMessage?.visibility = View.VISIBLE
                textmessage?.visibility = View.GONE
            }
            else
            {
                mediaMessage?.visibility = View.GONE
                message = intent.getStringExtra(StringContract.IntentString.MESSAGE)
                textmessage?.text = message
                textmessage?.visibility = View.VISIBLE
            }
        }
    }

    fun getReceipt()
    {
        Log.e("RECEIPT",""+id);
        var m : MessagesRequest = MessagesRequest.MessagesRequestBuilder().setGUID("supergroup2").setMessageId(id-1).build()
        m.fetchNext(object: CometChat.CallbackListener<List<BaseMessage>>()
        {
            override fun onSuccess(p0: List<BaseMessage>?) {
                Log.e("MR",p0.toString()!!)
            }

            override fun onError(p0: CometChatException?) {
                Log.e("MR",p0?.message)
            }
        })
        CometChat.getMessageReceipts(id,object : CometChat.CallbackListener<List<MessageReceipt>>(){
            override fun onSuccess(p0: List<MessageReceipt>?) {
                for (reciept : MessageReceipt in p0!!)
                {
                    Log.e("GINFO",reciept.toString())
                    recieptArray.put(reciept.sender.uid,reciept)
                }
                receiptListAdapter?.notifyDataSetChanged()
            }

            override fun onError(p0: CometChatException?) {
                Log.e("TAG",p0!!.message)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        messageListener()
    }
    override fun onResume() {
        super.onResume()
        messageListener();
    }

    override fun onPause() {
        super.onPause()
        groupChatViewModel.removeMessageListener("GroupMessageInfo")
    }
    fun messageListener()
    {
        CometChat.addMessageListener("GroupInfo",object : CometChat.MessageListener(){
            override fun onMessagesDelivered(messageReceipt: MessageReceipt?) {
                if (messageReceipt?.messageId==id)
                {
                    receiptListAdapter?.updateReciept(messageReceipt!!)
                    Log.e("DELIVERED",messageReceipt.toString())
                }
                super.onMessagesDelivered(messageReceipt)
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt?) {
                super.onMessagesRead(messageReceipt)
                if (messageReceipt?.messageId==id) {
                    Log.e("READ",messageReceipt.toString())
                    receiptListAdapter?.updateReciept(messageReceipt!!)
                }

            }
        })
    }
}
