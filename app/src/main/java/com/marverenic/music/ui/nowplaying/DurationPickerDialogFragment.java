package com.marverenic.music.ui.nowplaying;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.marverenic.music.R;
import com.triggertrap.seekarc.SeekArc;

import timber.log.Timber;

public class DurationPickerDialogFragment extends DialogFragment
        implements SeekArc.OnSeekArcChangeListener {

    private static final String KEY_TITLE = "DurationPickerDialogFragment.TITLE";
    private static final String KEY_MIN_VAL = "DurationPickerDialogFragment.MIN_VALUE";
    private static final String KEY_MAX_VAL = "DurationPickerDialogFragment.MAX_VALUE";
    private static final String KEY_DEFAULT_VAL = "DurationPickerDialogFragment.DEFAULT_VALUE";
    private static final String KEY_DISABLE_BUTTON = "DurationPickerDialogFragment.DISABLE_BUTTON";
    private static final String KEY_SAVED_VAL = "DurationPickerDialogFragment.SAVED_VALUE";

    public static final int NO_VALUE = Integer.MIN_VALUE;

    private SeekArc mSlider;
    private TextView mLabel;

    private int mMinValue;
    private int mOffsetValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMinValue = getArguments().getInt(KEY_MIN_VAL);
        if (savedInstanceState == null) {
            mOffsetValue = getArguments().getInt(KEY_DEFAULT_VAL);
        } else {
            mOffsetValue = savedInstanceState.getInt(KEY_SAVED_VAL);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SAVED_VAL, mOffsetValue);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_duration_picker, null);

        mSlider = contentView.findViewById(R.id.duration_picker_slider);
        mLabel = contentView.findViewById(R.id.duration_picker_time);

        String title = getArguments().getString(KEY_TITLE);
        String disableButton = getArguments().getString(KEY_DISABLE_BUTTON);
        int maxValue = getArguments().getInt(KEY_MAX_VAL, Integer.MAX_VALUE);

        mSlider.setOnSeekArcChangeListener(this);
        mSlider.setMax(maxValue - mMinValue);
        mSlider.setProgress(mOffsetValue - mMinValue);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(contentView)
                .setPositiveButton(R.string.action_done, (dialogInterface, i) -> {
                    onValueSelected(mSlider.getProgress() + mMinValue);
                })
                .setNegativeButton(R.string.action_cancel, null);

        if (disableButton != null) {
            dialogBuilder.setNeutralButton(disableButton, (dialogInterface, i) -> {
                onValueSelected(NO_VALUE);
            });
        }

        return dialogBuilder.show();
    }

    private void onValueSelected(int value) {
        Fragment resultFragment = getTargetFragment();

        if (resultFragment != null) {
            if (resultFragment instanceof OnDurationPickedListener) {
                ((OnDurationPickedListener) resultFragment).onDurationPicked(value);
            } else {
                String targetClassName = resultFragment.getClass().getSimpleName();
                Timber.w("%s does not implement OnDurationPickedListener. Ignoring chosen value.",
                        targetClassName);
            }
        } else {
            Activity hostActivity = requireActivity();
            if (hostActivity instanceof OnDurationPickedListener) {
                ((OnDurationPickedListener) hostActivity).onDurationPicked(value);
            } else {
                String targetClassName = hostActivity.getClass().getSimpleName();
                Timber.w("%s does not implement OnDurationPickedListener. Ignoring chosen value.",
                        targetClassName);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
        mOffsetValue = progress + mMinValue;
        mLabel.setText(getString(R.string.time_in_min_format, mOffsetValue));
    }

    @Override
    public void onStartTrackingTouch(SeekArc seekArc) {

    }

    @Override
    public void onStopTrackingTouch(SeekArc seekArc) {

    }

    public interface OnDurationPickedListener {
        void onDurationPicked(int durationInMinutes);
    }

    public static class Builder {

        private FragmentManager mFragmentManager;

        private String mTitle;
        private Fragment mTargetFragment;
        private int mMin;
        private int mMax;
        private int mDefault;
        private String mDisableButton;

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

        public Builder setDisableButtonText(String disableButtonText) {
            mDisableButton = disableButtonText;
            return this;
        }

        public void show(String tag) {
            Bundle args = new Bundle();
            args.putString(KEY_TITLE, mTitle);
            args.putInt(KEY_MIN_VAL, mMin);
            args.putInt(KEY_MAX_VAL, mMax);
            args.putString(KEY_DISABLE_BUTTON, mDisableButton);
            args.putInt(KEY_DEFAULT_VAL, mDefault);

            DurationPickerDialogFragment dialogFragment = new DurationPickerDialogFragment();
            dialogFragment.setArguments(args);

            dialogFragment.setTargetFragment(mTargetFragment, 0);
            dialogFragment.show(mFragmentManager, tag);
        }
    }
}
