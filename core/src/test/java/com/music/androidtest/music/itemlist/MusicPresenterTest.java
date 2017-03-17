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

import com.music.androidtest.domain.api.MusicApi;
import com.music.androidtest.domain.model.MusicItem;
import com.music.androidtest.domain.utils.SchedulerProvider;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MusicPresenterTest {

    private MusicView view;
    private SchedulerProvider scheduler;
    private MusicInteractor musicInteractor;
    private MusicApi api;
    private MusicPresenter musicPresenter;

    private final PublishSubject<List<MusicItem>> musicResponse = PublishSubject.create();
    private final PublishSubject<Void> refreshRelay = PublishSubject.create();

    @Before
    public void setup() {
        api = mock(MusicApi.class);
        scheduler = mock(SchedulerProvider.class);
        view = mock(MusicView.class);
        musicInteractor = mock(MusicInteractor.class);
        // mock scheduler to run immediately
        when(scheduler.mainThread())
                .thenReturn(Schedulers.immediate());
        when(scheduler.backgroundThread())
                .thenReturn(Schedulers.immediate());
        when(musicInteractor.getCharts()).thenReturn(musicResponse);
        when(view.onRefreshAction()).thenReturn(refreshRelay);
        musicPresenter = new MusicPresenterImpl(scheduler, musicInteractor);
    }


    @Test
    public void onViewAttachedAndRefreshed() {
        musicPresenter.register(view);
        musicPresenter.loadMusic();
        refreshRelay.onNext(null);
        verify(musicInteractor).getCharts();
    }

    @Test
    public void onRefreshAction_performsRefresh() throws Exception {
        musicPresenter.register(view);
        musicPresenter.loadMusic();
        refreshRelay.onNext(null);
        refreshRelay.onNext(null);
        verify(musicInteractor, times(2)).getCharts();
    }

    @Test
    public void onRefreshAction_withData_showLoading() throws Exception {
        musicPresenter.register(view);
        musicPresenter.loadMusic();
        refreshRelay.onNext(null);
        verify(view).showRefreshing(true);
    }

    @Test
    public void onNullElements() {
        when(musicInteractor.getCharts()).thenReturn(Observable.just(null));
        musicPresenter.register(view);
        musicPresenter.loadMusic();
        refreshRelay.onNext(null);
        verify(view).showRefreshing(false);
        verify(view).showMessage("Error to retrive : java.lang.NullPointerException");
    }

    @Test
    public void onZeroElements() {
        when(musicInteractor.getCharts()).thenReturn(Observable.just(new ArrayList<>()));
        musicPresenter.register(view);
        musicPresenter.loadMusic();
        refreshRelay.onNext(null);
        verify(view).showRefreshing(false);
        verify(view).showMessage("Error to retrive : java.util.NoSuchElementException");
    }

}
