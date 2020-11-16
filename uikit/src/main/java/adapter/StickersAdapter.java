package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cometchat.pro.uikit.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.cometchat.pro.uikit.Sticker.model.Sticker;

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */

public class StickersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private List<Sticker> stickerArrayList = new ArrayList<>();

    private static final String TAG = "StickerAdapter";

    private static final int STICKER_IMAGE = 1;

    /**
     * It is a contructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public StickersAdapter(Context context) {
        this.context = context;
    }

    /**
     * It is constructor which takes stickerArrayList as parameter and bind it with stickerArrayList in adapter.
     *
     * @param context          is a object of Context.
     * @param stickerArrayList is a list of stickers used in this adapter.
     */
    public StickersAdapter(Context context, List<Sticker> stickerArrayList) {
        setStickerList(stickerArrayList);
        this.context = context;
    }

    private void setStickerList(List<Sticker> stickerArrayList) {
        Collections.sort(stickerArrayList, new Comparator<Sticker>() {
            @Override
            public int compare(Sticker sticker, Sticker t1) {
                return sticker.getSetName().compareTo(t1.getSetName());
            }
        });
        this.stickerArrayList = stickerArrayList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.stickers_row, parent, false);
        return new ImageViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        setStickerData((ImageViewHolder) viewHolder, i);

    }



    private void setStickerData(ImageViewHolder viewHolder, int i) {
        Sticker sticker = stickerArrayList.get(i);
        Glide.with(context).asBitmap().load(sticker.getUrl())
                    .into(viewHolder.imageView);
        viewHolder.itemView.setTag(R.string.sticker, sticker);
    }

    @Override
    public int getItemCount() {
        return stickerArrayList.size();
    }

    public void updateStickerList(List<Sticker> stickerList) {
        setStickerList(stickerList);
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
