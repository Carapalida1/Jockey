package com.marverenic.music.ui.library.playlist.contents;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.Bindable;
import android.graphics.drawable.NinePatchDrawable;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.view.View;

import com.marverenic.adapter.DragDropAdapter;
import com.marverenic.adapter.DragDropDecoration;
import com.marverenic.music.R;
import com.marverenic.music.data.store.MusicStore;
import com.marverenic.music.data.store.PlaylistStore;
import com.marverenic.music.data.store.PreferenceStore;
import com.marverenic.music.model.AutoPlaylist;
import com.marverenic.music.model.Playlist;
import com.marverenic.music.model.Song;
import com.marverenic.music.player.PlayerController;
import com.marverenic.music.ui.BaseViewModel;
import com.marverenic.music.ui.common.LibraryEmptyState;
import com.marverenic.music.ui.common.OnSongSelectedListener;
import com.marverenic.music.ui.common.ShuffleAllSection;
import com.marverenic.music.ui.library.playlist.contents.edit.AutoPlaylistEditActivity;
import com.marverenic.music.view.DragBackgroundDecoration;
import com.marverenic.music.view.DragDividerDecoration;

import java.util.Collections;
import java.util.List;

public class PlaylistViewModel extends BaseViewModel {

    private FragmentManager mFragmentManager;

    private PlayerController mPlayerController;
    private MusicStore mMusicStore;
    private PlaylistStore mPlaylistStore;
    private PreferenceStore mPreferenceStore;

    private Playlist mPlaylist;

    private DragDropAdapter mAdapter;
    private PlaylistSongSection mSongSection;
    private ShuffleAllSection mShuffleAllSection;

    public PlaylistViewModel(Context context, FragmentManager fragmentManager,
                             PlayerController playerController, MusicStore musicStore,
                             PlaylistStore playlistStore, PreferenceStore preferenceStore,
                             Playlist playlist, @Nullable OnSongSelectedListener songSelectedListener) {
        super(context);
        mFragmentManager = fragmentManager;
        mPlayerController = playerController;
        mMusicStore = musicStore;
        mPlaylistStore = playlistStore;
        mPreferenceStore = preferenceStore;
        mPlaylist = playlist;

        createAdapter(songSelectedListener);
    }

    private void createAdapter(@Nullable OnSongSelectedListener songSelectedListener) {
        mAdapter = new DragDropAdapter();
        mAdapter.setHasStableIds(true);

        mSongSection = new PlaylistSongSection(Collections.emptyList(), mPlaylist, mFragmentManager,
                mMusicStore, mPlaylistStore, mPlayerController, songSelectedListener);
        mShuffleAllSection = new ShuffleAllSection(Collections.emptyList(), mPreferenceStore,
                mPlayerController, songSelectedListener);
        mAdapter.addSection(mShuffleAllSection);
        mAdapter.setDragSection(mSongSection);

        mAdapter.setEmptyState(new LibraryEmptyState(getContext(), mMusicStore, mPlaylistStore) {
            @Override
            public String getEmptyMessage() {
                if (mPlaylist instanceof AutoPlaylist) {
                    return getString(R.string.empty_auto_playlist);
                } else {
                    return getString(R.string.empty_playlist);
                }
            }

            @Override
            public String getEmptyMessageDetail() {
                if (mPlaylist instanceof AutoPlaylist) {
                    return getString(R.string.empty_auto_playlist_detail);
                } else {
                    return getString(R.string.empty_playlist_detail);
                }
            }

            @Override
            public String getEmptyAction1Label() {
                if (mPlaylist instanceof AutoPlaylist) {
                    return getString(R.string.action_edit_playlist_rules);
                } else {
                    return "";
                }
            }

            @Override
            public void onAction1(View button) {
                if (mPlaylist instanceof AutoPlaylist) {
                    AutoPlaylist playlist = (AutoPlaylist) mPlaylist;
                    Intent intent = AutoPlaylistEditActivity.newIntent(getContext(), playlist);

                    getContext().startActivity(intent);
                }
            }
        });
    }

    public void setCurrentSong(Song nowPlaying) {
        mSongSection.setCurrentSong(nowPlaying);
    }

    public void setSongs(List<Song> playlistSongs) {
        mShuffleAllSection.setData(playlistSongs);
        mSongSection.setData(playlistSongs);
        mAdapter.notifyDataSetChanged();
    }

    @Bindable
    public DragDropAdapter getAdapter() {
        return mAdapter;
    }

    @Bindable
    public ItemDecoration[] getItemDecorations() {
        NinePatchDrawable dragShadow = (NinePatchDrawable) ContextCompat.getDrawable(
                getContext(), R.drawable.list_drag_shadow);

        return new ItemDecoration[] {
                new DragBackgroundDecoration(R.id.song_drag_root),
                new DragDividerDecoration(R.id.song_drag_root, getContext(), R.id.empty_layout),
                new DragDropDecoration(dragShadow)
        };
    }

}
