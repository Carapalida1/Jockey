package com.marverenic.music.ui.library.genre.contents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.marverenic.music.model.Genre;
import com.marverenic.music.ui.BaseLibraryActivity;

public class GenreActivity extends BaseLibraryActivity {

    private static final String GENRE_EXTRA = "GenreActivity.GENRE";

    public static Intent newIntent(Context context, Genre genre) {
        Intent intent = new Intent(context, GenreActivity.class);
        intent.putExtra(GENRE_EXTRA, genre);

        return intent;
    }

    @Override
    protected Fragment onCreateFragment(Bundle savedInstanceState) {
        Genre genre = getIntent().getParcelableExtra(GENRE_EXTRA);
        return GenreFragment.newInstance(genre);
    }

}
