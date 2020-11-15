package com.marverenic.music.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.media.session.MediaButtonReceiver;
import android.widget.RemoteViews;

import com.marverenic.music.JockeyApplication;
import com.marverenic.music.player.MusicPlayer;
import com.marverenic.music.player.PlayerController;

import javax.inject.Inject;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

public abstract class BaseWidget extends AppWidgetProvider {

    @Inject PlayerController mPlayerController;

    @Override
    public final void onReceive(Context context, Intent intent) {
        JockeyApplication.getComponent(context).inject(this);

        String action = intent.getAction();
        if (MusicPlayer.UPDATE_BROADCAST.equals(action)) {
            if (isEnabled(context)) {
                onUpdate(context);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public final void onUpdate(Context context, AppWidgetManager widgetManager, int[] widgetIds) {
        if (isEnabled(context)) {
            onUpdate(context);
        }
    }

    /**
     * Called when a broadcast has been sent to trigger an update of the widget's remote view. This
     * method is responsible for creating an updated widget view and applying it either manually
     * or with {@link #updateAllInstances(Context, RemoteViews)}.
     * @param context A {@code Context} that can be used to create RemoteViews and update the widget
     */
    protected abstract void onUpdate(Context context);

    protected ComponentName getComponentName(Context context) {
        return new ComponentName(context.getPackageName(), getClass().getCanonicalName());
    }

    protected boolean isEnabled(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(getComponentName(context));

        return ids != null && ids.length > 0;
    }

    protected void updateAllInstances(Context context, RemoteViews delta) {
        AppWidgetManager wm = AppWidgetManager.getInstance(context);
        wm.updateAppWidget(getComponentName(context), delta);
    }

    protected PendingIntent getSkipNextIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_NEXT);
    }

    protected PendingIntent getSkipPreviousIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_PREVIOUS);
    }

    protected PendingIntent getPlayPauseIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PLAY_PAUSE);
    }
}
