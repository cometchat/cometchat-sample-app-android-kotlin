package screen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.MessageReceipt;
import com.cometchat.pro.uikit.CometChatReceiptsList;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import constant.StringContract;
import utils.Utils;

public class CometChatMessageInfoScreenActivity extends AppCompatActivity {

    private View textMessage;
    private View imageMessage;
    private View audioMessage;
    private View fileMessage;
    private View videoMessage;
    private View locationMessage;
    private View pollsMessage;

    private TextView question;
    private LinearLayout optionGroup;

    private ImageView ivMap;
    private TextView tvPlaceName;

    private TextView messageText;
    private ImageView messageImage;
    private ImageView messageVideo;
    private TextView txtTime;
    private RelativeLayout sensitiveLayout;

    private TextView audioFileSize;

    private TextView fileName;
    private TextView fileExtension;
    private TextView fileSize;

    private int id;
    private String message;
    private String messageType;
    private int messageSize;
    private String messageExtension;
    private int percentage=0;
    private String TAG = "CometChatMessageInfo";

    private SwipeRefreshLayout swipeRefreshLayout;
    private CometChatReceiptsList cometChatReceiptsList;

    private Toolbar toolbar;
    private RelativeLayout messageLayout;
    private ImageView backIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_chat_message_info_screen);
        toolbar = findViewById(R.id.detail_toolbar);
        messageLayout = findViewById(R.id.message_layout);
        backIcon = findViewById(R.id.backIcon);
        textMessage = findViewById(R.id.vwTextMessage);
        imageMessage = findViewById(R.id.vwImageMessage);
        audioMessage = findViewById(R.id.vwAudioMessage);
        fileMessage = findViewById(R.id.vwFileMessage);
        locationMessage = findViewById(R.id.vwLocationMessage);
        pollsMessage = findViewById(R.id.polls_message);
        findViewById(R.id.total_votes).setVisibility(View.GONE);
        question = findViewById(R.id.tv_question);
        optionGroup = findViewById(R.id.options_group);
        messageText = findViewById(R.id.go_txt_message);
        txtTime = findViewById(R.id.txt_time);
        txtTime.setVisibility(View.VISIBLE);
        messageImage = findViewById(R.id.go_img_message);
        messageVideo = findViewById(R.id.go_video_message);
        sensitiveLayout = findViewById(R.id.sensitive_layout);
        audioFileSize = findViewById(R.id.audiolength_tv);
        fileName = findViewById(R.id.tvFileName);
        fileSize = findViewById(R.id.tvFileSize);
        fileExtension = findViewById(R.id.tvFileExtension);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        cometChatReceiptsList = findViewById(R.id.rvReceipts);
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.red),
                getResources().getColor(R.color.grey));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchReceipts();
            }
        });
        ivMap = findViewById(R.id.iv_map);
        tvPlaceName = findViewById(R.id.tv_place_name);
        handleIntent();
        fetchReceipts();
        backIcon.setOnClickListener(view -> onBackPressed());
        if(Utils.isDarkMode(this)) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
            messageLayout.setBackgroundColor(getResources().getColor(R.color.darkModeBackground));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.textColorWhite));
            messageLayout.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }
    }

    private void fetchReceipts() {
        CometChat.getMessageReceipts(id, new CometChat.CallbackListener<List<MessageReceipt>>() {
                @Override
                public void onSuccess(List<MessageReceipt> messageReceipts) {
                    cometChatReceiptsList.clear();
                    cometChatReceiptsList.setMessageReceiptList(messageReceipts);
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onError(CometChatException e) {
                    Snackbar.make(cometChatReceiptsList,e.getMessage(),Snackbar.LENGTH_LONG).show();
                }
        });
    }

    private void handleIntent() {
        if (getIntent().hasExtra(StringContract.IntentStrings.ID)){
            id = getIntent().getIntExtra(StringContract.IntentStrings.ID,0);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.TEXTMESSAGE)) {
            message = getIntent().getStringExtra(StringContract.IntentStrings.TEXTMESSAGE);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.IMAGE_MODERATION)) {
            boolean isImageNotSafe = getIntent()
                    .getBooleanExtra(StringContract.IntentStrings.IMAGE_MODERATION,true);
            if (isImageNotSafe)
                sensitiveLayout.setVisibility(View.VISIBLE);
            else
                sensitiveLayout.setVisibility(View.GONE);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL)) {
            message = getIntent().getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_URL);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MESSAGE_TYPE)) {
            messageType = getIntent().getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)) {
            messageExtension = getIntent().getStringExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE)) {
            messageSize = getIntent().
                    getIntExtra(StringContract.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,0);
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.SENTAT)) {
            txtTime.setText(Utils.getHeaderDate(getIntent()
                    .getLongExtra(StringContract.IntentStrings.SENTAT,0)*1000));
        }
        if (getIntent().hasExtra(StringContract.IntentStrings.CUSTOM_MESSAGE)) {
            message = getIntent().getStringExtra(StringContract.IntentStrings.CUSTOM_MESSAGE);
        }

        if (getIntent().hasExtra(StringContract.IntentStrings.POLL_RESULT)) {
            percentage = getIntent().getIntExtra(StringContract.IntentStrings.POLL_RESULT,0);
        }
        if (messageType!=null) {
            if (messageType.equals(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                textMessage.setVisibility(View.VISIBLE);
                messageText.setText(message);
            } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                imageMessage.setVisibility(View.VISIBLE);
                Glide.with(this).load(message).into(messageImage);
            } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_VIDEO)) {
                videoMessage.setVisibility(View.VISIBLE);
                Glide.with(this).load(message).into(messageVideo);
            } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_FILE)) {
                fileMessage.setVisibility(View.VISIBLE);
                fileName.setText(message);
                fileSize.setText(Utils.getFileSize(messageSize));
                fileExtension.setText(messageExtension);
            } else if (messageType.equals(CometChatConstants.MESSAGE_TYPE_AUDIO)) {
                audioMessage.setVisibility(View.VISIBLE);
                audioFileSize.setText(Utils.getFileSize(messageSize));
            } else if (messageType.equals(StringContract.IntentStrings.LOCATION)) {
                try {
                    locationMessage.setVisibility(View.VISIBLE);
                    JSONObject jsonObject = new JSONObject(message);
                    double LATITUDE = jsonObject.getDouble("latitude");
                    double LONGITUDE = jsonObject.getDouble("longitude");
                    tvPlaceName.setVisibility(View.GONE);
                    String mapUrl = StringContract.MapUrl.MAPS_URL +LATITUDE+","+LONGITUDE+"&key="+ StringContract.MapUrl.MAP_ACCESS_KEY;
                    Glide.with(this)
                            .load(mapUrl)
                            .into(ivMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (messageType.equals(StringContract.IntentStrings.POLLS)) {
                pollsMessage.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    String questionStr = jsonObject.getString("question");
                    question.setText(questionStr);
                    JSONObject options = jsonObject.getJSONObject("options");
                    for (int i=0;i<options.length();i++) {
                        LinearLayout linearLayout = new LinearLayout(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout
                                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.bottomMargin = (int) Utils.dpToPx(this, 8);
                        linearLayout.setLayoutParams(layoutParams);

                        linearLayout.setPadding(8,8,8,8);
                        linearLayout.setBackground(getResources()
                                .getDrawable(R.drawable.cc_message_bubble_right));
                        linearLayout.setBackgroundTintList(ColorStateList.valueOf(getResources()
                                .getColor(R.color.textColorWhite)));
                        TextView textViewPercentage = new TextView(this);
                        TextView textViewOption = new TextView(this);
                        textViewPercentage.setPadding(16, 4, 0, 4);
                        textViewOption.setPadding(16, 4, 0, 4);
                        textViewOption.setTextAppearance(this, R.style.TextAppearance_AppCompat_Medium);
                        textViewPercentage.setTextAppearance(this, R.style.TextAppearance_AppCompat_Medium);
                        textViewPercentage.setTextColor(getResources().getColor(R.color.primaryTextColor));
                        textViewOption.setTextColor(getResources().getColor(R.color.primaryTextColor));
                        String optionStr = options.getString(String.valueOf(i + 1));
                        textViewOption.setText(optionStr);
                        if (percentage>0)
                                textViewPercentage.setText(percentage + "% ");
                        if (optionGroup.getChildCount()!=options.length()) {
                            linearLayout.addView(textViewPercentage);
                            linearLayout.addView(textViewOption);
                            optionGroup.addView(linearLayout);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        addReceiptListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        CometChat.removeMessageListener(TAG);
    }
}