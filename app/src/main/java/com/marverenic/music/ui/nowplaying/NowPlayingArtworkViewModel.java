package com.marverenic.music.ui.nowplaying;

import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.marverenic.music.BR;
import com.marverenic.music.R;
import com.marverenic.music.data.store.PreferenceStore;
import com.marverenic.music.player.MusicPlayer;
import com.marverenic.music.player.PlayerController;
import com.marverenic.music.view.GestureView;

import java.util.List;

import rx.Observable;
import timber.log.Timber;

public class NowPlayingArtworkViewModel extends BaseObservable {

    private PlayerController mPlayerController;
    private PreferenceStore mPrefStore;

    private Context mContext;
    private Bitmap mArtwork;
    private boolean mPlaying;

    public NowPlayingArtworkViewModel(Context context, PlayerController playerController,
                                       PreferenceStore prefStore) {
        mContext = context;
        mPrefStore = prefStore;
        mPlayerController = playerController;
    }

    public void setPlaying(boolean playing) {
        mPlaying = playing;
        notifyPropertyChanged(BR.tapIndicator);
    }

    public int getPortraitArtworkHeight() {
        // Only used when in portrait orientation
        int reservedHeight = (int) mContext.getResources().getDimension(R.dimen.player_frame_peek);

        // Default to a square view, so set the height equal to the width
        //noinspection SuspiciousNameCombination
        int preferredHeight = mContext.getResources().getDisplayMetrics().widthPixels;
        int maxHeight = mContext.getResources().getDisplayMetrics().heightPixels - reservedHeight;

        return Math.min(preferredHeight, maxHeight);
    }

    public void setArtwork(Bitmap artwork) {
        mArtwork = artwork;
        notifyPropertyChanged(BR.nowPlayingArtwork);
    }

    @Bindable
    public Drawable getNowPlayingArtwork() {
        if (mArtwork == null) {
            return ContextCompat.getDrawable(mContext, R.drawable.art_default_xl);
        } else {
            return new BitmapDrawable(mContext.getResources(), mArtwork);
        }
    }

    @Bindable
    public boolean getGesturesEnabled() {
        return mPrefStore.enableNowPlayingGestures();
    }

    @Bindable
    public Drawable getTapIndicator() {
        return ContextCompat.getDrawable(mContext,
                mPlaying
                        ? R.drawable.ic_pause_36dp
                        : R.drawable.ic_play_arrow_36dp);
    }

    public GestureView.OnGestureListener getGestureListener() {
        return new GestureView.OnGestureListener() {
            @Override
            public void onLeftSwipe() {
                mPlayerController.skip();
            }

            @Override
            public void onRightSwipe() {
                mPlayerController.getQueuePosition()
                        .take(1)
                        .flatMap((queuePosition) -> {
                            // Wrap to end of the queue when repeat all is enabled
                            if (queuePosition == 0
                                    && mPrefStore.getRepeatMode() == MusicPlayer.REPEAT_ALL) {
                                return mPlayerController.getQueue()
                                        .take(1)
                                        .map(List::size)
                                        .map(size -> size - 1);
                            } else {
                                return Observable.just(queuePosition - 1);
                            }
                        })
                        .subscribe((queuePosition) -> {
                            if (queuePosition >= 0) {
                                mPlayerController.changeSong(queuePosition);
                            } else {
                                mPlayerController.seek(0);
                            }
                        }, throwable -> {
                            Timber.e(throwable, "Failed to handle skip gesture");
                        });
            }

            @Override
            public void onTap() {
                mPlayerController.togglePlay();
                notifyPropertyChanged(BR.tapIndicator);
            }
        };
    }

}
