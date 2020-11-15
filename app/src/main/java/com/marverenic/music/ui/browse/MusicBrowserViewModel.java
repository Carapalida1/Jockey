package com.marverenic.music.ui.browse;

import android.content.Context;
import androidx.databinding.Bindable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marverenic.adapter.HeterogeneousAdapter;
import com.marverenic.music.BR;
import com.marverenic.music.R;
import com.marverenic.music.data.store.MediaStoreUtil;
import com.marverenic.music.ui.BaseViewModel;
import com.marverenic.music.ui.browse.BreadCrumbView.BreadCrumb;
import com.marverenic.music.utils.Util;
import com.marverenic.music.view.BackgroundDecoration;
import com.marverenic.music.view.PaddingDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class MusicBrowserViewModel extends BaseViewModel {

    private Stack<File> mHistory;
    private File mCurrentDirectory;

    private ThumbnailLoader mThumbnailLoader;
    private HeterogeneousAdapter mAdapter;
    private FolderSection mFolderSection;
    private FileSection mFileSection;
    private OnSongFileSelectedListener mSelectionListener;

    private List<File> mBreadCrumbs;
    private File mSelectedBreadCrumb;

    private BehaviorSubject<File> mDirectoryObservable;

    public MusicBrowserViewModel(Context context, File startingDirectory,
                                 @NonNull OnSongFileSelectedListener songSelectionListener) {
        super(context);
        mSelectionListener = songSelectionListener;
        mHistory = new Stack<>();
        mBreadCrumbs = Collections.emptyList();
        mDirectoryObservable = BehaviorSubject.create(startingDirectory);

        mAdapter = new HeterogeneousAdapter();
        mFolderSection = new FolderSection(Collections.emptyList(), this::onClickFolder);
        mThumbnailLoader = new ThumbnailLoader(context);
        mFileSection = new FileSection(Collections.emptyList(), this::onClickSong, mThumbnailLoader);
        mAdapter.addSection(mFolderSection);
        mAdapter.addSection(mFileSection);
        mAdapter.setEmptyState(new FileBrowserEmptyState(context, this::onRefreshFromEmptyState));
        mAdapter.setHasStableIds(true);

        setDirectory(startingDirectory);
    }

    public void onLowMemory() {
        mThumbnailLoader.clearCache();
    }

    public String[] getHistory() {
        String[] history = new String[mHistory.size()];
        for (int i = 0; i < mHistory.size(); i++) {
            history[i] = mHistory.get(i).getAbsolutePath();
        }
        return history;
    }

    public void setHistory(String[] history) {
        mHistory = new Stack<>();
        for (String file : history) {
            mHistory.push(new File(file));
        }
    }

    private void onRefreshFromEmptyState() {
        MediaStoreUtil.promptPermission(getContext())
                .subscribe(granted -> {
                    if (granted) {
                        setDirectory(mCurrentDirectory);
                    }
                }, throwable -> {
                    Timber.e(throwable, "Failed to get storage permission to refresh folder");
                });
    }

    public Observable<File> getObservableDirectory() {
        return mDirectoryObservable;
    }

    public File getDirectory() {
        return mCurrentDirectory;
    }

    public void setDirectory(File directory) {
        mCurrentDirectory = directory;
        mDirectoryObservable.onNext(directory);
        mSelectedBreadCrumb = directory;
        notifyPropertyChanged(BR.selectedBreadCrumb);

        if (!mBreadCrumbs.contains(directory)) {
            notifyPropertyChanged(BR.breadCrumbs);
        }

        if (directory.canRead()) {
            List<File> folders = new ArrayList<>();
            List<File> files = new ArrayList<>();

            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    folders.add(file);
                } else if (Util.isFileMusic(file)) {
                    files.add(file);
                }
            }

            Collections.sort(folders);
            Collections.sort(files);

            mFolderSection.setData(folders);
            mFileSection.setData(files);
            mAdapter.notifyDataSetChanged();
        } else {
            mFolderSection.setData(Collections.emptyList());
        }
    }

    @Bindable
    public List<BreadCrumb<File>> getBreadCrumbs() {
        mBreadCrumbs = generateBreadCrumbs();
        List<BreadCrumb<File>> breadCrumbs = new ArrayList<>();

        for (File crumb : mBreadCrumbs) {
            File parent = crumb.getParentFile();
            boolean parentAccessible = parent != null && parent.canRead();
            String name = (!parentAccessible) ? "/" : crumb.getName();
            breadCrumbs.add(new BreadCrumb<>(name, crumb));
        }

        return breadCrumbs;
    }

    private List<File> generateBreadCrumbs() {
        List<File> crumbs = new ArrayList<>();

        File curr = mCurrentDirectory;
        while (curr != null && curr.canRead()) {
            crumbs.add(curr);
            curr = curr.getParentFile();
        }

        Collections.reverse(crumbs);
        return crumbs;
    }

    @Bindable
    public File getSelectedBreadCrumb() {
        return mSelectedBreadCrumb;
    }

    public void setSelectedBreadCrumb(File selectedBreadCrumb) {
        if (!selectedBreadCrumb.equals(mCurrentDirectory)) {
            onClickFolder(selectedBreadCrumb);
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public RecyclerView.ItemDecoration[] getItemDecorations() {
        return new RecyclerView.ItemDecoration[] {
                new BackgroundDecoration(),
                new PaddingDecoration(getDimensionPixelSize(R.dimen.list_small_padding))
        };
    }

    private void onClickFolder(File folder) {
        mHistory.add(mCurrentDirectory);
        setDirectory(folder);
    }

    private void onClickSong(File song) {
        mSelectionListener.onSongFileSelected(song);
    }

    public boolean goBack() {
        if (!mHistory.isEmpty()) {
            setDirectory(mHistory.pop());
            return true;
        }
        return false;
    }

    interface OnSongFileSelectedListener {
        void onSongFileSelected(File song);
    }

}
