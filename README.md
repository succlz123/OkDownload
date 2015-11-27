# OkDownload
This is a simple single-threaded android download library,okhttp-base development,library body only 9kb.

![1](https://github.com/succlz123/okdownload/blob/master/screenshot/screenshot.gif)

# Download
Gradle:

```
compile 'org.succlz123.okdownload:okdownload:0.0.1'
```

# Use

## Start download

```
OkDownloadRequest request = new OkDownloadRequest.Builder()
                            .url(url)
                            .filePath(filePath)
                            .build();
                            
OkDownloadManager.getInstance(mContext).enqueue(request, listener);

OkDownloadEnqueueListener listener = new OkDownloadEnqueueListener() {

        @Override
        public void onStart(int id) {
            Log.e("OkDownload", "onStart : the download request id = "+id);
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
        public void onError(OkDownloadError error) {
            Log.e("OkDownload", error.getMessage());
        }
    };
                            
```

## Pause download

Multiple clicks automatically determine whether to suspend   

```
public void onClick(View v) {
	OkDownloadManager.getInstance(mContext).enqueue(request, listener);
}

1 click :
onStart();
2 click :
onPause();
3 click :
onReStart();
4 click :
onPause();
```
## Cancel download

use `OkDownloadEnqueueListener`

```
OkDownloadManager.getInstance(mContext).onCancel(url, new okDownloadEnqueueListener() {

	@Override
	public void onStart() {

	}
	
	......
	
	@Override
	public void onError(OkDownloadError error) {

	}
});
```

or use `OkDownloadCancelListener`

```
OkDownloadManager.getInstance(mContext).onCancel(url), new OkDownloadCancelListener() {

	@Override
	public void onCancel() {

	}

	@Override
	public void onError(OkDownloadError error) {

	}
});
```

## QueryAllDownloadRequest

```
List<OkDownloadRequest> requestList = OkDownloadManager.getInstance(this).queryAll();
if(requestList.size()>0){
	doSomethings...
}
```

## QueryDownloadRequestById

```
List<OkDownloadRequest> requestList = OkDownloadManager.getInstance(this).queryById(Integer.valueOf(id));
if(requestList.size()>0){
	doSomethings...
}
```

## Other
OkDownload wil check the phone remaining memory size,when the phone storage memory is less than 100m,download task will not be carried out.

# Contact Me

Email: succlz123@gmail.com  
Github: http://github.com/succlz123  
Weibo: http://weibo.com/zzzllzzz  

# License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```