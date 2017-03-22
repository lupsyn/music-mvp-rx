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

package com.music.androidtest.music.itemdetail;

import com.music.androidtest.domain.model.MusicItem;
import com.music.androidtest.domain.utils.SchedulerProvider;

import org.junit.Before;
import org.junit.Test;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MusicPresenterDetailTest {
    private MusicDetailView view;
    private MusicDetailPresenter musicDetailPresenter;
    private SchedulerProvider scheduler;


    private final PublishSubject<MusicItem> musicDetail = PublishSubject.create();
    private final PublishSubject<MusicItem> musicPreview = PublishSubject.create();
    private final PublishSubject<MusicItem> musicGet = PublishSubject.create();
    private MusicItem item;

    @Before
    public void setup() {
        item = MusicItem.builder().numShazam("31337").build();
        scheduler = mock(SchedulerProvider.class);
        view = mock(MusicDetailView.class);
        // mock scheduler to run immediately
        when(scheduler.mainThread())
                .thenReturn(Schedulers.immediate());
        when(scheduler.backgroundThread())
                .thenReturn(Schedulers.immediate());

        when(view.onMusicDetailAsked()).thenReturn(musicDetail);
        when(view.onMusicPreview()).thenReturn(musicPreview);
        when(view.onMusicGet()).thenReturn(musicGet);
        musicDetailPresenter = new MusicDetailPresenterImpl(scheduler);
    }


    @Test
    public void onViewAttachedAndRefreshed() {
        musicDetailPresenter.register(view);
        musicDetail.onNext(item);
        verify(view).showDetail(item);
    }


    @Test
    public void onViewGetMusic() {
        musicDetailPresenter.register(view);
        musicGet.onNext(item);
        verify(view).getMusic(item);
    }


    @Test
    public void onViewGetPreview() {
        musicDetailPresenter.register(view);
        musicPreview.onNext(item);
        verify(view).getPreview(item);
    }
}
