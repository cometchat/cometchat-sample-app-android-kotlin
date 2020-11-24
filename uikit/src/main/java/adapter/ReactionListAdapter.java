package adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.Reaction.model.Reaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose - ReactionListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th November 2020
 *
 * Modified on  - 20th November 2020
 *
 */

public class ReactionListAdapter extends RecyclerView.Adapter<ReactionListAdapter.ReactionViewHolder> {

    private  Context context;

    private List<Reaction> reactionArrayList = new ArrayList<>();

    private static final String TAG = "reactionListAdapter";

    /**
     * It is a contructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public ReactionListAdapter(Context context) {
        this.context=context;
    }

    /**
     * It is constructor which takes reactionArrayList as parameter and bind it with reactionArrayList in adapter.
     *
     * @param context is a object of Context.
     * @param reactionArrayList is a list of Reactions used in this adapter.
     */
    public ReactionListAdapter(Context context, List<Reaction> reactionArrayList) {
        this.reactionArrayList = reactionArrayList;
        this.context= context;
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.reaction_list_row, parent, false);

        return new ReactionViewHolder(view);
    }

    /**
     * This method is used to bind the ReactionViewHolder contents with user at given
     * position. It set username userAvatar in respective ReactionViewHolder content.
     *
     * @param reactionViewHolder is a object of ReactionViewHolder.
     * @param i is a position of item in recyclerView.
     * @see Reaction
     */
    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder reactionViewHolder, int i) {

        Reaction reaction = reactionArrayList.get(i);
        Log.e(TAG, "onBindViewHolder: "+reaction.getCode());
        reactionViewHolder.reaction.setText(reaction.getCode());
        reactionViewHolder.itemView.setTag(R.string.reaction,reaction);
    }

    @Override
    public int getItemCount() {
        return reactionArrayList.size();
    }


    /**
     * This method is used to update the reactions of reactionArrayList in adapter.
     *
     * @param reactions is a list of updated reactions.
     */
    public void updateList(List<Reaction> reactions) {
        for (int i = 0; i < reactions.size(); i++) {
            if (reactionArrayList.contains(reactions.get(i))){
                int index=reactionArrayList.indexOf(reactions.get(i));
                reactionArrayList.remove(index);
                reactionArrayList.add(index,reactions.get(i));
            }else {
                reactionArrayList.add(reactions.get(i));
            }
        }
        notifyDataSetChanged();
    }


    /**
     * This method is used to update particular user in userArrayList of adapter.
     *
     * @param reaction is a object of ReactionModel which will updated in reactionArrayList.
     * @see Reaction
     */
    public void updateReaction(Reaction reaction) {
        if (reactionArrayList.contains(reaction)){
            int index=reactionArrayList.indexOf(reaction);
            reactionArrayList.remove(index);
            reactionArrayList.add(index,reaction);
            notifyItemChanged(index);
        }else {
            reactionArrayList.add(reaction);
            notifyItemInserted(getItemCount()-1);
        }
    }

    /**
     * This method is used to remove particular reaction from reactionArrayList of adapter.
     *
     * @param reaction is a object of user which will be removed from reactionArrayList.
     * @see Reaction
     */
    public void remove(Reaction reaction) {
        if (reactionArrayList.contains(reaction)) {
            int index=reactionArrayList.indexOf(reaction);
            this.reactionArrayList.remove(reaction);
            notifyItemRemoved(index);
        }

    }

    /**
     * This method is used to add a reaction in reactionArrayList.
     * @param reaction is a object of ReactionModel which will be added in reactionArrayList.
     * @see Reaction
     */
    public void add(Reaction reaction) {
        updateReaction(reaction);
    }



    class ReactionViewHolder extends RecyclerView.ViewHolder {

        TextView reaction;
        ReactionViewHolder(View view) {
            super(view);
            reaction = view.findViewById(R.id.reaction);
        }

    }
}
