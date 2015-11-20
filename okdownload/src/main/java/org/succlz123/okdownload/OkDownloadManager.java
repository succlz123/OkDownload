package org.succlz123.okdownload;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.squareup.okhttp.OkHttpClient;

import java.util.List;

/**
 * Created by succlz123 on 15/9/11.
 */
public class OkDownloadManager {
    private static final String TAG = "OkDownloadManager";

    private static OkDownloadManager sInstance;
    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private DatabaseHelp mDatabaseHelp;
    private OkDownloadRequest mOkDownloadRequest;
    private OkDownloadEnqueueListener mOkDownloadEnqueueListener;
    private OkDownloadTask mOkDownloadTask;

    private OkDownloadManager() {
    }

    private OkDownloadManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
            mDatabaseHelp = DatabaseHelp.getInstance(mContext);
        }
    }

    public static OkDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OkDownloadManager.class) {
                if (sInstance == null) {
                    return sInstance = new OkDownloadManager(context);
                }
            }
        }
        return sInstance;
    }

    public void enqueue(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener okDownloadEnqueueListener) {
        if (okDownloadRequest.getOkHttpClient() != null) {
            mOkHttpClient = okDownloadRequest.getOkHttpClient();
        }

        if (okDownloadRequest == null || okDownloadEnqueueListener == null) {
            return;
        }

        mOkDownloadRequest = okDownloadRequest;
        mOkDownloadEnqueueListener = okDownloadEnqueueListener;

        if (!isRequestValid()) {
            return;
        }

        List<OkDownloadRequest> requestList = mDatabaseHelp.execQuery("url", mOkDownloadRequest.getUrl());

        if (requestList.size() > 0) {
            OkDownloadRequest queryRequest = requestList.get(0);

            switch (queryRequest.getStatus()) {
                case OkDownloadStatus.START:
                    onPause(queryRequest, mOkDownloadEnqueueListener);
                    break;
                case OkDownloadStatus.PAUSE:
                    onStart(queryRequest, mOkDownloadEnqueueListener);
                    break;
                case OkDownloadStatus.FINISH:
                    mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_REQUEST_IS_COMPLETE));
                    break;
                default:
                    break;
            }
        } else {
            onStart(mOkDownloadRequest, mOkDownloadEnqueueListener);
        }
    }

    public void onStart(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener listener) {
        if (!isUrlValid(okDownloadRequest.getUrl())) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }
        mOkDownloadTask.start(okDownloadRequest, mOkDownloadEnqueueListener);
    }

    public void onPause(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener listener) {
        if (!isUrlValid(okDownloadRequest.getUrl())) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }

        mOkDownloadTask.pause(okDownloadRequest, listener);
    }

    public void onCancel(String url, OkDownloadCancelListener listener) {
        if (!isUrlValid(url)) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }

        mOkDownloadTask.cancel(url, listener);
    }

    public List<OkDownloadRequest> queryAll() {
        return mDatabaseHelp.execQueryAll();
    }

    public List<OkDownloadRequest> queryById(int id) {
        return mDatabaseHelp.execQuery("id", String.valueOf(id));
    }

    private boolean isRequestValid() {
        String url = mOkDownloadRequest.getUrl();
        String filePath = mOkDownloadRequest.getFilePath();

        if (!isRequestComplete(url, filePath) || !isUrlValid(url)) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return false;
        }

        return true;
    }

    private boolean isRequestComplete(String url, String filePath) {
        return !TextUtils.isEmpty(url) && !TextUtils.isEmpty(filePath);
    }

    private boolean isUrlValid(String url) {
        return URLUtil.isNetworkUrl(url);
    }
}
