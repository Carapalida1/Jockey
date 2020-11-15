package com.marverenic.music.ui.library.recentlyadded;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marverenic.music.JockeyApplication;
import com.marverenic.music.R;
import com.marverenic.music.data.store.MusicStore;
import com.marverenic.music.data.store.PlaylistStore;
import com.marverenic.music.data.store.PreferenceStore;
import com.marverenic.music.databinding.FragmentRecentlyAddedBinding;
import com.marverenic.music.model.Song;
import com.marverenic.music.player.PlayerController;
import com.marverenic.music.ui.BaseFragment;
import com.marverenic.music.ui.common.OnSongSelectedListener;

import java.util.Collections;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class RecentlyAddedFragment extends BaseFragment {

    private static final String KEY_SAVED_LIST_STATE = "RecentlyAddedFragment.RecyclerViewState";

    private static final long RECENT_THRESHOLD_SEC = 30 * 24 * 60 * 60; // 30 days

    @Inject PlayerController mPlayerController;
    @Inject MusicStore mMusicStore;
    @Inject PlaylistStore mPlaylistStore;
    @Inject PreferenceStore mPreferenceStore;

    private FragmentRecentlyAddedBinding mBinding;
    private RecentlyAddedViewModel mViewModel;

    public static RecentlyAddedFragment newInstance() {
        return new RecentlyAddedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JockeyApplication.getComponent(this).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recently_added,
                container, false);

        mViewModel = new RecentlyAddedViewModel(getContext(), mPlayerController, mMusicStore,
                mPlaylistStore, getFragmentManager(),
                OnSongSelectedListener.defaultImplementation(getActivity(), mPreferenceStore));

        mBinding.setViewModel(mViewModel);
        mBinding.executePendingBindings();
        setupToolbar(mBinding.toolbar);

        if (savedInstanceState != null) {
            mBinding.recentlyAddedRecyclerView.getLayoutManager()
                    .onRestoreInstanceState(savedInstanceState.getParcelable(KEY_SAVED_LIST_STATE));
        }

        mMusicStore.getSongs()
                .flatMap(allSongs -> {
                    return Observable.from(allSongs)
                            .filter(song -> {
                                long dT = System.currentTimeMillis() / 1000 - song.getDateAdded();
                                return dT < RECENT_THRESHOLD_SEC;
                            })
                            .toList();
                })
                .map(recentlyAdded -> {
                    Collections.sort(recentlyAdded, (s1, s2) -> {
                        int dateCmp = Song.DATE_ADDED_COMPARATOR.compare(s1, s2);
                        return dateCmp == 0 ? s1.compareTo(s2) : dateCmp;
                    });
                    return recentlyAdded;
                })
                .subscribe(recentlyAdded -> {
                    mViewModel.setSongs(recentlyAdded);
                }, throwable -> {
                    Timber.e(throwable, "Failed to update recently added items");
                });

        mPlayerController.getNowPlaying()
                .subscribe(mViewModel::setCurrentlyPlaying, throwable -> {
                    Timber.e(throwable, "Failed to update currently playing song");
                });

        return mBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SAVED_LIST_STATE,
                mBinding.recentlyAddedRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    private void setupToolbar(Toolbar toolbar) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }
    }
}
