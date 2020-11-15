package com.marverenic.music.ui.library;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.marverenic.music.JockeyApplication;
import com.marverenic.music.R;
import com.marverenic.music.data.store.MusicStore;
import com.marverenic.music.data.store.PlaylistStore;
import com.marverenic.music.data.store.PreferenceStore;
import com.marverenic.music.data.store.ThemeStore;
import com.marverenic.music.databinding.FragmentLibraryBinding;
import com.marverenic.music.ui.BaseFragment;
import com.marverenic.music.ui.search.SearchActivity;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class LibraryFragment extends BaseFragment {

    private static final String KEY_SAVED_PAGE = "LibraryFragment.Page";

    @Inject MusicStore mMusicStore;
    @Inject PlaylistStore mPlaylistStore;
    @Inject ThemeStore mThemeStore;
    @Inject PreferenceStore mPrefStore;

    private FragmentLibraryBinding mBinding;
    private LibraryViewModel mViewModel;

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
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

        mBinding = FragmentLibraryBinding.inflate(inflater, container, false);
        mViewModel = new LibraryViewModel(getContext(), getFragmentManager(), mPrefStore, mThemeStore);
        mBinding.setViewModel(mViewModel);

        if (savedInstanceState != null) {
            mViewModel.setPage(savedInstanceState.getInt(KEY_SAVED_PAGE, mViewModel.getPage()));
        }

        Observable.combineLatest(mMusicStore.isLoading(), mPlaylistStore.isLoading(),
                (musicLoading, playlistLoading) -> {
                    return musicLoading || playlistLoading;
                })
                .compose(bindToLifecycle())
                .subscribe(mViewModel::setLibraryRefreshing, throwable -> {
                    Timber.e(throwable, "Failed to update refresh indicator");
                });

        mMusicStore.loadAll();
        mPlaylistStore.loadPlaylists();

        ViewPager pager = mBinding.libraryPager;
        AppBarLayout appBarLayout = mBinding.libraryAppBarLayout;

        appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) ->
                pager.setPadding(pager.getPaddingLeft(),
                        pager.getPaddingTop(),
                        pager.getPaddingRight(),
                        layout.getTotalScrollRange() + verticalOffset));

        mBinding.libraryTabs.setupWithViewPager(mBinding.libraryPager);

        setupToolbar(mBinding.toolbar);
        setHasOptionsMenu(true);

        return mBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SAVED_PAGE, mViewModel.getPage());
    }

    private void setupToolbar(Toolbar toolbar) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_library, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_library_search:
                startActivity(SearchActivity.newIntent(getContext()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean onBackPressed() {
        return super.onBackPressed();
    }
}
