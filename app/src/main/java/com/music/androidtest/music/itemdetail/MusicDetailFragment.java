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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.music.androidtest.MusicApp;
import com.music.androidtest.R;
import com.music.androidtest.base.BaseFragment;
import com.music.androidtest.domain.model.MusicItem;
import com.music.androidtest.utils.AppConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class MusicDetailFragment extends BaseFragment implements MusicDetailView {
    @Inject
    Context context;
    @Inject
    Resources resources;
    @Inject
    MusicDetailPresenter presenter;

    @BindView(R.id.fragmentarticledetailslayoutiv)
    ImageView fragmentArticleDetailsLayoutIv;

    @BindView(R.id.fragmentmusicdetailstitletv)
    TextView fragmentMusicDetailsTitleTv;

    @BindView(R.id.fragmentmusicdetailssubtitletv)
    TextView fragmentMusicDetailsSubtitleTv;

    @BindView(R.id.fragmentmusicshazamspottedtv)
    TextView fragmentMusicShazamSpottedTv;

    @BindView(R.id.fragmentmusicshazampreview)
    Button fragmentMusicShazamPreview;

    @BindView(R.id.fragmentmusicshazamonstore)
    Button fragmentMusicShazamOnstore;

    private PublishSubject notifyOnBack = PublishSubject.create();
    private PublishSubject<MusicItem> notifyToRetrive = PublishSubject.create();
    private PublishSubject<MusicItem> notifiyPreview = PublishSubject.create();
    private PublishSubject<MusicItem> notifiyGetStore = PublishSubject.create();
    private PublishSubject<String> notifyMessage = PublishSubject.create();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MusicDetailFragment() {
    }

    public static MusicDetailFragment newInstance() {
        MusicDetailFragment fragment = new MusicDetailFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        String obj = (String) getArguments().getSerializable(AppConstants.OBJ_TO_SEND);
        Gson gson = new Gson();
        MusicItem musicItem = gson.fromJson(obj, MusicItem.class);

        Observable.just(musicItem).subscribe(notifyToRetrive::onNext);


        RxView.clicks(fragmentMusicShazamPreview)
                .map(aVoid -> musicItem)
                .subscribe(notifiyPreview::onNext);

        RxView.clicks(fragmentMusicShazamOnstore)
                .map(aVoid -> musicItem)
                .subscribe(notifiyGetStore::onNext);

        return view;
    }


    @Override
    protected void injectDependencies(MusicApp application) {
        application.getMusicDetailSubComponent().inject(this);
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
    public void showMessage(String message) {
        notifyMessage.onNext(message);
    }

    @Override
    public Observable<MusicItem> onMusicDetailAsked() {
        return notifyToRetrive.asObservable();
    }

    @Override
    public Observable<MusicItem> onMusicPreview() {
        return notifiyPreview.asObservable();
    }

    @Override
    public Observable<MusicItem> onMusicGet() {
        return notifiyGetStore.asObservable();
    }

    @Override
    public Observable<Void> onArticleBackPressed() {
        return notifyOnBack.asObservable();
    }

    public Observable<String> onMessageToShow() {
        return notifyMessage.asObservable();
    }

    @Override
    public void showDetail(MusicItem item) {
        Timber.d("Received item to display:" + item.getTitle());
        if (item.getThumb() != null) {
            Glide.with(context).load(item.getThumb()).into(fragmentArticleDetailsLayoutIv);
        } else {
            Glide.with(context).load("http://royal-shop.kz/images/no-img.png").into(fragmentArticleDetailsLayoutIv);
        }
        fragmentMusicDetailsTitleTv.setText(item.getTitle());
        fragmentMusicDetailsSubtitleTv.setText(item.getSubtitle());
        fragmentMusicShazamSpottedTv.setText("Searched on shazam : " + item.getNumShazam() + " times");
    }

    @Override
    public void getPreview(MusicItem item) {
        Timber.d("Open preview: " + item.getPreviewlink());
        String url = item.getPreviewlink();
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setDataAndType(uri, "video/mp4");
        MusicDetailFragment.this.getActivity().startActivity(intent);

    }

    @Override
    public void getMusic(MusicItem item) {
        Timber.d("Get on store: " + item.getPreferredstore());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getIntenturi()));
        MusicDetailFragment.this.getActivity().startActivity(intent);
    }


}
