package com.jess.arms.integration.cache;


import Context.ACTIVITY_SERVICE;
import android.app.ActivityManager;
import android.content.Context;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.RepositoryManager;


public interface CacheType {
    int RETROFIT_SERVICE_CACHE_TYPE_ID = 0;

    int CACHE_SERVICE_CACHE_TYPE_ID = 1;

    int EXTRAS_TYPE_ID = 2;

    int ACTIVITY_CACHE_TYPE_ID = 3;

    int FRAGMENT_CACHE_TYPE_ID = 4;

    CacheType RETROFIT_SERVICE_CACHE = new CacheType() {
        private static final int MAX_SIZE = 150;

        private static final float MAX_SIZE_MULTIPLIER = 0.002F;

        @Override
        public int getCacheTypeId() {
            return CacheType.RETROFIT_SERVICE_CACHE_TYPE_ID;
        }

        @Override
        public int calculateCacheSize(Context context) {
            ActivityManager activityManager = ((ActivityManager) (context.getSystemService(ACTIVITY_SERVICE)));
            int targetMemoryCacheSize = ((int) (((activityManager.getMemoryClass()) * (MAX_SIZE_MULTIPLIER)) * 1024));
            if (targetMemoryCacheSize >= (MAX_SIZE)) {
                return MAX_SIZE;
            }
            return targetMemoryCacheSize;
        }
    };

    CacheType CACHE_SERVICE_CACHE = new CacheType() {
        private static final int MAX_SIZE = 150;

        private static final float MAX_SIZE_MULTIPLIER = 0.002F;

        @Override
        public int getCacheTypeId() {
            return CacheType.CACHE_SERVICE_CACHE_TYPE_ID;
        }

        @Override
        public int calculateCacheSize(Context context) {
            ActivityManager activityManager = ((ActivityManager) (context.getSystemService(ACTIVITY_SERVICE)));
            int targetMemoryCacheSize = ((int) (((activityManager.getMemoryClass()) * (MAX_SIZE_MULTIPLIER)) * 1024));
            if (targetMemoryCacheSize >= (MAX_SIZE)) {
                return MAX_SIZE;
            }
            return targetMemoryCacheSize;
        }
    };

    CacheType EXTRAS = new CacheType() {
        private static final int MAX_SIZE = 500;

        private static final float MAX_SIZE_MULTIPLIER = 0.005F;

        @Override
        public int getCacheTypeId() {
            return CacheType.EXTRAS_TYPE_ID;
        }

        @Override
        public int calculateCacheSize(Context context) {
            ActivityManager activityManager = ((ActivityManager) (context.getSystemService(ACTIVITY_SERVICE)));
            int targetMemoryCacheSize = ((int) (((activityManager.getMemoryClass()) * (MAX_SIZE_MULTIPLIER)) * 1024));
            if (targetMemoryCacheSize >= (MAX_SIZE)) {
                return MAX_SIZE;
            }
            return targetMemoryCacheSize;
        }
    };

    CacheType ACTIVITY_CACHE = new CacheType() {
        private static final int MAX_SIZE = 80;

        private static final float MAX_SIZE_MULTIPLIER = 8.0E-4F;

        @Override
        public int getCacheTypeId() {
            return CacheType.ACTIVITY_CACHE_TYPE_ID;
        }

        @Override
        public int calculateCacheSize(Context context) {
            ActivityManager activityManager = ((ActivityManager) (context.getSystemService(ACTIVITY_SERVICE)));
            int targetMemoryCacheSize = ((int) (((activityManager.getMemoryClass()) * (MAX_SIZE_MULTIPLIER)) * 1024));
            if (targetMemoryCacheSize >= (MAX_SIZE)) {
                return MAX_SIZE;
            }
            return targetMemoryCacheSize;
        }
    };

    CacheType FRAGMENT_CACHE = new CacheType() {
        private static final int MAX_SIZE = 80;

        private static final float MAX_SIZE_MULTIPLIER = 8.0E-4F;

        @Override
        public int getCacheTypeId() {
            return CacheType.FRAGMENT_CACHE_TYPE_ID;
        }

        @Override
        public int calculateCacheSize(Context context) {
            ActivityManager activityManager = ((ActivityManager) (context.getSystemService(ACTIVITY_SERVICE)));
            int targetMemoryCacheSize = ((int) (((activityManager.getMemoryClass()) * (MAX_SIZE_MULTIPLIER)) * 1024));
            if (targetMemoryCacheSize >= (MAX_SIZE)) {
                return MAX_SIZE;
            }
            return targetMemoryCacheSize;
        }
    };

    int getCacheTypeId();

    int calculateCacheSize(Context context);
}

