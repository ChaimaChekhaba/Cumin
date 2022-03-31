package com.jess.arms.http.imageloader;


import android.content.Context;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public final class ImageLoader {
    @Inject
    BaseImageLoaderStrategy mStrategy;

    @Inject
    public ImageLoader() {
    }

    public <T extends ImageConfig> void loadImage(Context context, T config) {
        this.mStrategy.loadImage(context, config);
    }

    public <T extends ImageConfig> void clear(Context context, T config) {
        this.mStrategy.clear(context, config);
    }

    public void setLoadImgStrategy(BaseImageLoaderStrategy strategy) {
        this.mStrategy = strategy;
    }

    public BaseImageLoaderStrategy getLoadImgStrategy() {
        return mStrategy;
    }
}

