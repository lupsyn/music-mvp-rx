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

import com.music.androidtest.domain.utils.SchedulerProvider;

import javax.inject.Inject;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class MusicDetailPresenterImpl implements MusicDetailPresenter {
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private MusicDetailView view;
    private SchedulerProvider mSchedulerProvider;

    @Inject
    public MusicDetailPresenterImpl(SchedulerProvider schedulerProvider) {
        mSchedulerProvider = schedulerProvider;

    }

    @Override
    public void register(MusicDetailView view) {
        if (this.view != null) {
            throw new IllegalStateException("View " + this.view + " is already attached. Cannot attach " + view);
        }
        this.view = view;

        addToUnsubscribe(view.onMusicDetailAsked()
                .observeOn(mSchedulerProvider.mainThread()).subscribe(
                        item -> {
                            view.showDetail(item);
                        },
                        throwable -> {
                            if (null != view) {
                                view.showMessage("Error to retrive : " + throwable);
                                view.onArticleBackPressed().doOnNext(null);
                            }
                        }
                ));

        addToUnsubscribe(view.onMusicPreview()
                .observeOn(mSchedulerProvider.mainThread())
                .subscribe(musicItem -> {
                            view.getPreview(musicItem);
                        },
                        throwable -> {
                            if (null != view) {
                            }
                        })

        );

        addToUnsubscribe(view.onMusicGet()
                .observeOn(mSchedulerProvider.mainThread())
                .subscribe(musicItem -> {
                            view.getMusic(musicItem);
                        },
                        throwable -> {
                            if (null != view) {
                            }
                        })

        );
    }

    @Override
    public void unregister() {
        if (view == null) {
            throw new IllegalStateException("View is already detached");
        }
        view = null;
        compositeSubscription.clear();
    }


    protected final void addToUnsubscribe(final Subscription subscription) {
        compositeSubscription.add(subscription);
    }
}
