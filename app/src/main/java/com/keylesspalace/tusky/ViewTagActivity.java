/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.keylesspalace.tusky.appstore.EventHub;
import com.keylesspalace.tusky.fragment.TimelineFragment;

import net.accelf.yuito.QuickTootHelper;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public class ViewTagActivity extends BottomSheetActivity implements HasAndroidInjector {

    private static final String HASHTAG = "hashtag";

    @Inject
    public DispatchingAndroidInjector<Object> dispatchingAndroidInjector;
    @Inject
    public EventHub eventHub;

    public static Intent getIntent(Context context, String tag){
        Intent intent = new Intent(context,ViewTagActivity.class);
        intent.putExtra(HASHTAG,tag);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tag);

        String hashtag = getIntent().getStringExtra(HASHTAG);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setTitle(String.format(getString(R.string.title_tag), hashtag));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = TimelineFragment.newInstance(TimelineFragment.Kind.TAG, hashtag);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        ConstraintLayout quickTootContainer = findViewById(R.id.quick_toot_container);
        QuickTootHelper quickTootHelper = new QuickTootHelper(this, quickTootContainer, accountManager, eventHub);

        eventHub.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(quickTootHelper::handleEvent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }

}
