package com.cometchat.pro.uikit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import listeners.ComposeActionListener;

public class ComposeBox extends RelativeLayout implements View.OnClickListener {

    private ImageView ivCamera,ivGallary, ivFile,ivSend,ivArrow;

    private EditText etCompose;

    private RelativeLayout rlActionContainer;

    private boolean hasFocus;

    private ComposeActionListener composeActionListener;

    private Context context;

    private int color;

    public ComposeBox(Context context) {
        super(context);
        initViewComponent(context,null,-1,-1);
    }

    public ComposeBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewComponent(context,attrs,-1,-1);
    }

    public ComposeBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViewComponent(context,attrs,defStyleAttr,-1);
    }

    private void initViewComponent(Context context,AttributeSet attributeSet,int defStyleAttr,int defStyleRes){

        View view =View.inflate(context, R.layout.layout_compose_box,null);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.ComposeBox, 0, 0);
        color = a.getColor(R.styleable.ComposeBox_color,getResources().getColor(R.color.colorPrimary));
        addView(view);

        this.context=context;

        ViewGroup viewGroup=(ViewGroup)view.getParent();
        viewGroup.setClipChildren(false);


        ivCamera=this.findViewById(R.id.ivCamera);
        ivGallary=this.findViewById(R.id.ivImage);

        ivFile =this.findViewById(R.id.ivFile);
        ivSend=this.findViewById(R.id.ivSend);
        ivArrow=this.findViewById(R.id.ivArrow);
        etCompose=this.findViewById(R.id.etComposeBox);
        rlActionContainer=this.findViewById(R.id.rlActionContainer);
        rlActionContainer.setVisibility(VISIBLE);

        ivArrow.setImageTintList(ColorStateList.valueOf(color));
        ivCamera.setImageTintList(ColorStateList.valueOf(color));
        ivGallary.setImageTintList(ColorStateList.valueOf(color));

        ivFile.setImageTintList(ColorStateList.valueOf(color));
        ivSend.setImageTintList(ColorStateList.valueOf(color));

        ivSend.setOnClickListener(this);
        ivFile.setOnClickListener(this);

        ivGallary.setOnClickListener(this);
        ivCamera.setOnClickListener(this);


        etCompose.setOnFocusChangeListener((view1, b) -> {
             this.hasFocus=b;
            if (b){
                rlActionContainer.setVisibility(GONE);
                ivArrow.setVisibility(VISIBLE);
            }else {
                rlActionContainer.setVisibility(VISIBLE);
                ivArrow.setVisibility(GONE);
            }
        });

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (composeActionListener!=null){
                    composeActionListener.beforeTextChanged(charSequence,i,i1,i2);
                }


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (composeActionListener!=null){
                    composeActionListener.onTextChanged(charSequence,i,i1,i2);
                }
                 if (charSequence.length()>0){
                     rlActionContainer.setVisibility(GONE);
                     ivArrow.setVisibility(VISIBLE);
                 }else {
                      if (hasFocus) {
                          rlActionContainer.setVisibility(VISIBLE);
                          ivArrow.setVisibility(GONE);
                      }
                 }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (composeActionListener!=null){
                    composeActionListener.afterTextChanged(editable);
                }
            }
        });

        ivArrow.setOnClickListener(view12 -> {
            if (rlActionContainer.getVisibility()==View.GONE){
                rlActionContainer.setVisibility(VISIBLE);
                view12.setVisibility(GONE);
            }
        });

        a.recycle();
    }
    public void setText(String text)
    {
        etCompose.setText(text);
    }
    public void setColor(int color)
    {

        ivSend.setImageTintList(ColorStateList.valueOf(color));
        ivCamera.setImageTintList(ColorStateList.valueOf(color));
        ivGallary.setImageTintList(ColorStateList.valueOf(color));
        ivFile.setImageTintList(ColorStateList.valueOf(color));

        ivArrow.setImageTintList(ColorStateList.valueOf(color));
    }
    public void setComposeBoxListener(ComposeActionListener composeActionListener){
        this.composeActionListener=composeActionListener;

        this.composeActionListener.getCameraActionView(ivCamera);
        this.composeActionListener.getGalleryActionView(ivGallary);
        this.composeActionListener.getFileActionView(ivFile);

    }

    @Override
    public void onClick(View view) {

       if (view.getId()==R.id.ivCamera){
           composeActionListener.onCameraActionClicked(ivCamera);

       }
       if (view.getId()==R.id.ivImage){
           composeActionListener.onGalleryActionClicked(ivGallary);

       }
       if (view.getId()==R.id.ivSend){
           composeActionListener.onSendActionClicked(etCompose);

       }

       if (view.getId()==R.id.ivFile){
           composeActionListener.onFileActionClicked(ivFile);
       }

    }
}
