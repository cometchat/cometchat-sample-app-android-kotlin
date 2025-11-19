package com.cometchat.chatuikit.shared.views.formbubble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Interaction;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.collaborative.CometChatWebViewActivity;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.models.interactiveactions.APIAction;
import com.cometchat.chatuikit.shared.models.interactiveactions.DeepLinkingAction;
import com.cometchat.chatuikit.shared.models.interactiveactions.URLNavigationAction;
import com.cometchat.chatuikit.shared.models.interactiveelements.BaseInputElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.ButtonElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.CheckboxElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.DateTimeElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.ElementEntity;
import com.cometchat.chatuikit.shared.models.interactiveelements.LabelElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.OptionElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.RadioButtonElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.SingleSelectElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.SpinnerElement;
import com.cometchat.chatuikit.shared.models.interactiveelements.TextInputElement;
import com.cometchat.chatuikit.shared.models.interactivemessage.FormMessage;
import com.cometchat.chatuikit.shared.models.interactivemessage.InteractiveConstants;
import com.cometchat.chatuikit.shared.resources.apicontroller.ApiController;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.quickview.CometChatQuickView;
import com.cometchat.chatuikit.shared.views.quickview.QuickViewStyle;
import com.cometchat.chatuikit.shared.views.schedulerbubble.SchedulerUtils;
import com.cometchat.chatuikit.shared.views.singleselect.CometChatSingleSelect;
import com.cometchat.chatuikit.shared.views.singleselect.SingleSelectStyle;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CometChatFormBubble extends MaterialCardView {
    private static final String TAG = CometChatFormBubble.class.getSimpleName();

    private LinearLayout formLayout, formContainer, quickViewContainer;
    private CometChatQuickView quickView;
    private Context context;
    private TextView title, separator;
    private List<ElementEntity> elementEntities;
    private FormBubbleStyle formBubbleStyle;
    private FormMessage formMessage;
    private HashMap<String, View> validateViews;
    private SingleSelectStyle singleSelectStyle;
    private List<BaseInputElement> baseInputElements;
    private OnSubmitClick onSubmitClick;
    private TextView goalCompletionTextView;
    private JSONArray responseJsonArray;
    private MaterialCardView formCard;
    private QuickViewStyle quickViewStyle;

    private Activity activity;

    public CometChatFormBubble(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        activity = Utils.getActivity(context);
        responseJsonArray = new JSONArray();
        baseInputElements = new ArrayList<>();
        validateViews = new HashMap<>();
        View view = View.inflate(context, R.layout.cometchat_form_bubble, null);
        formLayout = view.findViewById(R.id.form_base);
        formContainer = view.findViewById(R.id.form_container);
        separator = view.findViewById(R.id.separator);
        formCard = view.findViewById(R.id.form_card);
        quickViewContainer = view.findViewById(R.id.quick_view_container);
        quickView = view.findViewById(R.id.quick_view);
        title = view.findViewById(R.id.form_title);
        goalCompletionTextView = view.findViewById(R.id.quick_view_message);
        Utils.initMaterialCard(formCard);
        Utils.initMaterialCard(this);
        setFormContainerMargin(-1, -1, -1, 20);
        addView(view);
    }

    public void setFormContainerMargin(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) formContainer.getLayoutParams();
        layoutParams.topMargin = topMargin > -1 ? Utils.convertDpToPx(getContext(), topMargin) : Utils.convertDpToPx(getContext(),
                                                                                                                     layoutParams.topMargin);
        layoutParams.bottomMargin = bottomMargin > -1 ? Utils.convertDpToPx(getContext(), bottomMargin) : Utils.convertDpToPx(getContext(), 10);
        layoutParams.rightMargin = rightMargin > -1 ? Utils.convertDpToPx(getContext(), bottomMargin) : Utils.convertDpToPx(getContext(),
                                                                                                                            layoutParams.rightMargin);
        layoutParams.leftMargin = leftMargin > -1 ? Utils.convertDpToPx(getContext(), bottomMargin) : Utils.convertDpToPx(getContext(),
                                                                                                                          layoutParams.leftMargin);
        formContainer.setLayoutParams(layoutParams);
    }

    public CometChatFormBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CometChatFormBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void showQuickView() {
        quickViewContainer.setVisibility(VISIBLE);
        formCard.setVisibility(GONE);
    }

    public void showForm() {
        quickViewContainer.setVisibility(GONE);
        formCard.setVisibility(VISIBLE);
    }

    public void setSubmitButton(ButtonElement button) {
        if (button != null) {
            buildButton(button, true);
        }
    }

    public void setTitle(String title) {
        setTitleText(title);
    }

    private void buildForm() {
        validateViews.clear();
        formLayout.removeAllViews();
        for (ElementEntity elementEntity : elementEntities) {
            if (elementEntity != null) {
                switch (elementEntity.getElementType()) {
                    case UIKitConstants.UIElementsType.UI_ELEMENT_LABEL:
                        buildLabel((LabelElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_TEXT_INPUT:
                        buildTextInput((TextInputElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_CHECKBOX:
                        buildCheckbox((CheckboxElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_RADIO_BUTTON:
                        buildRadioButton((RadioButtonElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_SINGLE_SELECT:
                        buildSingleSelectButton((SingleSelectElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_SPINNER:
                        buildSpinner((SpinnerElement) elementEntity);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_BUTTON:
                        buildButton((ButtonElement) elementEntity, false);
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_DATE_TIME:
                        buildDateTime((DateTimeElement) elementEntity);
                        break;
                    default:
                        Log.i(TAG, "buildForm: UI Type not supported");
                }
            }
        }
    }

    private void buildDateTime(DateTimeElement elementEntity) {
        if (elementEntity.getLabel() != null && !elementEntity.getLabel().isEmpty())
            addViewToFormLayout(createTextView(elementEntity.getLabel() + (!elementEntity.isOptional() ? " *" : "")), 12);

        View view = View.inflate(context, R.layout.cometchat_material_from_edittext, null);
        EditText textInputEditText = view.findViewById(R.id.form_edit_ext);
        SimpleDateFormat outputFormat = Utils.getDateFormat(elementEntity);
        SimpleDateFormat readFormat = Utils.getDateTimeReadFormat(elementEntity);
        if (elementEntity.getPlaceHolder() == null) {
            textInputEditText.setHint(outputFormat.toLocalizedPattern());
        } else {
            textInputEditText.setHint(elementEntity.getPlaceHolder().getHint());
        }

        if (elementEntity.getDefaultValue() != null && !elementEntity.getDefaultValue().isEmpty()) {
            try {
                Date date = readFormat.parse(elementEntity.getDefaultValue());
                String formattedDateTime = outputFormat.format(date);
                textInputEditText.setText(formattedDateTime);
            } catch (ParseException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        }
        textInputEditText.setFocusable(false);
        CalendarConstraints constraintsBuilder;
        MaterialDatePicker.Builder dateBuilder = MaterialDatePicker.Builder.datePicker();
        long startDay = 0, endDay = 0, startDateTime = 0, endDateTime = 0;
        if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.DATE)) {
            startDay = SchedulerUtils.getDateFromString(elementEntity.getFrom(), "0000", elementEntity.getTimeZoneCode());
            endDay = SchedulerUtils.getDateFromString(elementEntity.getTo(), "2359", elementEntity.getTimeZoneCode());
        } else if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME)) {
            startDay = SchedulerUtils.getDateFromString(elementEntity.getFrom(), elementEntity.getTimeZoneCode());
            endDay = SchedulerUtils.getDateFromString(elementEntity.getTo(), elementEntity.getTimeZoneCode());
            startDateTime = SchedulerUtils.getDateFromString(elementEntity.getFrom(), "0000", elementEntity.getTimeZoneCode());
            endDateTime = SchedulerUtils.getDateFromString(elementEntity.getTo(), "2359", elementEntity.getTimeZoneCode());
        } else if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.TIME)) {
            int[] fromTime = SchedulerUtils.getHrsAndMin(elementEntity.getFrom());
            int[] toTime = SchedulerUtils.getHrsAndMin(elementEntity.getTo());
            startDay = SchedulerUtils.getDateFromString(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()),
                                                        fromTime[0] + "" + fromTime[1],
                                                        elementEntity.getTimeZoneCode());
            endDay = SchedulerUtils.getDateFromString(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()),
                                                      toTime[0] + "" + toTime[1],
                                                      elementEntity.getTimeZoneCode());
        }
        long finalStartDay = startDay;
        long finalEndDay = endDay;
        long finalStartDateTime = startDateTime;
        long finalEndDateTime = endDateTime;
        constraintsBuilder = new CalendarConstraints.Builder()
            .setStart(startDay)
            .setEnd(endDay)
            .setValidator(new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME))
                        return !(date < finalStartDateTime || date > finalEndDateTime);

                    return !(date < finalStartDay || date > finalEndDay);
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(@NonNull Parcel parcel, int i) {
                    parcel.writeLong(finalStartDay);
                    parcel.writeLong(finalEndDay);
                }
            })
            .build();
        dateBuilder.setCalendarConstraints(constraintsBuilder);
        dateBuilder.setTitleText(elementEntity.getLabel() != null && !elementEntity
            .getLabel()
            .isEmpty() ? elementEntity.getLabel() : "Select a Date");
        MaterialDatePicker datePicker = dateBuilder.build();
        datePicker.setCancelable(false);

        MaterialTimePicker.Builder timeBuilder = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText(elementEntity.getLabel() != null && !elementEntity.getLabel().isEmpty() ? elementEntity.getLabel() : "Select a Time");
        MaterialTimePicker timePicker = timeBuilder.build();
        timePicker.setCancelable(false);

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME)) {
                if (!timePicker.isAdded() && Utils.isActivityUsable(activity) && activity instanceof FragmentActivity)
                    timePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "TIME_PICKER");
            } else {
                textInputEditText.setText(Utils.getDateFormat(elementEntity).format(selection));
                elementEntity.setResponse(readFormat.format(selection));
                validateDateTime(elementEntity);
            }
        });

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            Calendar selectedTime = null;
            long selectedTimeInMillis = 0;
            if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.TIME))
                selectedTime = Calendar.getInstance();
            else if (elementEntity.getMode().equals(UIKitConstants.DateTimeMode.DATE_TIME)) {
                selectedTime = Calendar.getInstance();
                if (datePicker.getSelection() != null) {
                    selectedTime.setTimeInMillis((Long) datePicker.getSelection());
                }
            }
            if (selectedTime != null) {
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTimeInMillis = selectedTime.getTimeInMillis();
            }

            if (selectedTimeInMillis < finalStartDay || selectedTimeInMillis > finalEndDay) {
                textInputEditText.setText("");
                elementEntity.setResponse("");
            } else {
                textInputEditText.setText(Utils.getDateFormat(elementEntity).format(selectedTimeInMillis));
                elementEntity.setResponse(readFormat.format(selectedTimeInMillis));
            }
            validateDateTime(elementEntity);
        });

        textInputEditText.setOnClickListener(view1 -> {
            if (!elementEntity.getMode().equals(UIKitConstants.DateTimeMode.TIME)) {
                if (!datePicker.isAdded() && Utils.isActivityUsable(activity) && activity instanceof FragmentActivity)
                    datePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "DATE_PICKER");
            } else {
                if (!timePicker.isAdded() && Utils.isActivityUsable(activity) && activity instanceof FragmentActivity)
                    timePicker.show(((FragmentActivity) activity).getSupportFragmentManager(), "TIME_PICKER");
            }
        });

        textInputEditText.setMinWidth(Utils.convertDpToPx(context, 200));
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_border_outline, null);
        if (formBubbleStyle != null) {
            if (formBubbleStyle.getInputTextColor() != 0) {
                textInputEditText.setTextColor(formBubbleStyle.getInputTextColor());
            }
            if (formBubbleStyle.getBackground() != 0) {
                textInputEditText.setBackgroundColor(formBubbleStyle.getBackground());
            }
            if (formBubbleStyle.getInputTextAppearance() != 0)
                textInputEditText.setTextAppearance(formBubbleStyle.getInputTextAppearance());
            if (formBubbleStyle.getInputHintColor() != 0)
                textInputEditText.setHintTextColor(formBubbleStyle.getInputHintColor());
            if (formBubbleStyle.getInputStrokeColor() != 0) {
                if (drawable != null) {
                    drawable.setTint(formBubbleStyle.getInputStrokeColor());
                }
            }
        }
        textInputEditText.setBackground(drawable);
        textInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                                                                          null,
                                                                          ResourcesCompat.getDrawable(getResources(),
                                                                                                      R.drawable.cometchat_ic_calendar,
                                                                                                      null),
                                                                          null);
        validateViews.put(elementEntity.getElementId(), textInputEditText);
        addViewToFormLayout(view, 4);
    }

    public void setTitleText(String text) {
        if (text != null && !text.isEmpty()) {
            title.setVisibility(VISIBLE);
            title.setText(text);
            if (formBubbleStyle != null) {
                if (formBubbleStyle.getTitleColor() != 0)
                    title.setTextColor(formBubbleStyle.getTitleColor());
                if (formBubbleStyle.getTitleAppearance() != 0)
                    title.setTextAppearance(formBubbleStyle.getTitleAppearance());
            }
        } else {
            title.setVisibility(GONE);
        }
    }

    private void buildLabel(LabelElement uiElement) {
        if (uiElement.getText() == null || uiElement.getText().isEmpty()) return;
        TextView textView = createTextView(uiElement.getText());
        textView.setTag(uiElement.getElementId());
        addViewToFormLayout(textView, 12);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(context);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        if (formBubbleStyle != null) {
            if (formBubbleStyle.getLabelColor() != 0)
                textView.setTextColor(formBubbleStyle.getLabelColor());
            if (formBubbleStyle.getLabelAppearance() != 0)
                textView.setTextAppearance(formBubbleStyle.getLabelAppearance());
        }
        textView.setText(text);
        return textView;
    }

    private void buildSpinner(SpinnerElement uiElement) {
        if (uiElement.getOptions() != null && uiElement.getOptions().size() > 0) {
            List<String> stringList = new ArrayList<>();
            stringList.add("Select an option");
            HashMap<String, OptionElement> map = new HashMap<>();
            for (OptionElement optionElement : uiElement.getOptions()) {
                if (optionElement != null && optionElement.getValue() != null && !optionElement.getValue().isEmpty()) {
                    stringList.add(optionElement.getValue());
                    map.put(optionElement.getValue(), optionElement);
                }
            }

            if (stringList.isEmpty()) return;
            if (uiElement.getLabel() != null && !uiElement.getLabel().isEmpty())
                addViewToFormLayout(createTextView(uiElement.getLabel() + (!uiElement.isOptional() ? " *" : "")), 12);

            Spinner spinner = new Spinner(context);
            spinner.setPadding(10, 10, 100, 10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                   ViewGroup.LayoutParams.WRAP_CONTENT);
            spinner.setLayoutParams(layoutParams);
            spinner.setTag(uiElement.getElementId());
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_outline_dropdown1, null);
            spinner.setDropDownVerticalOffset(150);
            if (formBubbleStyle != null) {
                if (formBubbleStyle.getSpinnerBackgroundColor() != 0) {
                    if (drawable != null) {
                        drawable.setTint(formBubbleStyle.getSpinnerBackgroundColor());
                    }
                }
            }
            spinner.setBackground(drawable);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, stringList) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    if (formBubbleStyle != null) {
                        if (formBubbleStyle.getSpinnerTextColor() != 0)
                            textView.setTextColor(formBubbleStyle.getSpinnerTextColor());
                        if (formBubbleStyle.getSpinnerTextAppearance() != 0)
                            textView.setTextAppearance(formBubbleStyle.getSpinnerTextAppearance());
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    return super.getDropDownView(position, convertView, parent);
                }
            };

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                    resetSpinner(uiElement);
                    if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Select an option")) {
                        uiElement.setResponse("");
                    } else {
                        OptionElement selectedOption = map.get(parent.getItemAtPosition(position).toString());
                        if (selectedOption != null) {
                            String sId = selectedOption.getId();
                            if (sId != null) {
                                uiElement.setResponse(sId);
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            addViewToFormLayout(spinner, 4);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.post(new Runnable() {
                @Override
                public void run() {
                    if (uiElement.getDefaultValue() != null && !uiElement.getDefaultValue().isEmpty()) {
                        for (int i = 0; i < uiElement.getOptions().size(); i++) {
                            if (uiElement.getOptions().get(i).getId().equalsIgnoreCase(uiElement.getDefaultValue())) {
                                spinner.setSelection(i + 1);
                                uiElement.setResponse(uiElement.getDefaultValue());
                                break;
                            }
                        }
                    }
                }
            });
            validateViews.put(uiElement.getElementId(), spinner);
        }
    }

    public void resetSpinner(SpinnerElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            Spinner spinner = (Spinner) validateViews.get(element.getElementId());
            if (spinner != null) {
                if (formBubbleStyle != null && formBubbleStyle.getSpinnerBackgroundColor() != 0) {
                    Drawable drawable = spinner.getBackground();
                    drawable.setTint(formBubbleStyle.getSpinnerBackgroundColor());
                    spinner.setBackground(drawable);
                }
            }
        }
    }

    private void buildTextInput(TextInputElement uiElement) {
        if (uiElement.getLabel() != null && !uiElement.getLabel().isEmpty())
            addViewToFormLayout(createTextView(uiElement.getLabel() + (!uiElement.isOptional() ? " *" : "")), 12);

        View view = View.inflate(context, R.layout.cometchat_material_from_edittext, null);

        EditText textInputEditText = view.findViewById(R.id.form_edit_ext);
        textInputEditText.setMinWidth(Utils.convertDpToPx(context, 200));
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_border_outline, null);
        if (formBubbleStyle != null) {
            if (formBubbleStyle.getInputTextColor() != 0) {
                textInputEditText.setTextColor(formBubbleStyle.getInputTextColor());
            }
            if (formBubbleStyle.getBackground() != 0) {
                textInputEditText.setBackgroundColor(formBubbleStyle.getBackground());
            }
            if (formBubbleStyle.getInputTextAppearance() != 0)
                textInputEditText.setTextAppearance(formBubbleStyle.getInputTextAppearance());
            if (formBubbleStyle.getInputHintColor() != 0)
                textInputEditText.setHintTextColor(formBubbleStyle.getInputHintColor());
            if (formBubbleStyle.getInputStrokeColor() != 0) {
                if (drawable != null) {
                    drawable.setTint(formBubbleStyle.getInputStrokeColor());
                }
            }
        }
        textInputEditText.setBackground(drawable);
        if (uiElement.getPlaceHolder() != null)
            textInputEditText.setHint(uiElement.getPlaceHolder().getHint());
        if (uiElement.getDefaultValue() != null)
            textInputEditText.setText(uiElement.getDefaultValue());
        textInputEditText.setMaxLines(uiElement.getMaxLines() == 0 ? 1 : uiElement.getMaxLines());
        textInputEditText.setTag(uiElement.getElementId());

        validateViews.put(uiElement.getElementId(), textInputEditText);

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                uiElement.setResponse(charSequence.toString());
                if (charSequence.length() == 0 && !uiElement.isOptional()) {
                    if (!uiElement.isOptional()) {
                        if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                            if (drawable != null) {
                                drawable.setTint(formBubbleStyle.getErrorColor());
                            }
                            textInputEditText.setBackground(drawable);
                        }
                        textInputEditText.requestFocus();
                    } else {
                        if (formBubbleStyle != null && formBubbleStyle.getInputStrokeColor() != 0) {
                            if (drawable != null) {
                                drawable.setTint(formBubbleStyle.getInputStrokeColor());
                            }
                            textInputEditText.setBackground(drawable);
                        }
                    }
                } else if (charSequence.length() > 0) {
                    if (formBubbleStyle != null) {
                        if (formBubbleStyle.getActiveInputStrokeColor() != 0) {
                            if (drawable != null) {
                                drawable.setTint(formBubbleStyle.getActiveInputStrokeColor());
                            }
                            textInputEditText.setBackground(drawable);
                        } else if (formBubbleStyle.getInputStrokeColor() != 0) {
                            if (drawable != null) {
                                drawable.setTint(formBubbleStyle.getInputStrokeColor());
                            }
                            textInputEditText.setBackground(drawable);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        textInputEditText.setTag(uiElement.getElementId());
        addViewToFormLayout(view, 4);
    }

    private void buildCheckbox(CheckboxElement uiElement) {
        uiElement.setResponse(new ArrayList<>());
        if (uiElement.getOptions() != null && uiElement.getOptions().size() > 0) {
            if (uiElement.getLabel() != null && !uiElement.getLabel().isEmpty())
                addViewToFormLayout(createTextView(uiElement.getLabel() + (!uiElement.isOptional() ? " *" : "")), 12);

            for (OptionElement optionElement : uiElement.getOptions()) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setButtonDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_default_checkbox, null));
                if (formBubbleStyle != null) {
                    if (formBubbleStyle.getDefaultCheckboxButtonTint() != 0) {
                        checkBox.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getDefaultCheckboxButtonTint()));
                    }
                    if (formBubbleStyle.getCheckboxTextColor() != 0) {
                        checkBox.setTextColor(formBubbleStyle.getCheckboxTextColor());
                    }
                    if (formBubbleStyle.getCheckboxTextAppearance() != 0) {
                        checkBox.setTextAppearance(formBubbleStyle.getCheckboxTextAppearance());
                    }
                }

                checkBox.setText(optionElement.getValue());
                checkBox.setTag(optionElement.getId());
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    resetCheckBox(uiElement);
                    if (uiElement.getResponse() == null) uiElement.setResponse(new ArrayList<>());
                    if (uiElement.getResponse().contains(compoundButton.getTag().toString())) {
                        uiElement.getResponse().remove(compoundButton.getTag().toString());
                    } else {
                        uiElement.getResponse().add(compoundButton.getTag().toString());
                    }
                    if (b) {
                        checkBox.setButtonDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_filled_check, null));
                        if (formBubbleStyle != null && formBubbleStyle.getSelectedCheckboxButtonTint() != 0)
                            checkBox.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getSelectedCheckboxButtonTint()));
                    } else {
                        checkBox.setButtonDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_default_checkbox, null));
                        if (formBubbleStyle != null && formBubbleStyle.getDefaultCheckboxButtonTint() != 0)
                            checkBox.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getDefaultCheckboxButtonTint()));
                    }
                });
                addViewToFormLayout(checkBox, 3);
                if (uiElement.getDefaultValue() != null) {
                    boolean isDefault = uiElement.getDefaultValue().contains(optionElement.getId());
                    checkBox.setChecked(isDefault);
                    checkBox.setSelected(isDefault);
                }
                checkBox.setPadding(20, 0, 0, 0);
                validateViews.put(optionElement.getId(), checkBox);
            }
        } else {
            Log.i("buildCheckbox", "buildCheckbox: No options found");
        }
    }

    public void resetCheckBox(CheckboxElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            for (OptionElement optionElement : element.getOptions()) {
                CheckBox checkBox = (CheckBox) validateViews.get(optionElement.getId());
                if (checkBox != null) {
                    if (formBubbleStyle != null && formBubbleStyle.getDefaultCheckboxButtonTint() != 0) {
                        checkBox.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getDefaultCheckboxButtonTint()));
                    }
                }
            }
        }
    }

    private void buildRadioButton(RadioButtonElement uiElement) {
        if (uiElement.getOptions() != null && uiElement.getOptions().size() > 0) {
            if (uiElement.getLabel() != null && !uiElement.getLabel().isEmpty())
                addViewToFormLayout(createTextView(uiElement.getLabel() + (!uiElement.isOptional() ? " *" : "")), 12);

            RadioGroup radioGroup = new RadioGroup(context);
            for (OptionElement optionElement : uiElement.getOptions()) {
                if (optionElement.getValue() != null && !optionElement.getValue().isEmpty() && optionElement.getId() != null && !optionElement
                    .getId()
                    .isEmpty()) {
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setText(optionElement.getValue());
                    if (formBubbleStyle != null && formBubbleStyle.getRadioButtonTextColor() != 0)
                        radioButton.setTextColor(formBubbleStyle.getRadioButtonTextColor());
                    if (formBubbleStyle != null && formBubbleStyle.getRadioButtonTextAppearance() != 0)
                        radioButton.setTextAppearance(formBubbleStyle.getRadioButtonTextAppearance());
                    if (formBubbleStyle != null && formBubbleStyle.getRadioButtonTint() != 0)
                        radioButton.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getRadioButtonTint()));
                    radioButton.setTag(optionElement.getId());
                    radioButton.setOnCheckedChangeListener((compoundButton, selected) -> {
                        if (selected) {
                            resetRadioElement(uiElement);
                            uiElement.setResponse(compoundButton.getTag().toString());
                            if (formBubbleStyle != null && formBubbleStyle.getSelectedRadioButtonTint() != 0)
                                radioButton.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getSelectedRadioButtonTint()));
                        } else {
                            if (formBubbleStyle != null && formBubbleStyle.getRadioButtonTint() != 0)
                                radioButton.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getRadioButtonTint()));
                        }
                    });
                    radioGroup.addView(radioButton);
                }
            }
            if (uiElement.getDefaultValue() != null) {
                RadioButton radioButton = radioGroup.findViewWithTag(uiElement.getDefaultValue());
                if (radioButton != null) {
                    radioButton.setChecked(true);
                }
            }
            validateViews.put(uiElement.getElementId(), radioGroup);
            addViewToFormLayout(radioGroup, 4);
        } else {
            Log.i(TAG, "buildRadioButton: No options found");
        }
    }

    public void resetRadioElement(RadioButtonElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            RadioGroup radioGroup = (RadioGroup) validateViews.get(element.getElementId());
            if (radioGroup != null) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                    if (formBubbleStyle != null && formBubbleStyle.getRadioButtonTint() != 0) {
                        radioButton.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getRadioButtonTint()));
                    }
                }
            }
        }
    }

    private void buildSingleSelectButton(SingleSelectElement uiElement) {
        if (uiElement.getOptions() != null && !uiElement.getOptions().isEmpty()) {
            CometChatSingleSelect singleSelect = new CometChatSingleSelect(context);
            singleSelect.setStyle(singleSelectStyle);
            singleSelect.setOptions(uiElement.getOptions());
            if (uiElement.getLabel() != null && !uiElement.getLabel().isEmpty()) {
                singleSelect.setTitle(uiElement.getLabel() + (!uiElement.isOptional() ? " *" : ""));
            }
            singleSelect.setOnSelectChangeListener((compoundButton, selected) -> {
                if (selected) {
                    uiElement.setResponse(compoundButton.getTag().toString());
                }
            });
            if (uiElement.getDefaultValue() != null) {
                RadioButton radioButton = singleSelect.getRadioGroup().findViewWithTag(uiElement.getDefaultValue());
                if (radioButton != null) {
                    radioButton.setChecked(true);
                }
            }
            validateViews.put(uiElement.getElementId(), singleSelect.getRadioGroup());
            addViewToFormLayout(singleSelect, 12);
        } else {
            CometChatLogger.i(TAG, "No options found");
        }
    }

    private void buildButton(ButtonElement buttonElement, boolean isSubmitButton) {
        View buttonView = View.inflate(context, R.layout.cometchat_form_button_material, null);
        MaterialCardView button = buttonView.findViewById(R.id.form_button);
        MaterialCardView buttonCard = buttonView.findViewById(R.id.form_button_card);
        TextView buttonTitle = buttonView.findViewById(R.id.button_text);
        ProgressBar progressBar = buttonView.findViewById(R.id.button_progress);
        if (formBubbleStyle != null) {
            if (formBubbleStyle.getProgressBarTintColor() != 0) {
                progressBar
                    .getIndeterminateDrawable()
                    .setColorFilter(formBubbleStyle.getProgressBarTintColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            if (formBubbleStyle.getButtonBackgroundColor() != 0) {
                buttonCard.setBackgroundColor(formBubbleStyle.getButtonBackgroundColor());
            }
            if (formBubbleStyle.getButtonTextColor() != 0)
                buttonTitle.setTextColor(formBubbleStyle.getButtonTextColor());
            if (formBubbleStyle.getButtonTextAppearance() != 0)
                buttonTitle.setTextAppearance(formBubbleStyle.getButtonTextAppearance());
        }
        button.setCardBackgroundColor(Color.TRANSPARENT);
        buttonTitle.setText(buttonElement.getText());
        button.setTag(buttonElement.getElementId());
        buttonCard.setOnClickListener(view -> {
            InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(getWindowToken(), 0);
            if (isSubmitButton) {
                if (validateForm()) {
                    if (onSubmitClick != null) {
                        onSubmitClick.onSubmitClick(context, baseInputElements);
                    } else {
                        performAction(buttonElement, true);
                    }
                }
            } else {
                performAction(buttonElement, false);
            }
        });
        validateViews.put(buttonElement.getElementId(), buttonCard);
        validateViews.put(buttonElement.getElementId() + "_text", buttonTitle);
        validateViews.put(buttonElement.getElementId() + "_progress", progressBar);
        disableButton(buttonElement, buttonCard);
        addViewToFormLayout(buttonView, 16);
        button.setRadius(16);
        buttonCard.setRadius(16);
    }

    private void disableButton(ButtonElement buttonElement, MaterialCardView button) {
        if (!Utils.isGoalCompleted(formMessage)) {
            if (buttonElement.isDisableAfterInteracted()) {
                if (formMessage.getInteractions() != null && formMessage.getInteractions().size() > 0) {
                    for (int i = 0; i < formMessage.getInteractions().size(); i++) {
                        Interaction interaction = formMessage.getInteractions().get(i);
                        if (interaction.getElementId().equals(buttonElement.getElementId())) {
                            changeButtonColorToDisable(button);
                            break;
                        }
                    }
                }
            }

            if (!formMessage.isAllowSenderInteraction() && formMessage.getSender() != null && formMessage
                .getSender()
                .getUid()
                .equals(CometChatUIKit.getLoggedInUser().getUid())) {
                changeButtonColorToDisable(button);
            }
        } else {
            showQuickView();
        }
    }

    private void changeButtonColorToDisable(MaterialCardView button) {
        ColorDrawable colorDrawable = (ColorDrawable) button.getBackground();
        int newColor = Utils.multiplyColorAlpha(colorDrawable.getColor(), 150);
        button.setBackgroundColor(newColor);
        button.setEnabled(false);
    }

    private void performAction(ButtonElement buttonElement, boolean isSubmit) {
        if (buttonElement.getAction() != null && !buttonElement.getAction().getActionType().isEmpty()) {
            switch (buttonElement.getAction().getActionType()) {
                case InteractiveConstants.ACTION_TYPE_API_ACTION:
                    TextView textView = (TextView) validateViews.get(buttonElement.getElementId() + "_text");
                    MaterialCardView button = (MaterialCardView) validateViews.get(buttonElement.getElementId());
                    ProgressBar progressBar = (ProgressBar) validateViews.get(buttonElement.getElementId() + "_progress");
                    if (progressBar != null) {
                        progressBar.setVisibility(VISIBLE);
                    }
                    if (textView != null) {
                        textView.setVisibility(GONE);
                    }
                    if (button != null) {
                        button.setEnabled(false);
                    }
                    APIAction apiAction = (APIAction) buttonElement.getAction();
                    JSONObject jsonObject = Utils.getInteractiveRequestPayload(apiAction.getPayload(), buttonElement.getElementId(), formMessage);
                    try {
                        JSONObject responseJson = jsonObject.getJSONObject(InteractiveConstants.InteractiveRequestPayload.DATA);
                        responseJson.put(InteractiveConstants.InteractiveRequestPayload.FORM_DATA, responseJsonArray);
                        jsonObject.put(InteractiveConstants.InteractiveRequestPayload.DATA, responseJson);
                    } catch (Exception e) {
                        CometChatLogger.e(TAG, e.toString());
                    }
                    ApiController
                        .getInstance()
                        .call(apiAction.getMethod(), apiAction.getUrl(), jsonObject, apiAction.getHeaders(), new ApiController.APICallback() {
                            @Override
                            public void onSuccess(String response) {
                                if (progressBar != null) {
                                    progressBar.setVisibility(GONE);
                                }
                                if (textView != null) {
                                    textView.setVisibility(VISIBLE);
                                }
                                if (button != null) {
                                    button.setEnabled(true);
                                }
                                markInteract(buttonElement, button);
                            }

                            @Override
                            public void onError(Exception e) {
                                if (progressBar != null) {
                                    progressBar.setVisibility(GONE);
                                }
                                if (textView != null) {
                                    textView.setVisibility(VISIBLE);
                                }
                                if (button != null) {
                                    button.setEnabled(true);
                                }
                            }
                        });

                    break;
                case InteractiveConstants.ACTION_TYPE_URL_NAVIGATION:
                    URLNavigationAction urlNavigationAction = (URLNavigationAction) buttonElement.getAction();
                    if (urlNavigationAction.getUrl() != null && !urlNavigationAction.getUrl().isEmpty()) {
                        Intent intent = new Intent(context, CometChatWebViewActivity.class);
                        intent.putExtra(UIKitConstants.IntentStrings.URL, urlNavigationAction.getUrl());
                        context.startActivity(intent);
                        markInteract(buttonElement, (MaterialCardView) validateViews.get(buttonElement.getElementId()));
                    }
                    break;
                case InteractiveConstants.ACTION_TYPE_DEEP_LINKING:
                    DeepLinkingAction deepLinking = (DeepLinkingAction) buttonElement.getAction();
                    if (deepLinking.getUrl() != null && !deepLinking.getUrl().isEmpty()) {
                        String url = deepLinking.getUrl();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        if (i.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(i);
                            markInteract(buttonElement, (MaterialCardView) validateViews.get(buttonElement.getElementId()));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void markInteract(ButtonElement buttonElement, MaterialCardView buttonView) {
        CometChat.markAsInteracted(formMessage.getId(), buttonElement.getElementId(), new CometChat.CallbackListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                formMessage.getInteractions().add(new Interaction(buttonElement.getElementId(), System.currentTimeMillis() / 100));
                disableButton(buttonElement, buttonView);
            }

            @Override
            public void onError(CometChatException e) {
                CometChatLogger.e(TAG, e.toString());
            }
        });
    }

    public void setOnSubmitClick(OnSubmitClick onSubmitClick) {
        if (onSubmitClick != null) this.onSubmitClick = onSubmitClick;
    }

    private boolean validateForm() {
        baseInputElements.clear();
        responseJsonArray = new JSONArray();
        boolean validate = true;
        for (ElementEntity element : elementEntities) {
            if (element instanceof BaseInputElement) {
                switch (element.getElementType()) {
                    case UIKitConstants.UIElementsType.UI_ELEMENT_TEXT_INPUT:
                        if (!validateTextInput((TextInputElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_RADIO_BUTTON:
                        if (!validateRadioButtons((RadioButtonElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_SINGLE_SELECT:
                        if (!validateSingleSelect((SingleSelectElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_SPINNER:
                        if (!validateSpinner((SpinnerElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }

                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_CHECKBOX:
                        if (!validateCheckbox((CheckboxElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }
                        break;
                    case UIKitConstants.UIElementsType.UI_ELEMENT_DATE_TIME:
                        if (!validateDateTime((DateTimeElement) element)) {
                            if (validate) validate = false;
                        } else {
                            baseInputElements.add((BaseInputElement) element);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return validate;
    }

    private boolean validateDateTime(DateTimeElement element) {
        EditText textInputLayout = (EditText) validateViews.get(element.getElementId());
        if (textInputLayout == null) {
            return !(element.getResponse() == null || element.getResponse().isEmpty());
        }
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_border_outline, null);
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                if (drawable != null) {
                    drawable.setTint(formBubbleStyle.getErrorColor());
                }
                textInputLayout.setBackground(drawable);
            }
            return false;
        }
        if (formBubbleStyle != null && formBubbleStyle.getInputStrokeColor() != 0) {
            if (drawable != null) {
                drawable.setTint(formBubbleStyle.getInputStrokeColor());
            }
            textInputLayout.setBackground(drawable);
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    public boolean validateRadioButtons(RadioButtonElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            RadioGroup radioGroup = (RadioGroup) validateViews.get(element.getElementId());
            if (radioGroup != null) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                    if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                        radioButton.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getErrorColor()));
                    }
                }
                radioGroup.requestFocus();
            }
            return false;
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    public boolean validateSingleSelect(SingleSelectElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            RadioGroup radioGroup = (RadioGroup) validateViews.get(element.getElementId());
            if (radioGroup != null) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                    if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                        Drawable drawable = radioButton.getBackground();
                        drawable.setTint(formBubbleStyle.getErrorColor());
                        radioButton.setBackground(drawable);
                    }
                }
                radioGroup.requestFocus();
            }
            return false;
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    public boolean validateSpinner(SpinnerElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            Spinner spinner = (Spinner) validateViews.get(element.getElementId());
            if (spinner != null) {
                if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                    Drawable drawable = spinner.getBackground();
                    drawable.setTint(formBubbleStyle.getErrorColor());
                    spinner.setBackground(drawable);
                }
            }
            return false;
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    public boolean validateCheckbox(CheckboxElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            for (int i = 0; i < element.getOptions().size(); i++) {
                CheckBox checkBox = (CheckBox) validateViews.get(element.getOptions().get(i).getId());
                if (checkBox != null) {
                    if (formBubbleStyle != null && formBubbleStyle.getErrorCheckboxButtonTint() != 0) {
                        checkBox.setButtonTintList(ColorStateList.valueOf(formBubbleStyle.getErrorCheckboxButtonTint()));
                    }
                    checkBox.requestFocus();
                }
            }
            return false;
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    public boolean validateTextInput(TextInputElement element) {
        if (!element.isOptional() && (element.getResponse() == null || element.getResponse().isEmpty())) {
            EditText textInputLayout = (EditText) validateViews.get(element.getElementId());
            if (textInputLayout != null) {
                if (formBubbleStyle != null && formBubbleStyle.getErrorColor() != 0) {
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_border_outline, null);
                    if (drawable != null) {
                        drawable.setTint(formBubbleStyle.getErrorColor());
                    }
                    textInputLayout.setBackground(drawable);
                }
            }
            return false;
        }
        saveResponse(element.getElementId(), element.getResponse());
        return true;
    }

    private void addViewToFormLayout(View view, int topMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                               ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = Utils.convertDpToPx(context, topMargin);
        view.setLayoutParams(layoutParams);
        formLayout.addView(view);
    }

    public FormBubbleStyle getStyle() {
        return formBubbleStyle;
    }

    public void setStyle(FormBubbleStyle style) {
        if (style != null) {
            setTitleColor(style.getTitleColor());
            setTitleAppearance(style.getTitleAppearance());
            setSingleSelectStyle(style.getSingleSelectStyle());
            setSeparatorColor(style.getSeparatorColor());
            this.formBubbleStyle = style;
            if (style.getQuickViewStyle() != null) {
                this.quickViewStyle = style.getQuickViewStyle();
            }
            if (style.getDrawableBackground() != null)
                formCard.setBackground(style.getDrawableBackground());
            else if (style.getBackground() != 0)
                formCard.setCardBackgroundColor(style.getBackground());
            if (style.getStrokeWidth() >= 0) formCard.setStrokeWidth(style.getStrokeWidth());
            if (style.getCornerRadius() >= 0) formCard.setRadius(style.getCornerRadius());
            if (style.getStrokeColor() != 0) formCard.setStrokeColor(style.getStrokeColor());
        }
    }

    private void setTitleColor(@ColorInt int color) {
        if (color != 0) title.setTextColor(color);
    }

    private void setTitleAppearance(int appearance) {
        if (appearance != 0) title.setTextAppearance(appearance);
    }

    public void setSingleSelectStyle(SingleSelectStyle style) {
        if (style != null) {
            this.singleSelectStyle = style;
        }
    }

    public void setSeparatorColor(@ColorInt int color) {
        if (color != 0) {
            separator.setBackgroundColor(color);
        }
    }

    public LinearLayout getFormLayout() {
        return formLayout;
    }

    public List<ElementEntity> getUiElements() {
        return elementEntities;
    }

    private void setUiElements(List<ElementEntity> elementEntities) {
        this.elementEntities = elementEntities;
        if (elementEntities != null && !elementEntities.isEmpty()) {
            buildForm();
        }
    }

    public FormMessage getFormMessage() {
        return formMessage;
    }

    public void setFormMessage(FormMessage formMessage) {
        if (formMessage != null) {
            this.formMessage = formMessage;
            quickView.setTitle(CometChatUIKit.getLoggedInUser().getName());
            quickView.setSubTitle(formMessage.getTitle());
            quickView.setCloseIconVisibility(View.GONE);
            quickView.setStyle(quickViewStyle);
            goalCompletionTextView.setText(formMessage.getGoalCompletionText() == null ? getResources().getString(R.string.cometchat_form_completion_message) : formMessage.getGoalCompletionText());
            if (quickViewStyle != null) {
                if (quickViewStyle.getSubtitleColor() != 0) {
                    goalCompletionTextView.setTextColor(quickViewStyle.getSubtitleColor());
                }
                if (quickViewStyle.getSubtitleAppearance() != 0) {
                    goalCompletionTextView.setTextAppearance(quickViewStyle.getSubtitleAppearance());
                }
            }
            if (!Utils.isGoalCompleted(formMessage)) {
                showForm();
                setTitle(formMessage.getTitle());
                setUiElements(formMessage.getFormFields());
                setSubmitButton(formMessage.getSubmitElement());
            } else {
                showQuickView();
            }
        }
        setFormContainerMargin(-1, -1, -1, 10);
    }

    private void saveResponse(String key, String value) {
        try {
            JSONObject elementJson = new JSONObject();
            elementJson.put("elementId", key);
            elementJson.put("value", value);
            responseJsonArray.put(elementJson);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
    }

    private void saveResponse(String key, List<String> list) {
        try {
            JSONObject elementJson = new JSONObject();
            elementJson.put("elementId", key);
            if (list != null && list.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (String value : list) {
                    jsonArray.put(value);
                }
                elementJson.put("value", jsonArray);
            }
            responseJsonArray.put(elementJson);
        } catch (Exception e) {
            CometChatLogger.e(TAG, e.toString());
        }
    }

    public interface OnSubmitClick {
        void onSubmitClick(Context context, List<BaseInputElement> baseInputElements);
    }
}
