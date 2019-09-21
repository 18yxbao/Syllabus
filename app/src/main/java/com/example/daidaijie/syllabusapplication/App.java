package com.example.daidaijie.syllabusapplication;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tendcloud.tenddata.TCAgent;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ImageLoader;
import cn.finalteam.galleryfinal.ThemeConfig;
import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by daidaijie on 2016/7/24.
 */
public class App extends Application {

    private static Context context;

    public static final String TAG = "App";

    public static final String FOIDER_NAME = "汕大课程表";

    public static boolean isDebug = true;

    public static boolean isLogger = true;

    public static final int userVersion = 1;

    public static int versionCode = Integer.MAX_VALUE;

    public static String versionName = "";

    AppComponent mAppComponent;

    private static final String WX_APP_ID = "wxcce81e2a1528e155";

    private static IWXAPI api;

    /**
     * Will be used in the mystu module.*/
    private String TCookie;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);

        context = getApplicationContext();

        CrashReport.initCrashReport(context, "900058134", isDebug);

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(this)
                .schemaVersion(userVersion);

        if (isDebug) {
            builder.deleteRealmIfMigrationNeeded();
        }
        Realm.setDefaultConfiguration(builder.build());

        mAppComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .retrofitModule(new RetrofitModule())
                .utilModule(new UtilModule())
                .build();

        initGalleryFinal();

        regToWX();

        initVersion();

        //初始化 talking data
        TCAgent.LOG_ON=true;
        TCAgent.init(this);
        TCAgent.setReportUncaughtExceptions(true);
    }

    private void initVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pi.versionCode;
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    public static IWXAPI getApi() {
        return api;
    }

    private void regToWX() {
        api = WXAPIFactory.createWXAPI(this, WX_APP_ID, true);
        api.registerApp(WX_APP_ID);
    }


    public static Context getContext() {
        return context;
    }

    private void initGalleryFinal() {
        ThemeConfig theme = new ThemeConfig.Builder()
                .setTitleBarBgColor(getResources().getColor(R.color.colorPrimary))
                .setFabNornalColor(getResources().getColor(R.color.colorPrimary))
                .setFabPressedColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCheckSelectedColor(getResources().getColor(R.color.colorPrimary))
                .setCropControlColor(getResources().getColor(R.color.colorPrimary))
                .build();

        ImageLoader imageloader = new FrescoImageLoader(this);
        CoreConfig coreConfig = new CoreConfig.Builder(context, imageloader, theme)
                .build();
        GalleryFinal.init(coreConfig);
    }


    public AppComponent getAppComponent() {
        return mAppComponent;
    }


    /**在mystu模块中
     * 通过这两个方法使用cookie,解决cookie请求固有时长过长问题
     * */
    public String getTCookie(){
        return TCookie;
    }
    public void setTCookie(String cookies){
        this.TCookie =cookies;
    }
}
