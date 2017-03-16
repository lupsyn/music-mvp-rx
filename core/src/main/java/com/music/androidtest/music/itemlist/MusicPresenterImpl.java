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

import com.music.androidtest.domain.utils.SchedulerProvider;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.subscriptions.CompositeSubscription;

public class MusicPresenterImpl implements MusicPresenter {
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private SchedulerProvider mSchedulerProvider;
    private MusicInteractor mMusicInteractor;

    private MusicView view;


    @Inject
    MusicPresenterImpl(SchedulerProvider scheduleProvider,
                       MusicInteractor interactor) {
        this.mSchedulerProvider = scheduleProvider;
        this.mMusicInteractor = interactor;
    }

    @Override
    public void loadMusic() {

        addToUnsubscribe(view.onRefreshAction()
                .doOnNext(ignored -> view.showRefreshing(true))
                .switchMap(ignored -> mMusicInteractor.getCharts())
                .observeOn(mSchedulerProvider.mainThread())
                .map(articles -> {
                    if (articles == null)
                        throw Exceptions.propagate(new NullPointerException());
                    else if (articles.size() > 0)
                        return articles;
                    else
                        throw Exceptions.propagate(new NoSuchElementException());
                })

//                .map(articles -> {
//                    List<MusicItem> toReturn = new ArrayList<Article>();
//                    for (Article toScan : articles) {
//                        if (!mDatabaseHelper.isItFavourite(toScan)) {
//                            toReturn.add(toScan);
//                        }
//                    }
//                    return toReturn;
//                })
                .subscribe(items -> {
                            view.showRefreshing(false);
//                            if (mDatabaseHelper.getFavourites().size() > 0) {
//                                view.showFavourites(mDatabaseHelper.getFavourites());
//                            }
                            view.showMusic(items);
                        },
                        // handle exceptions
                        throwable -> {
                            if (null != view) {
                                view.showRefreshing(false);
                                view.showMessage("Error to retrive : " + throwable);
                            }
                        }
                ));
    }


    @Override
    public void register(MusicView view) {
        if (this.view != null) {
            throw new IllegalStateException("View " + this.view + " is already attached. Cannot attach " + view);
        }
        this.view = view;
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
