package adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.UserListRowBinding;

import java.util.ArrayList;
import java.util.List;

import listeners.StickyHeaderAdapter;
import utils.Extensions;
import utils.FontUtils;
import utils.MediaUtils;
import utils.Utils;

/**
 * Purpose - UserListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of users. It helps to organize the users in recyclerView.
 *
 * Created on - 20th December 2019
 *
 * Modified on  - 23rd March 2020
 *
 */

public class SharedMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private List<BaseMessage> messageArrayList = new ArrayList<>();

    private static final String TAG = "SharedMediaAdapter";

    private static final int SHARED_MEDIA_IMAGE = 1;

    private static final int SHARED_MEDIA_VIDEO = 2;

    private static final int SHARED_MEDIA_FILE = 3;

    private FontUtils fontUtils;

    /**
     * It is a contructor which is used to initialize wherever we needed.
     *
     * @param context is a object of Context.
     */
    public SharedMediaAdapter(Context context) {
        this.context = context;
        fontUtils = FontUtils.getInstance(context);
    }

    /**
     * It is constructor which takes userArrayList as parameter and bind it with userArrayList in adapter.
     *
     * @param context          is a object of Context.
     * @param messageArrayList is a list of messages used in this adapter.
     */
    public SharedMediaAdapter(Context context, List<BaseMessage> messageArrayList) {
        setMessageList(messageArrayList);
        this.context = context;
        fontUtils = FontUtils.getInstance(context);
    }

    private void setMessageList(List<BaseMessage> messageArrayList) {
        this.messageArrayList.addAll(messageArrayList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    private int getItemViewTypes(int position) {
        BaseMessage baseMessage = messageArrayList.get(position);
        if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            return SHARED_MEDIA_IMAGE;
        } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
            return SHARED_MEDIA_VIDEO;
        } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
            return SHARED_MEDIA_FILE;
        }

        return -1;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (i) {
            case SHARED_MEDIA_IMAGE:
                view = layoutInflater.inflate(R.layout.shared_media_image_row, parent, false);
                return new ImageViewHolder(view);

            case SHARED_MEDIA_VIDEO:
                view = layoutInflater.inflate(R.layout.shared_media_video_row, parent, false);
                return new VideoViewHolder(view);

            case SHARED_MEDIA_FILE:
                view = layoutInflater.inflate(R.layout.shared_media_file_row, parent, false);
                return new FileViewHolder(view);

            default:
                view = layoutInflater.inflate(R.layout.shared_media_image_row, parent, false);
                return new FileViewHolder(view);

        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageArrayList.get(i);
        if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            setImageData((ImageViewHolder) viewHolder, i);
        } else if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
            setFileData((FileViewHolder) viewHolder, i);
        } else {
            setVideoData((VideoViewHolder) viewHolder, i);
        }
    }

    private void setVideoData(VideoViewHolder viewHolder, int i) {
        BaseMessage message = messageArrayList.get(i);
        Glide.with(context).load(((MediaMessage) message).getAttachment().getFileUrl()).into(viewHolder.imageView);
        viewHolder.imageView.setOnClickListener(view ->{
            MediaUtils.openFile(((MediaMessage) message).getAttachment().getFileUrl(), context);
        });
        viewHolder.itemView.setTag(R.string.baseMessage, message);
    }


    private void setFileData(FileViewHolder viewHolder, int i) {
        BaseMessage message = messageArrayList.get(i);
        viewHolder.fileName.setText(((MediaMessage) message).getAttachment().getFileName());
        viewHolder.fileExtension.setText(((MediaMessage) message).getAttachment().getFileExtension());
        viewHolder.itemView.setOnClickListener(view -> {
                MediaUtils.openFile(((MediaMessage) message).getAttachment().getFileUrl(), context);
        });
        viewHolder.itemView.setTag(R.string.baseMessage, message);
    }

    private void setImageData(ImageViewHolder viewHolder, int i) {
        BaseMessage message = messageArrayList.get(i);
        boolean isImageNotSafe = Extensions.getImageModeration(context,message);
        String smallUrl = Extensions.getThumbnailGeneration(context,message);

        if (smallUrl!=null) {
            Glide.with(context).asBitmap().load(smallUrl).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (isImageNotSafe)
                        viewHolder.imageView.setImageBitmap(Utils.blur(context,resource));
                    else
                        viewHolder.imageView.setImageBitmap(resource);
                }
            });
        } else {
            Glide.with(context).asBitmap().load(((MediaMessage)message).getAttachment().getFileUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (isImageNotSafe)
                        viewHolder.imageView.setImageBitmap(Utils.blur(context,resource));
                    else
                        viewHolder.imageView.setImageBitmap(resource);
                }
            });
        }
        if (isImageNotSafe) {
            viewHolder.sensitiveLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sensitiveLayout.setVisibility(View.GONE);
        }
        viewHolder.imageView.setOnClickListener(view -> {
            if (isImageNotSafe) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Unsafe Content");
                alert.setIcon(R.drawable.ic_hand);
                alert.setMessage("Are you surely want to see this unsafe content");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaUtils.openFile(((MediaMessage) message).getAttachment().getFileUrl(), context);
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.create().show();
            } else {
                MediaUtils.openFile(((MediaMessage) message).getAttachment().getFileUrl(), context);
            }

        });
        viewHolder.itemView.setTag(R.string.baseMessage, message);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public void updateMessageList(List<BaseMessage> baseMessageList) {
        setMessageList(baseMessageList);
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private RelativeLayout sensitiveLayout;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            sensitiveLayout = itemView.findViewById(R.id.sensitive_layout);
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.video_img);
        }
    }

    class FileViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView fileExtension;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName_tv);
            fileExtension = itemView.findViewById(R.id.extension_tv);
        }
    }
}
