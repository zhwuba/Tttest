package com.freeme.themeclub;

import com.freeme.themeclub.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class NavigationControlView extends RadioGroup {

    private int mSdk;
    private Context mCtx;

    private OnSelectionChangedListener mListener;

    private int selectedColor = Color.parseColor("#0099CC");
    private int unselectedColor = Color.TRANSPARENT;
    private int unselectedTextColor = Color.parseColor("#0099CC");
    private int defaultSelection = -1;
    private boolean stretch = false;
    private int selectedTextColor = Color.WHITE;
    private boolean equalWidth = false;
    private String identifier = "";
    private ColorStateList textColorStateList;

    private LinkedHashMap<String, String> itemMap = new LinkedHashMap<String, String>();
    private ArrayList<RadioButton> options;

    public NavigationControlView(Context context) {
        super(context, null);
        init(context);
        update();
    }

    public NavigationControlView(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);

        init(context);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MultipleSelectionButton,
                0, 0);

        try {

            selectedColor = attributes.getColor(R.styleable.MultipleSelectionButton_ascv_selectedColor, selectedColor);
            selectedTextColor = attributes.getColor(R.styleable.MultipleSelectionButton_ascv_selectedTextColor, selectedTextColor);
            unselectedColor = attributes.getColor(R.styleable.MultipleSelectionButton_ascv_unselectedColor, unselectedColor);
            unselectedTextColor = attributes.getColor(R.styleable.MultipleSelectionButton_ascv_unselectedTextColor, selectedColor);

            textColorStateList = new ColorStateList(new int[][]{
                    {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[]{unselectedTextColor, selectedTextColor}
                    );

            defaultSelection = attributes.getInt(R.styleable.MultipleSelectionButton_ascv_defaultSelection, defaultSelection);
            equalWidth = attributes.getBoolean(R.styleable.MultipleSelectionButton_ascv_equalWidth, equalWidth);
            stretch = attributes.getBoolean(R.styleable.MultipleSelectionButton_ascv_stretch, stretch);
            identifier = attributes.getString(R.styleable.MultipleSelectionButton_ascv_identifier);

            CharSequence[] itemArray = attributes.getTextArray(R.styleable.MultipleSelectionButton_ascv_items);
            CharSequence[] valueArray = attributes.getTextArray(R.styleable.MultipleSelectionButton_ascv_values);

            if (this.isInEditMode()) {
                itemArray = new CharSequence[]{"YES", "NO", "MAYBE", "DON'T KNOW"};
            }

            if (itemArray != null && valueArray != null) {
                if (itemArray.length != valueArray.length) {
                    throw new Exception("Item labels and value arrays must be the same size");
                }
            }

            if (itemArray != null) {

                if (valueArray != null) {
                    for (int i = 0; i < itemArray.length; i++) {
                        itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                    }

                } else {

                    for (CharSequence item : itemArray) {
                        itemMap.put(item.toString(), item.toString());
                    }

                }

            }

        } finally {
            attributes.recycle();
        }

        update();
    }

    private void init(Context context) {
        mCtx = context;
        //Needed for calling the right "setbackground" method
        mSdk = android.os.Build.VERSION.SDK_INT;
        //Provide a default bit of padding for the view
//        this.setPadding(10, 10, 10, 10);
    }

    @TargetApi(16)
    private void update() {

        this.removeAllViews();

        int twoDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

        this.setOrientation(RadioGroup.HORIZONTAL);

        float textWidth = 0;
        options = new ArrayList<RadioButton>();

        Iterator<Map.Entry<String, String>> itemIterator = (itemMap.entrySet().iterator());
        int i = 0;
        while (itemIterator.hasNext()) {

            Map.Entry<String, String> item = itemIterator.next();
            RadioButton rb = new RadioButton(mCtx);
            rb.setTextColor(textColorStateList);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (stretch) {
                params.weight = 1.0f;
            }
//            if (i > 0) {
//                params.setMargins(-twoDP, 0, 0, 0);
//            }

            rb.setLayoutParams(params);


            //Clear out button drawable (text only)
            rb.setButtonDrawable(new StateListDrawable());

//            //Create state list for background
//            if (i == 0) {
//                //Left
//                GradientDrawable leftUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.left_option).mutate();
//                leftUnselected.setStroke(twoDP, selectedColor);
//
//                leftUnselected.setColor(unselectedColor);
//                GradientDrawable leftSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.left_option_selected).mutate();
//                leftSelected.setColor(selectedColor);
//                leftSelected.setStroke(twoDP, selectedColor);
//
//                StateListDrawable leftStateListDrawable = new StateListDrawable();
//                leftStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, leftUnselected);
//                leftStateListDrawable.addState(new int[]{android.R.attr.state_checked}, leftSelected);
//                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
//                    rb.setBackgroundDrawable(leftStateListDrawable);
//                } else {
//                    rb.setBackground(leftStateListDrawable);
//                }
//
//
//            } else if (i == (itemMap.size() - 1)) {
//                //Right
//                GradientDrawable rightUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.right_option).mutate();
//                rightUnselected.setStroke(twoDP, selectedColor);
//
//                rightUnselected.setColor(unselectedColor);
//                GradientDrawable rightSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.right_option_selected).mutate();
//                rightSelected.setColor(selectedColor);
//                rightSelected.setStroke(twoDP, selectedColor);
//
//                StateListDrawable rightStateListDrawable = new StateListDrawable();
//                rightStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, rightUnselected);
//                rightStateListDrawable.addState(new int[]{android.R.attr.state_checked}, rightSelected);
//                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
//                    rb.setBackgroundDrawable(rightStateListDrawable);
//                } else {
//                    rb.setBackground(rightStateListDrawable);
//                }
//
//            } else {
//                //Middle
//                GradientDrawable middleUnselected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.middle_option).mutate();
//                middleUnselected.setStroke(twoDP, selectedColor);
//                middleUnselected.setDither(true);
//                middleUnselected.setColor(unselectedColor);
//                GradientDrawable middleSelected = (GradientDrawable) mCtx.getResources().getDrawable(R.drawable.middle_option_selected).mutate();
//                middleSelected.setColor(selectedColor);
//                middleSelected.setStroke(twoDP, selectedColor);
//
//                StateListDrawable middleStateListDrawable = new StateListDrawable();
//                middleStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, middleUnselected);
//                middleStateListDrawable.addState(new int[]{android.R.attr.state_checked}, middleSelected);
//                if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
//                    rb.setBackgroundDrawable(middleStateListDrawable);
//                } else {
//                    rb.setBackground(middleStateListDrawable);
//                }
//
//            }
            
            Drawable unselectedBackground = mCtx.getResources().getDrawable(R.drawable.navigation_unselected_background);
            Drawable selectedBackground = mCtx.getResources().getDrawable(R.drawable.navigation_selected_background);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, unselectedBackground);
            stateListDrawable.addState(new int[]{android.R.attr.state_checked}, selectedBackground);
            if (mSdk < Build.VERSION_CODES.JELLY_BEAN) {
                rb.setBackgroundDrawable(stateListDrawable);
            } else {
                rb.setBackground(stateListDrawable);
            }

            rb.setLayoutParams(params);
            rb.setMinWidth(twoDP * 10);
            rb.setGravity(Gravity.CENTER);
            rb.setTextSize(16.67f);
            rb.setText(item.getKey());
            textWidth = Math.max(rb.getPaint().measureText(item.getKey()), textWidth);
            options.add(rb);

            i++;
        }

        //We do this to make all the segments the same width
        for (RadioButton option : options) {
            if (equalWidth) {
                option.setWidth((int) (textWidth + (twoDP * 20)));
            }
            this.addView(option);
        }

        this.setOnCheckedChangeListener(selectionChangedlistener);

        if (defaultSelection > -1) {
            this.check(((RadioButton) getChildAt(defaultSelection)).getId());
        }
    }

    public String[] getCheckedWithIdentifier() {
        return new String[]{identifier, itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString())};
    }

    public String getChecked() {
        return itemMap.get(((RadioButton) this.findViewById(this.getCheckedRadioButtonId())).getText().toString());
    }

    private OnCheckedChangeListener selectionChangedlistener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mListener != null) {
                mListener.newSelection(identifier, itemMap.get(((RadioButton) group.findViewById(checkedId)).getText().toString()));
            }

        }
    };

    public interface OnSelectionChangedListener {
        public void newSelection(String identifier, String value);
    }

    public void setItems(String[] itemArray, String[] valueArray) throws Exception {

        itemMap.clear();

        if (itemArray != null && valueArray != null) {
            if (itemArray.length != valueArray.length) {
                throw new Exception("Item labels and value arrays must be the same size");
            }
        }

        if (itemArray != null) {

            if (valueArray != null) {
                for (int i = 0; i < itemArray.length; i++) {
                    itemMap.put(itemArray[i].toString(), valueArray[i].toString());
                }

            } else {

                for (CharSequence item : itemArray) {
                    itemMap.put(item.toString(), item.toString());
                }

            }


        }

        update();
    }

    public void setItems(String[] items, String[] values, int defaultSelection) throws Exception {

        if (defaultSelection > (items.length - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            setItems(items, values);
        }
    }

    public void setDefaultSelection(int defaultSelection) throws Exception {
        if (defaultSelection > (itemMap.size() - 1)) {
            throw new Exception("Default selection cannot be greater than the number of items");
        } else {
            this.defaultSelection = defaultSelection;
            update();
        }
    }

    public void setColors(int primaryColor, int secondaryColor) {
        this.selectedColor = primaryColor;
        this.selectedTextColor = secondaryColor;
        this.unselectedColor = secondaryColor;
        this.unselectedTextColor = primaryColor;

        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
                );

        update();
    }

    public void setColors(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor) {
        this.selectedColor = selectedColor;
        this.selectedTextColor = selectedTextColor;
        this.unselectedColor = unselectedColor;
        this.unselectedTextColor = unselectedTextColor;

        textColorStateList = new ColorStateList(new int[][]{
                {-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                new int[]{unselectedTextColor, selectedTextColor}
                );

        update();
    }

    /**
     * Used to set the selected value based on the value (not the visible text) provided in the
     * value array provided via xml or code
     */
    public void setByValue(String value) {
        String buttonText = "";
        if (this.itemMap.containsValue(value)) {
            for (String entry : itemMap.keySet()) {
                if (itemMap.get(entry).equalsIgnoreCase(value)) {
                    buttonText = entry;
                }
            }
        }

        for (RadioButton option : options) {
            if (option.getText().toString().equalsIgnoreCase(buttonText)) {
                this.check(option.getId());
            }
        }

    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mListener = listener;
    }

    /**
     * For use with multiple views. This identifier will be provided in the
     * onSelectionChanged mListener
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEqualWidth(boolean equalWidth) {
        this.equalWidth = equalWidth;
        update();
    }

    public void setStretch(boolean stretch) {
        this.stretch = stretch;
        update();
    }

}
