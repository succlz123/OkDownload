package org.succlz123.sample;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

import org.succlz123.okdownload.OkDownloadEnqueueListener;
import org.succlz123.okdownload.OkDownloadError;

/**
 * Created by succlz123 on 15/11/20.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
    }

    OkDownloadEnqueueListener listener = new OkDownloadEnqueueListener() {

        @Override
        public void onStart(int id) {
            Log.e("OkDownload", "onStart");
        }

        @Override
        public void onProgress(int progress, long cacheSize, long totalSize) {
            Log.e("OkDownload", cacheSize + "/" + totalSize);
        }

        @Override
        public void onRestart() {
            Log.e("OkDownload", "onRestart");
        }

        @Override
        public void onPause() {
            Log.e("OkDownload", "onPause");
        }

        @Override
        public void onCancel() {
            Log.e("OkDownload", "onCancel");
        }

        @Override
        public void onFinish() {
            Log.e("OkDownload", "onFinish");
        }

        @Override
        public void onError(final OkDownloadError error) {
            Log.e("OkDownload", error.getMessage());
        }
    };
}
