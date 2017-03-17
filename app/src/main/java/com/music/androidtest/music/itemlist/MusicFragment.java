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

package com.music.androidtest.music.itemlist;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.androidtest.MusicApp;
import com.music.androidtest.R;
import com.music.androidtest.base.BaseFragment;
import com.music.androidtest.domain.model.MusicItem;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Emitter;
import rx.Observable;
import rx.subjects.PublishSubject;

public class MusicFragment extends BaseFragment implements MusicView {

    //
    @BindView(R.id.musicswiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.musicrecyclerview)
    RecyclerView recyclerView;

    // injecting dependencies via Dagger
    @Inject
    Context context;

    @Inject
    Resources resources;

    @Inject
    MusicPresenter presenter;

    private MusicAdapter adapter;
    private PublishSubject<MusicItem> notify = PublishSubject.create();
    private PublishSubject<String> notifyMessage = PublishSubject.create();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MusicFragment() {
    }

    public static MusicFragment newInstance() {
        MusicFragment fragment = new MusicFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter.register(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        presenter.unregister();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);

        adapter = new MusicAdapter(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        presenter.loadMusic();
        return view;
    }

    @Override
    public void showMusic(List<MusicItem> articles) {
        adapter.showMusicItems(articles);
        adapter.asObservable().subscribe(notify::onNext);
    }


    @Override
    public Observable<Void> onRefreshAction() {
        return Observable.fromEmitter(emitter -> {
            swipeRefreshLayout.setOnRefreshListener(() -> emitter.onNext(null));
            emitter.setCancellation(() -> swipeRefreshLayout.setOnRefreshListener(null));
        }, Emitter.BackpressureMode.ERROR).startWith((Void) null).map(ignored -> null);

    }


    @Override
    protected void injectDependencies(MusicApp application) {
        application.getMusicSubComponent().inject(this);
    }

    @Override
    public void showMessage(String message) {
        notifyMessage.onNext(message);
    }

    @Override
    public void showRefreshing(boolean isRefreshing) {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }


    public Observable<MusicItem> onClickMusicItem() {
        return notify.asObservable();
    }
    public Observable<String> onMessageToShow() {
        return notifyMessage.asObservable();
    }

}
