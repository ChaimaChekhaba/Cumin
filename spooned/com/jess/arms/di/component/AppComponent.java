package com.jess.arms.di.component;


import android.app.Application;
import com.google.gson.Gson;
import com.jess.arms.base.delegate.AppDelegate;
import com.jess.arms.di.module.AppModule;
import com.jess.arms.di.module.ClientModule;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.utils.ArmsUtils;
import dagger.BindsInstance;
import dagger.Component;
import java.io.File;
import javax.inject.Singleton;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import okhttp3.OkHttpClient;


@Singleton
@Component(modules = { AppModule.class, ClientModule.class, GlobalConfigModule.class })
public interface AppComponent {
    Application application();

    AppManager appManager();

    IRepositoryManager repositoryManager();

    RxErrorHandler rxErrorHandler();

    ImageLoader imageLoader();

    OkHttpClient okHttpClient();

    Gson gson();

    File cacheFile();

    Cache<String, Object> extras();

    Cache.Factory cacheFactory();

    void inject(AppDelegate delegate);

    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent.Builder globalConfigModule(GlobalConfigModule globalConfigModule);

        AppComponent build();
    }
}

