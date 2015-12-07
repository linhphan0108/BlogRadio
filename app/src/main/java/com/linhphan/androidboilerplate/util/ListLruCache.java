package com.linhphan.androidboilerplate.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.LruCache;

import java.util.List;

/**
 * Created by linhphan on 12/7/15.
 */
public class ListLruCache extends LruCache<String, List<String>> {

    private static ListLruCache mCache;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public ListLruCache(int maxSize) {
        super(maxSize);
    }

    public static ListLruCache getInstance(Context context){
        if(mCache == null){
            int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            int cacheSize = 1024 * 1024 * memClass/8; // 1/8 of available memory.
            mCache = new ListLruCache(cacheSize);
        }
        return mCache;
    }
}
