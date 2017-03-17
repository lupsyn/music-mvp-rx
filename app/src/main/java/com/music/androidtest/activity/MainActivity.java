/*
 * MIT License
 *
 * Copyright (c) 2017 Enrico Bruno Del Zotto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.music.androidtest.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.music.androidtest.ApplicationComponent;
import com.music.androidtest.MusicApp;
import com.music.androidtest.R;
import com.music.androidtest.base.BaseActivity;
import com.music.androidtest.domain.model.MusicItem;
import com.music.androidtest.music.itemdetail.MusicDetailFragment;
import com.music.androidtest.music.itemlist.MusicFragment;
import com.music.androidtest.utils.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends BaseActivity {
    @BindView(R.id.toolbarone)
    Toolbar toolbar;
    @BindView(R.id.toolbar_back)
    ImageView backtoolbar;

    private static final String TAG_ADVERT_FRAGMENT = "FRAGMENT_MUSIC_LIST";
    private static final String TAG_ADVERT_FRAGMENT_DETAIL = "FRAGMENT_MUSIC_DETAIL";
    private MusicFragment musicFragment;
    private MusicDetailFragment musicDetailFragment;
    private CompositeSubscription subscriptions;
    private CompositeSubscription detailsSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (null == savedInstanceState) {
            musicFragment = MusicFragment.newInstance();
            attachFragment(musicFragment, TAG_ADVERT_FRAGMENT);
        } else {
            musicFragment = (MusicFragment) getSupportFragmentManager().findFragmentByTag(TAG_ADVERT_FRAGMENT);
        }
    }

    @Override
    protected void injectDependencies(MusicApp application, ApplicationComponent component) {
        component.inject(this);
    }

    @Override
    protected void releaseSubComponents(MusicApp application) {
        application.releaseMusicSubComponent();
        application.releaseMusicDetailsSubComponent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (null == subscriptions || subscriptions.isUnsubscribed()) {
            subscriptions = new CompositeSubscription();
        }
        subscriptions.addAll(
                musicFragment.onClickMusicItem().subscribe(this::onMusicItemClicked),
                musicFragment.onMessageToShow().subscribe(this::showMessage)
        );
    }


    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            backtoolbar.setVisibility(View.INVISIBLE);
            if (detailsSubscriptions != null && detailsSubscriptions.hasSubscriptions()) {
                detailsSubscriptions.unsubscribe();
            }
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }


    public void onMusicItemClicked(@NonNull MusicItem item) {
        Timber.d("Music item clicked: " + item.getTitle() + " clicked");
        backtoolbar.setVisibility(View.VISIBLE);
        if (musicDetailFragment == null) {
            musicDetailFragment = MusicDetailFragment.newInstance();
        }
        if (null == detailsSubscriptions || detailsSubscriptions.isUnsubscribed()) {
            detailsSubscriptions = new CompositeSubscription();
        }
        detailsSubscriptions.addAll(
                musicDetailFragment.onArticleBackPressed().subscribe(this::onHackyBack),
                musicDetailFragment.onMessageToShow().subscribe(this::showMessage)
        );
        //TODO: to optimize with parcelable
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String mapJsonStr = gson.toJson(item);
        bundle.putSerializable(AppConstants.OBJ_TO_SEND, mapJsonStr);
        musicDetailFragment.setArguments(bundle);
        attachFragment(musicDetailFragment, TAG_ADVERT_FRAGMENT_DETAIL);
    }


    private void attachFragment(@NonNull Fragment fragment, @NonNull
            String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contentframe, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    public void showMessage(String message) {
        Timber.d("Showing Message: %s", message);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void onHackyBack(@Nullable Object ob) {
        onBackPressed();
    }

    @OnClick(R.id.toolbar_back)
    public void onToolbarBackPressed(View view) {
        onBackPressed();
    }
}
