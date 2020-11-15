package com.marverenic.music.ui.library.playlist.contents;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.PopupMenu;
import android.view.Gravity;
import android.view.View;

import com.marverenic.music.R;
import com.marverenic.music.data.store.MusicStore;
import com.marverenic.music.data.store.PlaylistStore;
import com.marverenic.music.model.Song;
import com.marverenic.music.player.PlayerController;
import com.marverenic.music.ui.browse.MusicBrowserActivity;
import com.marverenic.music.ui.common.OnSongSelectedListener;
import com.marverenic.music.ui.library.album.contents.AlbumActivity;
import com.marverenic.music.ui.library.artist.contents.ArtistActivity;
import com.marverenic.music.ui.library.song.SongItemViewModel;

import timber.log.Timber;

public class PlaylistSongItemViewModel extends SongItemViewModel {

    private MusicStore mMusicStore;
    private PlayerController mPlayerController;

    private OnPlaylistEntriesChangeListener mRemoveListener;

    public PlaylistSongItemViewModel(Context context, FragmentManager fragmentManager,
                                     MusicStore musicStore, PlaylistStore playlistStore,
                                     PlayerController playerController,
                                     OnPlaylistEntriesChangeListener listener,
                                     @Nullable OnSongSelectedListener songSelectedListener) {

        super(context, fragmentManager, musicStore, playlistStore, playerController, songSelectedListener);
        mMusicStore = musicStore;
        mPlayerController = playerController;

        mRemoveListener = listener;
    }

    public interface OnPlaylistEntriesChangeListener {
        void onPlaylistEntriesChange();
    }

    @Override
    public View.OnClickListener onClickMenu() {
        return v -> {
            final PopupMenu menu = new PopupMenu(getContext(), v, Gravity.END);
            menu.inflate(R.menu.instance_song_playlist);
            menu.setOnMenuItemClickListener(onMenuItemClick(v));
            menu.show();
        };
    }

    private PopupMenu.OnMenuItemClickListener onMenuItemClick(View view) {
        return menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_queue_item_next:
                    mPlayerController.queueNext(getReference());
                    return true;
                case R.id.menu_item_queue_item_last:
                    mPlayerController.queueLast(getReference());
                    return true;
                case R.id.menu_item_navigate_to_artist:
                    mMusicStore.findArtistById(getReference().getArtistId()).subscribe(
                            artist -> {
                                startActivity(ArtistActivity.newIntent(getContext(), artist));
                            }, throwable -> {
                                Timber.e(throwable, "Failed to find artist");
                            });

                    return true;
                case R.id.menu_item_navigate_to_album:
                    mMusicStore.findAlbumById(getReference().getAlbumId()).subscribe(
                            album -> {
                                startActivity(AlbumActivity.newIntent(getContext(), album));
                            }, throwable -> {
                                Timber.e(throwable, "Failed to find album");
                            });

                    return true;
                case R.id.menu_item_navigate_to_folder:
                    getContext().startActivity(MusicBrowserActivity.newIntent(getContext(), getReference()));
                    return true;
                case R.id.menu_item_remove:
                    removeFromPlaylist(view);
                    return true;
            }
            return false;
        };
    }

    private void removeFromPlaylist(View snackbarContainer) {
        Song removed = getReference();
        int removedIndex = getIndex();

        getSongs().remove(getIndex());
        mRemoveListener.onPlaylistEntriesChange();

        String songName = removed.getSongName();
        String message = getString(R.string.message_removed_song, songName);

        Snackbar.make(snackbarContainer, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, view -> {
                    getSongs().add(removedIndex, removed);
                    mRemoveListener.onPlaylistEntriesChange();
                }).show();
    }
}
