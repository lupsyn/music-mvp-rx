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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.music.androidtest.R;
import com.music.androidtest.domain.model.MusicItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public class MusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<MusicItem> mItems = new ArrayList<>();
    private static Context context;
    private PublishSubject<MusicItem> notify = PublishSubject.create();

    public MusicAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.fragment_list_item, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ArticleViewHolder articleViewHolder = (ArticleViewHolder) holder;
        articleViewHolder.bind(mItems.get(position));

        RxView.clicks(((ArticleViewHolder) holder).rootView)
                .map(aVoid -> mItems.get(position))
                .subscribe(notify::onNext);
    }

    public Observable<MusicItem> asObservable() {
        return notify.asObservable();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    void showMusicItems(List<MusicItem> articles) {
        this.mItems.addAll(articles);
        notifyDataSetChanged();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        public View rootView;

        @BindView(R.id.list_section_item_tile)
        TextView title;

        @BindView(R.id.list_section_item_subtitle)
        TextView subtitle;

        @BindView(R.id.list_section_item_thumbnail)
        ImageView thumbnailImageView;

        ArticleViewHolder(View view) {
            super(view);
            rootView = view;
        }

        void bind(MusicItem item) {
            ButterKnife.bind(this, itemView);
            title.setText(item.getTitle());
            subtitle.setText(item.getSubtitle());
            if (item.getThumb() != null) {
                Glide.with(context).load(item.getThumb()).into(thumbnailImageView);
            } else {
                Glide.with(context).load("http://royal-shop.kz/images/no-img.png").into(thumbnailImageView);
            }

        }
    }
}
