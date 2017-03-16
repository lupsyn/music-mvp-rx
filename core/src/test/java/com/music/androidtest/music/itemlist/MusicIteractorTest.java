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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.music.androidtest.domain.api.LocalResponseDispatcher;
import com.music.androidtest.domain.api.MusicApi;
import com.music.androidtest.domain.api.model.Response;
import com.music.androidtest.domain.model.MusicItem;
import com.music.androidtest.domain.model.MusicMapper;
import com.music.androidtest.domain.utils.SchedulerProvider;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicIteractorTest {

    MusicInteractor interactor;
    MusicApi api;
    SchedulerProvider scheduler;
    Response response;
    List<MusicItem> expectedResponse;

    @Before
    public void setup() {
        api = mock(MusicApi.class);
        scheduler = mock(SchedulerProvider.class);


        Gson gson = new Gson();
        Type type = new TypeToken<List<MusicItem>>() {
        }.getType();
        try {
            String jsonMockResult = readFile("get_shazam_v2_en_US_android_-_tracks_web_chart_future_hits_us.json");
            response = gson.fromJson(jsonMockResult, Response.class);
            expectedResponse = gson.<ArrayList<MusicItem>>fromJson(readFile("parsed_results.json"), type);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //mock scheduler to run immediately
        when(scheduler.mainThread())
                .thenReturn(Schedulers.immediate());
        when(scheduler.backgroundThread())
                .thenReturn(Schedulers.immediate());

        // mock api result with expected result
        when(api.getWebChart()).thenReturn(Observable.just(response));

        interactor = new MusicInteractorImpl(api, scheduler, new MusicMapper());
    }

    @Test
    public void testGetWebChart() throws Exception {
        TestSubscriber<List<MusicItem>> testSubscriber = new TestSubscriber<>();
        interactor.getCharts()
                .subscribe(testSubscriber);
        // it must return the expectedResult with no error
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Arrays.asList(expectedResponse));
   }


    private String readFile(String jsonFileName) throws IOException {
        InputStream inputStream = LocalResponseDispatcher.class.getResourceAsStream("/"
                + jsonFileName);
        if (inputStream == null) {
            throw new NullPointerException("Have you added the local resource correctly?, "
                    + "Hint: name it as: " + jsonFileName);
        }
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(inputStream);
            BufferedReader rdr = new BufferedReader(isr);
            for (int c; (c = rdr.read()) != -1; ) {
                sb.append((char) c);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            if (isr != null) {
                isr.close();
            }
        }
        return sb.toString();
    }
}
