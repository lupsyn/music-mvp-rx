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

package com.music.androidtest;

import android.app.Application;
import android.content.Context;

import com.music.androidtest.music.itemdetail.MusicDetailModule;
import com.music.androidtest.music.itemdetail.MusicDetailSubComponent;
import com.music.androidtest.music.itemlist.MusicModule;
import com.music.androidtest.music.itemlist.MusicSubComponent;

public abstract class MusicApp extends Application {
    private static ApplicationComponent component;
    private MusicSubComponent mMusicSubComponent;
    private MusicDetailSubComponent mMusicDetailsSubComponent;

    public static MusicApp get(Context context) {
        return (MusicApp) context.getApplicationContext();
    }

    public static ApplicationComponent getComponent() {
        return component;
    }


    public MusicSubComponent getMusicSubComponent() {
        if (null == mMusicSubComponent)
            createMusicSubModule();

        return mMusicSubComponent;
    }

    public MusicSubComponent createMusicSubModule() {
        mMusicSubComponent = component.plus(new MusicModule());
        return mMusicSubComponent;
    }


    public void releaseMusicSubComponent() {
        mMusicSubComponent = null;
    }

    public void releaseMusicDetailsSubComponent() {
        mMusicDetailsSubComponent = null;
    }

    public MusicDetailSubComponent getMusicDetailSubComponent()
    {
        if (null == mMusicDetailsSubComponent)
            createMusicDetailSubModule();
        return mMusicDetailsSubComponent;
    }


    public MusicDetailSubComponent createMusicDetailSubModule() {
        mMusicDetailsSubComponent = component.plus(new MusicDetailModule());
        return mMusicDetailsSubComponent;
    }


    public ApplicationComponent createComponent() {
        return DaggerApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
    }

    public abstract void initApplication();

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        component = createComponent();
    }
}