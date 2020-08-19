package adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.EmojiSpan;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.Avatar;
import com.cometchat.pro.uikit.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import constant.StringContract;
import listeners.StickyHeaderAdapter;
import screen.threadconversation.CometChatThreadMessageActivity;
import utils.Extensions;
import utils.FontUtils;
import utils.MediaUtils;
import utils.Utils;
import utils.ZoomIv;

/**
 * Purpose - ThreadeAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of messages in thread. It helps to organize the messages based on its type i.e TextMessage,
 * MediaMessage, Actions. It also helps to manage whether message is sent or recieved and organizes
 * view based on it. It is single adapter used to manage group thread messages and user thread messages.
 *
 * Created on - 17th July 2020
 *
 * Modified on  - 17th July 2020
 *
 */


public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {// implements StickyHeaderAdapter<ThreadAdapter.DateItemHolder> {

    private static final int IMAGE_MESSAGE = 11;

    private static final int VIDEO_MESSAGE = 12;

    private static final int AUDIO_MESSAGE = 13;

    private List<BaseMessage> messageList = new ArrayList<>();

    private static final int TEXT_MESSAGE = 14;

    private static final int REPLY_TEXT_MESSAGE = 15;

    private static final int FILE_MESSAGE = 16;

    private static final int LINK_MESSAGE = 17;

    private static final int DELETE_MESSAGE = 18;

    private static final int CUSTOM_MESSAGE = 19;

    public Context context;

    private User loggedInUser = CometChat.getLoggedInUser();

    private boolean isLongClickEnabled;

    private List<Integer> selectedItemList = new ArrayList<>();

    public List<BaseMessage> longselectedItemList = new ArrayList<>();

    private FontUtils fontUtils;

    private MediaPlayer mediaPlayer;

    private int messagePosition=0;

    private OnMessageLongClick messageLongClick;

    private String TAG = "MessageAdapter";

    private boolean isSent;

    private boolean isTextMessageClick;

    private boolean isImageMessageClick;
    /**
     * It is used to initialize the adapter wherever we needed. It has parameter like messageList
     * which contains list of messages and it will be used in adapter and paramter type is a String
     * whose values will be either CometChatConstants.RECEIVER_TYPE_USER
     * CometChatConstants.RECEIVER_TYPE_GROUP.
     *
     *
     * @param context is a object of Context.
     * @param messageList is a list of messages used in this adapter.
     * @param type is a String which identifies whether it is a user messages or a group messages.
     *
     */
    public ThreadAdapter(Context context, List<BaseMessage> messageList, String type) {
        setMessageList(messageList);
        this.context = context;
        try {
            messageLongClick = (CometChatThreadMessageActivity)context;
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }

        fontUtils=FontUtils.getInstance(context);
    }

    /**
     * This method is used to return the different view types to adapter based on item position.
     * It uses getItemViewTypes() method to identify the view type of item.
     * @see ThreadAdapter#getItemViewTypes(int)
     *      *
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     * @see ThreadAdapter#onCreateViewHolder(ViewGroup, int)
     */
    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    private void setMessageList(List<BaseMessage> messageList) {
        this.messageList.addAll(0, messageList);
        notifyItemRangeInserted(0,messageList.size());

    }


    /**
     * This method is used to inflate the view for item based on its viewtype.
     * It helps to differentiate view for different type of messages.
     * Based on view type it returns various ViewHolder
     * Ex :- For MediaMessage it will return ImageMessageViewHolder,
     *       For TextMessage it will return TextMessageViewHolder,etc.
     *
     * @param parent is a object of ViewGroup.
     * @param i is viewType based on it various view will be inflated by adapter for various type
     *          of messages.
     * @return It return different ViewHolder for different viewType.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view;
        switch (i) {
            case DELETE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_item,parent,false);
                view.setTag(DELETE_MESSAGE);
                return new DeleteMessageViewHolder(view);
            case TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_item, parent, false);
                view.setTag(TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case REPLY_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_item, parent, false);
                view.setTag(REPLY_TEXT_MESSAGE);
                return new TextMessageViewHolder(view);

            case LINK_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_link_item,parent,false);
                view.setTag(LINK_MESSAGE);
                return new LinkMessageViewHolder(view);

            case AUDIO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_audio_layout,parent,false);
                view.setTag(AUDIO_MESSAGE);
                return new AudioMessageViewHolder(view);

            case IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_image_item, parent, false);
                view.setTag(IMAGE_MESSAGE);
                return new ImageMessageViewHolder(view);

            case VIDEO_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_video_item,parent,false);
                view.setTag(VIDEO_MESSAGE);
                return new VideoMessageViewHolder(view);

            case FILE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_file_item, parent, false);
                view.setTag(FILE_MESSAGE);
                return new FileMessageViewHolder(view);

            case CUSTOM_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_item, parent, false);
                view.setTag(TEXT_MESSAGE);
                return new CustomMessageViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_message_item, parent, false);
                view.setTag(-1);
                return new TextMessageViewHolder(view);
        }
    }


    /**
     * This method is used to bind the various ViewHolder content with their respective view types.
     * Here different methods are being called for different view type and in each method different
     * ViewHolder are been passed as parameter along with position of item.
     *
     * Ex :- For TextMessage <code>setTextData((TextMessageViewHolder)viewHolder,i)</code> is been called.
     * where <b>viewHolder</b> is casted as <b>TextMessageViewHolder</b> and <b>i</b> is position of item.
     *
     * @see ThreadAdapter#setTextData(TextMessageViewHolder, int)
     * @see ThreadAdapter#setImageData(ImageMessageViewHolder, int)
     * @see ThreadAdapter#setFileData(FileMessageViewHolder, int)
     *
     * @param viewHolder is a object of RecyclerViewHolder.
     * @param i is position of item in recyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        switch (viewHolder.getItemViewType()) {

            case DELETE_MESSAGE:
                setDeleteData((DeleteMessageViewHolder)viewHolder,i);
                break;
            case TEXT_MESSAGE:
            case REPLY_TEXT_MESSAGE:
                setTextData((TextMessageViewHolder) viewHolder, i);
                break;
            case LINK_MESSAGE:
                setLinkData((LinkMessageViewHolder) viewHolder,i);
                break;
            case IMAGE_MESSAGE:
                setImageData((ImageMessageViewHolder) viewHolder, i);
                break;
            case AUDIO_MESSAGE:
                setAudioData((AudioMessageViewHolder) viewHolder,i);
                break;
            case VIDEO_MESSAGE:
                setVideoData((VideoMessageViewHolder) viewHolder,i);
                break;
            case FILE_MESSAGE:
                setFileData((FileMessageViewHolder) viewHolder, i);
                break;
            case CUSTOM_MESSAGE:
                setCustomData((CustomMessageViewHolder) viewHolder, i);
                break;


        }
    }

    private void setAudioData(AudioMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null&&baseMessage.getDeletedAt()==0) {
            viewHolder.playBtn.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.textColorWhite)));
            setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
            viewHolder.tvUser.setText(baseMessage.getSender().getName());


            showMessageTime(viewHolder,baseMessage);
//            if (selectedItemList.contains(baseMessage.getId()))
//                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);

            viewHolder.length.setText(Utils.getFileSize(((MediaMessage)baseMessage).getAttachment().getFileSize()));
            viewHolder.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context);
                    mediaPlayer.reset();
                    if (messagePosition!=i) {
                        notifyItemChanged(messagePosition);
                        messagePosition = i;
                    }
                    try {
                        mediaPlayer.setDataSource(((MediaMessage)baseMessage).getAttachment().getFileUrl());
                        mediaPlayer.prepare();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                viewHolder.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "MediaPlayerError: "+e.getMessage());
                    }
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        viewHolder.playBtn.setImageResource(R.drawable.ic_pause_24dp);
                    } else {
                        mediaPlayer.pause();
                        viewHolder.playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    }
                }
            });
            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isLongClickEnabled && !isTextMessageClick) {
                        isImageMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
        }
    }

    public void stopPlayingAudio() {
        if (mediaPlayer!=null)
            mediaPlayer.stop();
    }
    /**
     * This method is called whenever viewType of item is file. It is used to bind FileMessageViewHolder
     * contents with MediaMessage at a given position.
     * It shows FileName, FileType, FileSize.
     *
     * @param viewHolder is a object of FileMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     * @see BaseMessage
     */
    private void setFileData(FileMessageViewHolder viewHolder, int i) {
        BaseMessage baseMessage = messageList.get(i);

          if (baseMessage!=null&&baseMessage.getDeletedAt()==0) {
              setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
              viewHolder.tvUser.setText(baseMessage.getSender().getName());

              viewHolder.fileName.setText(((MediaMessage) baseMessage).getAttachment().getFileName());
              viewHolder.fileExt.setText(((MediaMessage) baseMessage).getAttachment().getFileExtension());
              int fileSize = ((MediaMessage) baseMessage).getAttachment().getFileSize();

              viewHolder.fileSize.setText(Utils.getFileSize(fileSize));

              showMessageTime(viewHolder, baseMessage);

//              if (selectedItemList.contains(baseMessage.getId()))
//                  viewHolder.txtTime.setVisibility(View.VISIBLE);
//              else
//                  viewHolder.txtTime.setVisibility(View.GONE);


              viewHolder.rlMessageBubble.setOnClickListener(view -> {
//                  if (isLongClickEnabled && !isTextMessageClick) {
//                          setLongClickSelectedItem(baseMessage);
//                  }
//                  else {
                      setSelectedMessage(baseMessage.getId());
//                  }
                  notifyDataSetChanged();
              });
              viewHolder.fileName.setOnClickListener(view -> MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(),context));
              viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View v) {
                      if (!isLongClickEnabled && !isTextMessageClick) {
                          isImageMessageClick = true;
                          setLongClickSelectedItem(baseMessage);
                          messageLongClick.setLongMessageClick(longselectedItemList);
                          notifyDataSetChanged();
                      }
                      return true;
                  }
              });
          }
    }


    /**
     * This method is called whenever viewType of item is media. It is used to bind ImageMessageViewHolder
     * contents with MediaMessage at a given position.
     * It loads image of MediaMessage using its url.
     *
     * @param viewHolder is a object of ImageMessageViewHolder.
     * @param i is a position of item in recyclerView.
     * @see MediaMessage
     * @see BaseMessage
     */
    private void setImageData(ImageMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);

        setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
        viewHolder.tvUser.setText(baseMessage.getSender().getName());

        boolean isImageNotSafe = Extensions.getImageModeration(context,baseMessage);
        String smallUrl = Extensions.getThumbnailGeneration(context,baseMessage);
        viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_defaulf_image));
        if (smallUrl!=null) {
            Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).load(smallUrl).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (isImageNotSafe)
                        viewHolder.imageView.setImageBitmap(Utils.blur(context,resource));
                    else
                        viewHolder.imageView.setImageBitmap(resource);
                }
            });
        } else {
            Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).load(((MediaMessage)baseMessage).getAttachment().getFileUrl()).into(new SimpleTarget<Bitmap>() {
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
        showMessageTime(viewHolder, baseMessage);
//        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);



        viewHolder.rlMessageBubble.setOnClickListener(view -> {
            if (isImageNotSafe) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Unsafe Content");
                alert.setIcon(R.drawable.ic_hand);
                alert.setMessage("Are you surely want to see this unsafe content");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(), context);
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
                setSelectedMessage(baseMessage.getId());
                notifyDataSetChanged();
                MediaUtils.openFile(((MediaMessage) baseMessage).getAttachment().getFileUrl(), context);
            }

        });
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    private void displayImage(BaseMessage baseMessage) {
        Dialog imageDialog = new Dialog(context);
        View messageVw = LayoutInflater.from(context).inflate(R.layout.image_dialog_view, null);
        ZoomIv imageView = messageVw.findViewById(R.id.imageView);
        Glide.with(context).asBitmap().load(((MediaMessage) baseMessage).getAttachment().getFileUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImageBitmap(resource);
            }
        });
        imageDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageDialog.setContentView(messageVw);
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.show();
    }


    private void setVideoData(VideoMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);

        setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
        viewHolder.tvUser.setText(baseMessage.getSender().getName());

        if (((MediaMessage)baseMessage).getAttachment()!=null)
            Glide.with(context).load(((MediaMessage) baseMessage).getAttachment().getFileUrl()).into(viewHolder.imageView);

        showMessageTime(viewHolder, baseMessage);
