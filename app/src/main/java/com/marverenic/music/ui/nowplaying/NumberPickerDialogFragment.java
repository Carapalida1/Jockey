package com.marverenic.music.ui.nowplaying;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.marverenic.music.R;

import java.lang.reflect.Field;

import timber.log.Timber;

public class NumberPickerDialogFragment extends DialogFragment {

    private static final String KEY_TITlE = "NumberPickerDialogFragment.TITLE";
    private static final String KEY_MESSAGE = "NumberPickerDialogFragment.MESSAGE";
    private static final String KEY_MIN_VAL = "NumberPickerDialogFragment.MIN_VALUE";
    private static final String KEY_MAX_VAL = "NumberPickerDialogFragment.MAX_VALUE";
    private static final String KEY_DEFAULT_VAL = "NumberPickerDialogFragment.DEFAULT_VALUE";
    private static final String KEY_SAVED_VAL = "NumberPickerDialogFragment.SAVED_VALUE";
    private static final String KEY_WRAP_SELECTOR = "NumberPickerDialogFragment.WRAP_SELECTOR";

    private NumberPicker mNumberPicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_number_picker, null);

        TextView messageText = contentView.findViewById(R.id.dialog_number_message);
        mNumberPicker = contentView.findViewById(R.id.dialog_number_picker);

        String title = getArguments().getString(KEY_TITlE);
        String message = getArguments().getString(KEY_MESSAGE);
        int minValue = getArguments().getInt(KEY_MIN_VAL, 0);
        int maxValue = getArguments().getInt(KEY_MAX_VAL, Integer.MAX_VALUE);
        boolean wrapSelectorWheel = getArguments().getBoolean(KEY_WRAP_SELECTOR, true);

        messageText.setText(message);
        mNumberPicker.setMinValue(minValue);
        mNumberPicker.setMaxValue(maxValue);
        mNumberPicker.setWrapSelectorWheel(wrapSelectorWheel);

        if (savedInstanceState == null) {
            int defaultValue = getArguments().getInt(KEY_DEFAULT_VAL, minValue);
            mNumberPicker.setValue(defaultValue);
        } else {
            mNumberPicker.setValue(savedInstanceState.getInt(KEY_SAVED_VAL));
        }

        setNumberPickerAccentColor(getContext(), mNumberPicker);

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(contentView)
                .setPositiveButton(R.string.action_done, (dialogInterface, i) -> onValueSelected())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private static void setNumberPickerAccentColor(Context context, NumberPicker picker) {
        TypedArray arr = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
        int accentColor = arr.getColor(0, Color.TRANSPARENT);
        arr.recycle();

        try {
            Field selectionDivider = picker.getClass().getDeclaredField("mSelectionDivider");
            selectionDivider.setAccessible(true);
            selectionDivider.set(picker, new ColorDrawable(accentColor));
        } catch (Exception exception) {
            Timber.e(exception, "Failed to set NumberPicker color");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SAVED_VAL, mNumberPicker.getValue());
    }

    private void onValueSelected() {
        int value = mNumberPicker.getValue();
        Fragment resultFragment = getTargetFragment();

        if (resultFragment != null) {
            if (resultFragment instanceof OnNumberPickedListener) {
                ((OnNumberPickedListener) resultFragment).onNumberPicked(value);
            } else {
                String targetClassName = resultFragment.getClass().getSimpleName();
                Timber.w("%s does not implement OnNumberPickedListener. Ignoring chosen value.",
                        targetClassName);
            }
        } else {
            Activity hostActivity = requireActivity();
            if (hostActivity instanceof OnNumberPickedListener) {
                ((OnNumberPickedListener) hostActivity).onNumberPicked(value);
            } else {
                String targetClassName = hostActivity.getClass().getSimpleName();
                Timber.w("%s does not implement OnNumberPickedListener. Ignoring chosen value.",
                        targetClassName);
            }
        }
    }

    public interface OnNumberPickedListener {
        void onNumberPicked(int chosen);
    }

    public static class Builder {

        private FragmentManager mFragmentManager;

        private String mTitle;
        private String mMessage;
        private Fragment mTargetFragment;
        private int mMin;
        private int mMax;
        private int mDefault;
        private boolean mWrapSelectorWheel;

        public Builder(AppCompatActivity activity) {
            mFragmentManager = activity.getSupportFragmentManager();
            mTargetFragment = null;
        }

        public Builder(Fragment fragment) {
            mFragmentManager = fragment.getFragmentManager();
            mTargetFragment = fragment;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setMinValue(int min) {
            mMin = min;
            return this;
        }

        public Builder setMaxValue(int max) {
            mMax = max;
            return this;
        }

        public Builder setDefaultValue(int value) {
            mDefault = value;
            return this;
        }

        public Builder setWrapSelectorWheel(boolean wrapSelectorWheel) {
            mWrapSelectorWheel = wrapSelectorWheel;
            return this;
        }

        public void show(String tag) {
            Bundle args = new Bundle();
            args.putString(KEY_TITlE, mTitle);
            args.putString(KEY_MESSAGE, mMessage);
            args.putInt(KEY_MIN_VAL, mMin);
            args.putInt(KEY_MAX_VAL, mMax);
            args.putInt(KEY_DEFAULT_VAL, mDefault);
            args.putBoolean(KEY_WRAP_SELECTOR, mWrapSelectorWheel);

            NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
            dialogFragment.setArguments(args);

            dialogFragment.setTargetFragment(mTargetFragment, 0);
            dialogFragment.show(mFragmentManager, tag);
        }
    }
}
