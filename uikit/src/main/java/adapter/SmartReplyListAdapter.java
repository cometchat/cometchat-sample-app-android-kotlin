package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import utils.FontUtils;

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */

public class SmartReplyListAdapter extends RecyclerView.Adapter<SmartReplyListAdapter.SmartReplyViewHolder> {

    private  Context context;

    private List<String> replyArrayList = new ArrayList<>();

    private static final String TAG = "SmartReplyListAdapter";

    private FontUtils fontUtils;

    /**
     * It is a constructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public SmartReplyListAdapter(Context context) {
        this.context=context;
        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context is a object of Context.
     * @param replyArrayList is a list of users used in this adapter.
     */
    public SmartReplyListAdapter(Context context, List<String> replyArrayList) {
        this.replyArrayList = replyArrayList;
        this.context= context;
        fontUtils=FontUtils.getInstance(context);
    }

    @NonNull
    @Override
    public SmartReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_list_row, parent, false);

        return new SmartReplyViewHolder(view);
    }

    /**
     * This method is used to bind the UserViewHolder contents with user at given
     * position. It set username userAvatar in respective UserViewHolder content.
     *
     * @param smartReplyViewHolder is a object of UserViewHolder.
     * @param i is a position of item in recyclerView.
     * @see User
     *
     */
    @Override
    public void onBindViewHolder(@NonNull SmartReplyViewHolder smartReplyViewHolder, int i) {

        final String reply = replyArrayList.get(i);
        smartReplyViewHolder.cReply.setText(reply);
        smartReplyViewHolder.itemView.setTag(R.string.replyTxt,reply);


    }

    @Override
    public int getItemCount() {
        return replyArrayList.size();
    }

    public void updateList(List<String> replies) {
        this.replyArrayList = replies;
        notifyDataSetChanged();
    }

    class SmartReplyViewHolder extends RecyclerView.ViewHolder {

       private Chip cReply;

       SmartReplyViewHolder(View view) {
            super(view);
           cReply = view.findViewById(R.id.replyText);
        }

    }
}