//        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//


        viewHolder.rlMessageBubble.setOnClickListener(view -> {
            setSelectedMessage(baseMessage.getId());
            notifyDataSetChanged();

        });
        viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickEnabled && !isTextMessageClick) {
                    isImageMessageClick = true;
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                    notifyDataSetChanged();
                }
                return true;
            }
        });
        viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaUtils.openFile(((MediaMessage)baseMessage).getAttachment().getFileUrl(),context);
            }
        });
    }





    private void setDeleteData(DeleteMessageViewHolder viewHolder, int i) {


        BaseMessage baseMessage = messageList.get(i);

        setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
        viewHolder.tvUser.setText(baseMessage.getSender().getName());

        if (baseMessage.getDeletedAt()!=0) {
            viewHolder.txtMessage.setText(R.string.message_deleted);
            viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
            viewHolder.txtMessage.setTypeface(null, Typeface.ITALIC);
        }
        showMessageTime(viewHolder, baseMessage);

//        if (selectedItemList.contains(baseMessage.getId()))
//            viewHolder.txtTime.setVisibility(View.VISIBLE);
//        else
//            viewHolder.txtTime.setVisibility(View.GONE);
//
    }




    /**
     * This method is used to show message time below message whenever we click on message.
     * Since we have different ViewHolder, we have to pass <b>txtTime</b> of each viewHolder to
     * <code>setStatusIcon(RecyclerView.ViewHolder viewHolder,BaseMessage baseMessage)</code>
     * along with baseMessage.
     * @see ThreadAdapter#setStatusIcon(TextView, BaseMessage)
     *      *
     * @param viewHolder is object of ViewHolder.
     * @param baseMessage is a object of BaseMessage.
     *
     * @see BaseMessage
     */
    private void showMessageTime(RecyclerView.ViewHolder viewHolder, BaseMessage baseMessage) {

        if (viewHolder instanceof TextMessageViewHolder) {
            setStatusIcon(((TextMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof LinkMessageViewHolder) {
            setStatusIcon(((LinkMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof ImageMessageViewHolder) {
            setStatusIcon(((ImageMessageViewHolder) viewHolder).txtTime, baseMessage);
        } else if (viewHolder instanceof FileMessageViewHolder) {
            setStatusIcon(((FileMessageViewHolder) viewHolder).txtTime, baseMessage);
        }

    }

    /**
     * This method is used set message time i.e sentAt, deliveredAt & readAt in <b>txtTime</b>.
     * If sender of baseMessage is user then for user side messages it will show readAt, deliveredAt
     * time along with respective icon. For reciever side message it will show only deliveredAt time
     *
     * @param txtTime is a object of TextView which will show time.
     * @param baseMessage is a object of BaseMessage used to identify baseMessage sender.
     * @see BaseMessage
     */
    private void setStatusIcon(TextView txtTime, BaseMessage baseMessage) {
        if (baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
            if (baseMessage.getReadAt() != 0) {
                txtTime.setText(Utils.getHeaderDate(baseMessage.getReadAt() * 1000));
//                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_double_tick, 0);
                txtTime.setCompoundDrawablePadding(10);
            } else if (baseMessage.getDeliveredAt() != 0) {
                txtTime.setText(Utils.getHeaderDate(baseMessage.getDeliveredAt() * 1000));
//                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_all_black_24dp, 0);
                txtTime.setCompoundDrawablePadding(10);
            } else {
                txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt() * 1000));
//                txtTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_black_24dp, 0);
                txtTime.setCompoundDrawablePadding(10);
            }
        } else {
            txtTime.setText(Utils.getHeaderDate(baseMessage.getSentAt() * 1000));
        }
    }

    /**
     * This method is called whenever viewType of item is TextMessage. It is used to bind
     * TextMessageViewHolder content with TextMessage at given position.
     * It shows text of a message if deletedAt = 0 else it shows "message deleted"
     *
     * @param viewHolder is a object of TextMessageViewHolder.
     * @param i is postion of item in recyclerView.
     * @see TextMessage
     * @see BaseMessage
     */
    private void setTextData(TextMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            if (!baseMessage.getSender().getUid().equals(loggedInUser.getUid())) {
                boolean isSentimentNegative = Extensions.checkSentiment(baseMessage);
                if (isSentimentNegative) {
                    viewHolder.txtMessage.setVisibility(View.GONE);
                    viewHolder.sentimentVw.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.txtMessage.setVisibility(View.VISIBLE);
                    viewHolder.sentimentVw.setVisibility(View.GONE);
                }
                viewHolder.viewSentimentMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder sentimentAlert = new AlertDialog.Builder(context)
                                .setTitle(context.getResources().getString(R.string.sentiment_alert))
                                .setMessage(context.getResources().getString(R.string.sentiment_alert_message))
                                .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        viewHolder.txtMessage.setVisibility(View.VISIBLE);
                                        viewHolder.sentimentVw.setVisibility(View.GONE);
                                    }
                                })
                                .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        sentimentAlert.create().show();
                    }
                });
            } else {
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.sentimentVw.setVisibility(View.GONE);
            }
            setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
            viewHolder.tvUser.setText(baseMessage.getSender().getName());

            String txtMessage = ((TextMessage) baseMessage).getText().trim();
            viewHolder.txtMessage.setTextSize(16f);
            int count = 0;
            CharSequence processed = EmojiCompat.get().process(txtMessage, 0,
                    txtMessage.length() -1, Integer.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL);
            if (processed instanceof Spannable) {
                Spannable spannable = (Spannable) processed;
                count = spannable.getSpans(0, spannable.length() - 1, EmojiSpan.class).length;
                if (Utils.removeEmojiAndSymbol(txtMessage).trim().length() == 0) {
                    if (count == 1) {
                        viewHolder.txtMessage.setTextSize((int) Utils.dpToPx(context, 32));
                    } else if (count == 2) {
                        viewHolder.txtMessage.setTextSize((int) Utils.dpToPx(context, 24));
                    }
                }
            }

            viewHolder.txtMessage.setText(txtMessage);
            String profanityFilter = Extensions.checkProfanityMessage(baseMessage);
            viewHolder.txtMessage.setText(profanityFilter);
            viewHolder.txtMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));

            viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

            showMessageTime(viewHolder, baseMessage);
            if (messageList.get(messageList.size()-1).equals(baseMessage))
            {
                selectedItemList.add(baseMessage.getId());
            }

            setColorFilter(baseMessage,viewHolder.cardView);

            viewHolder.rlMessageBubble.setOnClickListener(view -> {
                if (isLongClickEnabled && !isImageMessageClick) {
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                }
                else {
                    setSelectedMessage(baseMessage.getId());
                }
                notifyDataSetChanged();

            });

            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true;
                        isTextMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
            viewHolder.itemView.setTag(R.string.message, baseMessage);
        }
    }


    private void setCustomData(CustomMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);
        if (baseMessage!=null) {
            setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
            viewHolder.tvUser.setText(baseMessage.getSender().getName());

            viewHolder.txtMessage.setText(context.getResources().getString(R.string.custom_message));
            viewHolder.txtMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoLight));
            viewHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

            showMessageTime(viewHolder, baseMessage);
            if (messageList.get(messageList.size()-1).equals(baseMessage))
            {
                selectedItemList.add(baseMessage.getId());
            }
            if (selectedItemList.contains(baseMessage.getId()))
                viewHolder.txtTime.setVisibility(View.VISIBLE);
            else
                viewHolder.txtTime.setVisibility(View.GONE);

            viewHolder.rlMessageBubble.setOnClickListener(view -> {
                setSelectedMessage(baseMessage.getId());
                notifyDataSetChanged();

            });
            viewHolder.itemView.setTag(R.string.message, baseMessage);
        }
    }

    private void setColorFilter(BaseMessage baseMessage,View view){

        if (!longselectedItemList.contains(baseMessage))
        {
            view.getBackground().setColorFilter(context.getResources().getColor(R.color.message_bubble_grey), PorterDuff.Mode.SRC_ATOP);
        } else {
            view.getBackground().setColorFilter(context.getResources().getColor(R.color.secondaryTextColor), PorterDuff.Mode.SRC_ATOP);
        }

    }


    private void setLinkData(LinkMessageViewHolder viewHolder, int i) {

        BaseMessage baseMessage = messageList.get(i);

        String url = null;

        if (baseMessage!=null) {

            setAvatar(viewHolder.ivUser, baseMessage.getSender().getAvatar(), baseMessage.getSender().getName());
            viewHolder.tvUser.setText(baseMessage.getSender().getName());

            if (baseMessage.getDeletedAt() == 0) {
                HashMap<String,JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
                if (extensionList!=null) {
                    if (extensionList.containsKey("linkPreview")) {
                        JSONObject linkPreviewJsonObject = extensionList.get("linkPreview");
                        try {
                            String description = linkPreviewJsonObject.getString("description");
                            String image = linkPreviewJsonObject.getString("image");
                            String title = linkPreviewJsonObject.getString("title");
                            url = linkPreviewJsonObject.getString("url");
                            Log.e("setLinkData: ", baseMessage.toString() + "\n\n" + url + "\n" + description + "\n" + image);
                            viewHolder.linkTitle.setText(title);
                            viewHolder.linkSubtitle.setText(description);
                            Glide.with(context).load(Uri.parse(image)).timeout(1000).into(viewHolder.linkImg);
                            if (url.contains("youtu.be") || url.contains("youtube")) {
                                viewHolder.videoLink.setVisibility(View.VISIBLE);
                                viewHolder.linkVisit.setText(context.getResources().getString(R.string.view_on_youtube));
                            } else {
                                viewHolder.videoLink.setVisibility(View.GONE);
                                viewHolder.linkVisit.setText(context.getResources().getString(R.string.visit));
                            }
                            String messageStr = ((TextMessage) baseMessage).getText();
                            if (((TextMessage) baseMessage).getText().equals(url) || ((TextMessage) baseMessage).getText().equals(url + "/")) {
                                viewHolder.message.setVisibility(View.GONE);
                            } else {
                                viewHolder.message.setVisibility(View.VISIBLE);
                            }
                            viewHolder.message.setText(messageStr);
                        } catch (Exception e) {
                            Log.e("setLinkData: ", e.getMessage());
                        }
                    }
                }
            }

            showMessageTime(viewHolder, baseMessage);
            String finalUrl = url;
            viewHolder.linkVisit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (finalUrl != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(finalUrl));
                        context.startActivity(intent);
                    }
                }
            });
