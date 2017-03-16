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

package com.music.androidtest.domain.model;

import com.music.androidtest.domain.api.model.Chart;
import com.music.androidtest.domain.api.model.Response;
import com.music.androidtest.domain.api.model.Store;
import com.music.androidtest.domain.api.model.StoreAction;

import java.util.ArrayList;
import java.util.List;

public class MusicMapper {
    public List<MusicItem> map(Response apiResponse) {
        List<MusicItem> musicitems = new ArrayList<>();
        for (Chart itemResponse : apiResponse.chart) {
            try {
                Store preferredStore = null;
                String preferred = "";
                if (itemResponse.stores.google != null) {
                    preferredStore = itemResponse.stores.google;
                    preferred = "google";
                } else if (itemResponse.stores.itunes != null) {
                    preferredStore = itemResponse.stores.itunes;
                    preferred = "itunes";
                } else if (itemResponse.stores.apple != null) {
                    preferredStore = itemResponse.stores.apple;
                    preferred = "apple";
                }

                musicitems.add(
                        MusicItem.builder()
                                .title(itemResponse.heading.title)
                                .subtitle(itemResponse.heading.subtitle)
                                .thumb(itemResponse.images.defaultimage)
                                .coverartgoogleplay(preferredStore != null ? preferredStore.coverarturl : "")
                                .previewlink(preferredStore != null ? preferredStore.previewurl : "")
                                .intenturi(preferredStore != null ? filterStoreAction(preferredStore.actions) : "")
                                .preferredstore(preferred)
                                .numShazam(itemResponse.properties.numberOfShazams)
                                .build());
            } catch (Exception e) {
                int i = 1;
            }
        }
        return musicitems;
    }

    private String filterStoreAction(List<StoreAction> actions) {
        for (StoreAction toFilter : actions) {
            if (toFilter.type.equals("uri")) {
                return toFilter.uri;
            }
        }
//        for (StoreAction toFilter : actions) {
//            if (toFilter.type.equals("uri")) {
//                return toFilter.uri;
//            }
//        }
        return null;
    }
}
