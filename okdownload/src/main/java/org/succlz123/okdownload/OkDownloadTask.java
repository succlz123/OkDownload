package org.succlz123.okdownload;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by succlz123 on 15/9/11.
 */
public class OkDownloadTask {
    private static final String TAG = "OkDownloadTask";

    private Context mContext;
    private OkDatabaseHelp mOkDatabaseHelp;
    private OkHttpClient mOkHttpClient;
    private OkDownloadRequest mOkDownloadRequest;

    public OkDownloadTask(Context context, OkHttpClient okHttpClient, OkDatabaseHelp okDatabaseHelp) {
        if (context != null) {
            mContext = context;
            mOkDatabaseHelp = okDatabaseHelp;
            if (mOkDatabaseHelp == null) {
                mOkDatabaseHelp = OkDatabaseHelp.getInstance(mContext);
            }
        }
        if (okHttpClient != null) {
            mOkHttpClient = okHttpClient;
        } else {
            mOkHttpClient = OkHttpClientManager.getsInstance();
        }
    }

    public void start(OkDownloadRequest okDownloadRequest, final OkDownloadEnqueueListener listener) {
        if (okDownloadRequest == null) {
            return;
        }

        mOkDownloadRequest = okDownloadRequest;

        final String url = mOkDownloadRequest.getUrl();
        final String filePath = mOkDownloadRequest.getFilePath();

        // get to write to the local file length
        // if the length is equals 0 , no such cached file
        // if the length is greater than 0 is already cached file size
        final File file = new File(filePath);
        final long range = file.length();

        Request.Builder builder = new Request.Builder();
        builder.url(url)
                .tag(url)
                .addHeader("User-Agent", "OkDownload")
                .addHeader("Connection", "Keep-Alive");

        if (range > 0) {
            builder.addHeader("Range", "bytes=" + range + "-");
        }

        Request okHttpRequest = builder.build();
        Call call = mOkHttpClient.newCall(okHttpRequest);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                listener.onError(new OkDownloadError(OkDownloadError.OKHTTP_ONFAILURE));
            }

            @Override
            public void onResponse(Response response) {
                // code 2xx
                boolean isSuccessful = response.isSuccessful();
                // code 3xx is url redirect
                boolean isRedirect = response.isRedirect();
                Log.w(TAG, "OkDownload : http status code: " + response.code());

                if (!isSuccessful && !isRedirect) {
                    listener.onError(new OkDownloadError(OkDownloadError.OKHTTP_ONRESPONSE_FAIL));
                    return;
                }

                InputStream in = null;
                RandomAccessFile out = null;
                long fileLength = mOkDownloadRequest.getFileSize();

                if (fileLength == 0) {
                    mOkDownloadRequest.setStatus(OkDownloadStatus.START);
                    mOkDownloadRequest.setStartTime(System.currentTimeMillis());

                    if (response.header("Content-Length") != null) {
                        fileLength = Long.valueOf(response.header("Content-Length"));
                        mOkDownloadRequest.setFileSize(fileLength);
                    }
                    writeDatabase();
                    listener.onStart(mOkDownloadRequest.getId());

                } else {
                    switch (mOkDownloadRequest.getStatus()) {
                        case OkDownloadStatus.START:
                            mOkDownloadRequest.setStatus(OkDownloadStatus.PAUSE);
                            break;
                        case OkDownloadStatus.PAUSE:
                            mOkDownloadRequest.setStatus(OkDownloadStatus.START);
                            listener.onRestart();
                            break;
                        default:
                            break;
                    }
                    updateDownloadStatus();
                }

                if (filePath.startsWith("/data/data/")) {
                    if (OkDownloadManager.getAvailableInternalMemorySize() - fileLength < 100 * 1024 * 1024) {
                        listener.onError(new OkDownloadError(OkDownloadError.ANDROID_MEMORY_SIZE_IS_TOO_LOW));
                        return;
                    }
                } else {
                    if (OkDownloadManager.getAvailableExternalMemorySize() - fileLength < 100 * 1024 * 1024) {
                        listener.onError(new OkDownloadError(OkDownloadError.ANDROID_MEMORY_SIZE_IS_TOO_LOW));
                        return;
                    }
                }

                byte[] bytes = new byte[2048];
                int len = 0;
                long curSize = 0;

                try {
                    in = new BufferedInputStream(response.body().byteStream());
                    out = new RandomAccessFile(filePath, "rwd");

                    out.seek(range);
                    while ((len = in.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                        curSize += len;
                        if (fileLength != 0) {
                            long cacheSize = file.length();
                            int progress = (int) (cacheSize * 100 / fileLength);

                            listener.onProgress(progress, cacheSize, fileLength);
                        }
                    }
                    if (fileLength != 0 && curSize == fileLength) {
                        long finishTime = System.currentTimeMillis();
                        mOkDownloadRequest.setFinishTime(finishTime);
                        mOkDownloadRequest.setFileSize(fileLength);
                        mOkDownloadRequest.setStatus(OkDownloadStatus.FINISH);

                        updateDatabase();
                        listener.onFinish();
                    }
                } catch (IOException e) {

                } finally {
                    try {
                        if (in != null) in.close();
                    } catch (IOException e) {

                    }
                    try {
                        if (out != null) out.close();
                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    public void pause(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener okDownloadEnqueueListener) {
        mOkDownloadRequest = okDownloadRequest;
        mOkHttpClient.cancel(mOkDownloadRequest.getUrl());

        mOkDownloadRequest.setStatus(OkDownloadStatus.PAUSE);
        updateDownloadStatus();

        okDownloadEnqueueListener.onPause();
    }

    public void cancel(String url, OkDownloadCancelListener listener) {
        mOkHttpClient.cancel(url);

        List<OkDownloadRequest> requestList = mOkDatabaseHelp.execQuery("url", url);

        if (requestList.size() > 0) {
            OkDownloadRequest queryRequest = requestList.get(0);
            if (queryRequest.getFilePath() == null) {
                return;
            }
            mOkDownloadRequest = queryRequest;
            deleteFile();
        }
        mOkDatabaseHelp.execDelete("url", url);
        listener.onCancel();
    }

    private void writeDatabase() {
        mOkDatabaseHelp.execInsert(mOkDownloadRequest);
    }

    private void updateDatabase() {
        mOkDatabaseHelp.execUpdate(mOkDownloadRequest);
    }

    private void updateDownloadStatus() {
        mOkDatabaseHelp.execUpdateDownloadStatus(mOkDownloadRequest);
    }

    private void deleteFile() {
        File file = new File(mOkDownloadRequest.getFilePath());

        if (file.delete()) {
            Log.w(file.getName(), " is deleted!");
        } else {
            Log.w(file.getName(), " delete operation is failed!");
        }
    }
}