//            if (selectedItemList.contains(baseMessage.getId()))
//                viewHolder.txtTime.setVisibility(View.VISIBLE);
//            else
//                viewHolder.txtTime.setVisibility(View.GONE);
//            if (i < selectedItems.length && selectedItems[i] == 0) {
//                viewHolder.txtTime.setVisibility(View.GONE);
//            } else
//                viewHolder.txtTime.setVisibility(View.VISIBLE);

            viewHolder.rlMessageBubble.setOnClickListener(view -> {
                if (isLongClickEnabled && !isImageMessageClick) {
                    setLongClickSelectedItem(baseMessage);
                    messageLongClick.setLongMessageClick(longselectedItemList);
                }
                else {
                    setSelectedMessage(baseMessage.getId());
                }
                notifyDataSetChanged();

            });
            viewHolder.rlMessageBubble.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!isImageMessageClick) {
                        isLongClickEnabled = true;
                        isTextMessageClick = true;
                        setLongClickSelectedItem(baseMessage);
                        messageLongClick.setLongMessageClick(longselectedItemList);
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
            viewHolder.itemView.setTag(R.string.message, baseMessage);
        }
    }

    public void setSelectedMessage(Integer id)
    {
        if (selectedItemList.contains(id))
            selectedItemList.remove(id);
        else
            selectedItemList.add(id);
    }

    public void setLongClickSelectedItem(BaseMessage baseMessage) {


        if (longselectedItemList.contains(baseMessage))
            longselectedItemList.remove(baseMessage);
        else
            longselectedItemList.add(baseMessage);
    }
    /**
     * This method is used to set avatar of groupMembers to show in groupMessages. If avatar of
     * group member is not available then it calls <code>setInitials(String name)</code> to show
     * first two letter of group member name.
     *
     * @param avatar is a object of Avatar
     * @param avatarUrl is a String. It is url of avatar.
     * @param name is a String. It is a name of groupMember.
     * @see Avatar
     *
     */
    private void setAvatar(Avatar avatar, String avatarUrl, String name) {

        if (avatarUrl != null && !avatarUrl.isEmpty())
            Glide.with(context).load(avatarUrl).into(avatar);
        else
            avatar.setInitials(name);

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

//    @Override
//    public long getHeaderId(int var1) {
//
//        BaseMessage baseMessage = messageList.get(var1);
//        return Long.parseLong(Utils.getDateId(baseMessage.getSentAt() * 1000));
//    }
//
//    @Override
//    public DateItemHolder onCreateHeaderViewHolder(ViewGroup var1) {
//        View view = LayoutInflater.from(var1.getContext()).inflate(R.layout.cc_message_list_header,
//                var1, false);
//
//        return new DateItemHolder(view);
//    }
//
//    @Override
//    public void onBindHeaderViewHolder(DateItemHolder var1, int var2, long var3) {
//        BaseMessage baseMessage = messageList.get(var2);
//        Date date = new Date(baseMessage.getSentAt() * 1000L);
//        String formattedDate = Utils.getDate(date.getTime());
//        var1.txtMessageDate.setBackground(context.getResources().getDrawable(R.drawable.cc_rounded_date_button));
//        var1.txtMessageDate.setText(formattedDate);
//    }

    /**
     * This method is used to maintain different viewType based on message category and type and
     * returns the different view types to adapter based on it.
     *
     * Ex :- For message with category <b>CometChatConstants.CATEGORY_MESSAGE</b> and type
     * <b>CometChatConstants.MESSAGE_TYPE_TEXT</b> and message is sent by a <b>Logged-in user</b>,
     * It will return <b>RIGHT_TEXT_MESSAGE</b>
     *
     *
     * @param position is a position of item in recyclerView.
     * @return It returns int which is value of view type of item.
     *
     * @see ThreadAdapter#onCreateViewHolder(ViewGroup, int)
     * @see BaseMessage
     *
     */
    private int getItemViewTypes(int position) {
        BaseMessage baseMessage = messageList.get(position);
        HashMap<String,JSONObject> extensionList = Extensions.extensionCheck(baseMessage);
        if (baseMessage.getDeletedAt()==0) {
            if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_MESSAGE)) {
                switch (baseMessage.getType()) {

                    case CometChatConstants.MESSAGE_TYPE_TEXT:
                        if (extensionList != null && extensionList.containsKey("linkPreview") && extensionList.get("linkPreview") != null)
                            return LINK_MESSAGE;
                        else if (baseMessage.getMetadata()!=null && baseMessage.getMetadata().has("reply"))
                            return REPLY_TEXT_MESSAGE;
                        else
                            return TEXT_MESSAGE;
                    case CometChatConstants.MESSAGE_TYPE_AUDIO:
                        return AUDIO_MESSAGE;
                    case CometChatConstants.MESSAGE_TYPE_IMAGE:
                        return IMAGE_MESSAGE;
                    case CometChatConstants.MESSAGE_TYPE_VIDEO:
                        return VIDEO_MESSAGE;
                    case CometChatConstants.MESSAGE_TYPE_FILE:
                        return FILE_MESSAGE;
                    default:
                        return -1;
                }
            } else {
                if (baseMessage.getCategory().equals(CometChatConstants.CATEGORY_CUSTOM)){
                    return CUSTOM_MESSAGE;
                }
            }
        }
        else
        {
            return DELETE_MESSAGE;
        }
        return -1;

    }

    /**
     * This method is used to update message list of adapter.
     *
     * @param baseMessageList is list of baseMessages.
     *
     */
    public void updateList(List<BaseMessage> baseMessageList) {
        setMessageList(baseMessageList);
    }

    /**
     * This method is used to set real time delivery receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
    public void setDeliveryReceipts(MessageReceipt messageReceipt) {

        for (int i = messageList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageList.get(i);
            if (baseMessage.getDeliveredAt() == 0) {
                int index = messageList.indexOf(baseMessage);
                messageList.get(index).setDeliveredAt(messageReceipt.getDeliveredAt());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * This method is used to set real time read receipt of particular message in messageList
     * of adapter by updating message.
     *
     * @param messageReceipt is a object of MessageReceipt.
     * @see MessageReceipt
     */
    public void setReadReceipts(MessageReceipt messageReceipt) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            BaseMessage baseMessage = messageList.get(i);
            if (baseMessage.getReadAt() == 0) {
                int index = messageList.indexOf(baseMessage);
                messageList.get(index).setReadAt(messageReceipt.getReadAt());
            }
        }

        notifyDataSetChanged();
    }

    /**
     * This method is used to add message in messageList when send by a user or when received in
     * real time.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will added.
     * @see BaseMessage
     *
     */
    public void addMessage(BaseMessage baseMessage) {
//        if (!messageList.contains(baseMessage)) {
            messageList.add(baseMessage);
            selectedItemList.clear();
//        }
        notifyItemInserted(messageList.size()-1);
    }

    /**
     * This method is used to update previous message with new message in messageList of adapter.
     *
     * @param baseMessage is a object of BaseMessage. It is new message which will be updated.
     */
    public void setUpdatedMessage(BaseMessage baseMessage) {

        if (messageList.contains(baseMessage)) {
            int index = messageList.indexOf(baseMessage);
            messageList.remove(baseMessage);
            messageList.add(index, baseMessage);
            notifyItemChanged(index);
        }
    }

    public void resetList() {
        messageList.clear();
        notifyDataSetChanged();
    }

    public void clearLongClickSelectedItem() {
        isLongClickEnabled = false;
        isTextMessageClick = false;
        isImageMessageClick = false;
        longselectedItemList.clear();
        notifyDataSetChanged();
    }

    public BaseMessage getLastMessage() {
        if (messageList.size()>0)
        {
            Log.e(TAG, "getLastMessage: "+messageList.get(messageList.size()-1 ));
            return messageList.get(messageList.size()-1);
        }
        else
            return null;
    }

    public int getPosition(BaseMessage baseMessage){
        return messageList.indexOf(baseMessage);
    }

    class ImageMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private CardView cardView;
        private ProgressBar progressBar;
        private Avatar ivUser;
        public TextView txtTime,tvUser;
        private RelativeLayout rlMessageBubble;

        private RelativeLayout sensitiveLayout;
        public ImageMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            imageView = view.findViewById(R.id.go_img_message);
            tvUser= view.findViewById(R.id.tv_user);
            cardView = view.findViewById(R.id.cv_image_message_container);
            progressBar = view.findViewById(R.id.img_progress_bar);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            sensitiveLayout = view.findViewById(R.id.sensitive_layout);
            rlMessageBubble = view.findViewById(R.id.rl_message);
        }
    }

    public class ActionMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public ActionMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            textView = view.findViewById(R.id.go_txt_message);
        }
    }

    class VideoMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageView playBtn;
        private CardView cardView;
        private ProgressBar progressBar;
        private Avatar ivUser;
        public TextView txtTime,tvUser;
        private RelativeLayout rlMessageBubble;

        public VideoMessageViewHolder(@NonNull View view) {
            super(view);
            int type = (int) view.getTag();
            imageView = view.findViewById(R.id.go_video_message);
            playBtn = view.findViewById(R.id.playBtn);
            tvUser= view.findViewById(R.id.tv_user);
            cardView = view.findViewById(R.id.cv_image_message_container);
            progressBar = view.findViewById(R.id.img_progress_bar);
            txtTime = view.findViewById(R.id.txt_time);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
        }
    }

    public class FileMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView fileExt;
        public TextView txtTime;
        private TextView fileSize;
        private TextView tvUser;
        private View view;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;

        FileMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            fileSize = itemView.findViewById(R.id.tvFileSize);
            ivUser = itemView.findViewById(R.id.iv_user);
            tvUser = itemView.findViewById(R.id.tv_user);
            fileExt = itemView.findViewById(R.id.tvFileExtension);
            txtTime = itemView.findViewById(R.id.txt_time);
            fileName = itemView.findViewById(R.id.tvFileName);
            rlMessageBubble = itemView.findViewById(R.id.rl_message);
            this.view = itemView;
        }
    }

    public class DeleteMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private ImageView imgStatus;
        private int type;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;

        DeleteMessageViewHolder(@NonNull View view) {
            super(view);
            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            this.view = view;
        }
    }

    public class TextMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private ImageView imgStatus;
        private int type;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;
        private RelativeLayout replyLayout;
        private TextView replyUser;
        private TextView replyMessage;
        private RelativeLayout sentimentVw;
        private TextView viewSentimentMessage;

        TextMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            replyLayout = view.findViewById(R.id.replyLayout);
            replyUser = view.findViewById(R.id.reply_user);
            replyMessage = view.findViewById(R.id.reply_message);
            sentimentVw = view.findViewById(R.id.sentiment_layout);
            viewSentimentMessage = view.findViewById(R.id.view_sentiment);
            this.view = view;

        }
    }

    public class CustomMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        public TextView tvUser;
        private ImageView imgStatus;
        private int type;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;


        CustomMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            txtMessage = view.findViewById(R.id.go_txt_message);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            this.view = view;
        }
    }

    public class AudioMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView length;
        private ImageView playBtn;
        private int type;
        private TextView tvUser;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;
        private TextView txtTime;
        public AudioMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            type = (int)itemView.getTag();
            length = itemView.findViewById(R.id.audiolength_tv);
            playBtn = itemView.findViewById(R.id.playBtn);
            rlMessageBubble = itemView.findViewById(R.id.cv_message_container);
            tvUser = itemView.findViewById(R.id.tv_user);
            ivUser = itemView.findViewById(R.id.iv_user);
            txtTime = itemView.findViewById(R.id.txt_time);
        }
    }
    public class LinkMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView linkTitle;
        private TextView linkVisit;
        private TextView linkSubtitle;
        private TextView message;
        private ImageView videoLink;
        private RelativeLayout cardView;
        private View view;
        public TextView txtTime;
        private ImageView imgStatus;
        private ImageView linkImg;
        private int type;
        private TextView tvUser;
        private Avatar ivUser;
        private RelativeLayout rlMessageBubble;

        LinkMessageViewHolder(@NonNull View view) {
            super(view);

            type = (int) view.getTag();
            tvUser = view.findViewById(R.id.tv_user);
            linkTitle = view.findViewById(R.id.link_title);
            linkSubtitle = view.findViewById(R.id.link_subtitle);
            linkVisit = view.findViewById(R.id.visitLink);
            linkImg = view.findViewById(R.id.link_img);
            message = view.findViewById(R.id.message);
            videoLink = view.findViewById(R.id.videoLink);
            cardView = view.findViewById(R.id.cv_message_container);
            txtTime = view.findViewById(R.id.txt_time);
            imgStatus = view.findViewById(R.id.img_pending);
            ivUser = view.findViewById(R.id.iv_user);
            rlMessageBubble = view.findViewById(R.id.rl_message);
            this.view = view;
        }
    }

    public class DateItemHolder extends RecyclerView.ViewHolder {

        TextView txtMessageDate;

        DateItemHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageDate = itemView.findViewById(R.id.txt_message_date);
        }
    }

    public interface OnMessageLongClick
    {
        void setLongMessageClick(List<BaseMessage> baseMessage);
    }
}



