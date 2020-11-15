package com.marverenic.music.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.widget.RemoteViews;

import com.marverenic.music.R;
import com.marverenic.music.model.Song;
import com.marverenic.music.player.PlayerController;
import com.marverenic.music.ui.library.LibraryActivity;

import rx.Observable;
import timber.log.Timber;

public class SquareWidget extends BaseWidget {

    @Override
    protected void onUpdate(Context context) {
        PlayerController.Binding binding = mPlayerController.bind();
        Observable.just(createBaseView(context))
                .flatMap(views -> mPlayerController.getNowPlaying().take(1)
                        .map(song -> setSong(context, views, song)))
                .flatMap(views -> mPlayerController.isPlaying().take(1)
                        .map(isPlaying -> setPlaying(views, isPlaying)))
                .flatMap(views -> mPlayerController.getArtwork().take(1)
                        .map(artwork -> setArtwork(views, artwork)))
                .subscribe(views -> updateAllInstances(context, views),
                        throwable -> Timber.e(throwable, "Failed to update widget"),
                        () -> mPlayerController.unbind(binding));
    }

    private RemoteViews createBaseView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_square);

        Intent launcherIntent = LibraryActivity.newNowPlayingIntent(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launcherIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_square_container, pendingIntent);

        views.setOnClickPendingIntent(R.id.widget_next, getSkipNextIntent(context));
        views.setOnClickPendingIntent(R.id.widget_play_pause, getPlayPauseIntent(context));
        views.setOnClickPendingIntent(R.id.widget_previous, getSkipPreviousIntent(context));

        return views;
    }

    private static RemoteViews setSong(Context context, RemoteViews views, @Nullable Song song) {
        if (song == null) {
            String defaultSongName = context.getString(R.string.nothing_playing);

            views.setTextViewText(R.id.widget_square_title, defaultSongName);
            views.setTextViewText(R.id.widget_square_artist, "");
            views.setTextViewText(R.id.widget_square_album, "");
        } else {
            views.setTextViewText(R.id.widget_square_title, song.getSongName());
            views.setTextViewText(R.id.widget_square_artist, song.getArtistName());
            views.setTextViewText(R.id.widget_square_album, song.getAlbumName());
        }

        return views;
    }

    private static RemoteViews setPlaying(RemoteViews views, boolean isPlaying) {
        views.setImageViewResource(R.id.widget_play_pause,
                (isPlaying)
                        ? R.drawable.ic_pause_36dp
                        : R.drawable.ic_play_arrow_36dp);

        return views;
    }

    private static RemoteViews setArtwork(RemoteViews views, @Nullable Bitmap artwork) {
        if (artwork == null) {
            views.setImageViewResource(R.id.widget_square_artwork, R.drawable.art_default_xl);
        } else {
            views.setImageViewBitmap(R.id.widget_square_artwork, artwork);
        }

        return views;
    }
}
