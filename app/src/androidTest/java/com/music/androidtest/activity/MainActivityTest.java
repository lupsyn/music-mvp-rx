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


import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.music.androidtest.ApplicationTestComponent;
import com.music.androidtest.BaseLogicTest;
import com.music.androidtest.MusicTestApp;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.net.HttpURLConnection;

import okhttp3.mockwebserver.MockResponse;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest extends BaseLogicTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<>(
            MainActivity.class,
            true,
            // false: do not launch the activity immediately
            false);


    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        MusicTestApp app = (MusicTestApp) instrumentation.getTargetContext().getApplicationContext();
        ApplicationTestComponent component = (ApplicationTestComponent) MusicTestApp.getComponent();
        // inject dagger
        component.inject(this);

    }

    @Test
    public void test1DemoResponseNotFound() throws InterruptedException {

        setResponse(
                new MockResponse()
                        .setBody("Huston we have a problem.")
                        .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));

        mainActivity.launchActivity(new Intent());
        unlockScreen();
        onView(withText("Error to retrive : retrofit2.adapter.rxjava.HttpException: HTTP 404 Client Error")).inRoot(withDecorView(not(is(mainActivity.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
        getMockWebServer().takeRequest();
    }



    public void unlockScreen() {

        final MainActivity activity = mainActivity.getActivity();
        Runnable wakeUpDevice = () -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.runOnUiThread(wakeUpDevice);
    }

}

