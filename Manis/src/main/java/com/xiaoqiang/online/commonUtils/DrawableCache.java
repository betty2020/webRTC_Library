package com.xiaoqiang.online.commonUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xiaoqiang on 2017/6/23 16:13
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp
 */

public class DrawableCache {
    static private DrawableCache cache;
    // 用于Chche内容的存储的map
    private Hashtable<String,MySoftRef> hashRefs;
    // 垃圾Reference的队列（所引用的对象已经被回收，则将该引用存入队列中）
    private ReferenceQueue<Drawable> q;

    private ExecutorService executor;
    private Map<String,List<Handler>> RequestList = new HashMap<String,List<Handler>>();

    /**
     * 继承SoftReference，使得每一个实例都具有可识别的标识。
     */
    private class MySoftRef extends SoftReference<Drawable> {
        private String _key = "";

        public MySoftRef(Drawable db, ReferenceQueue<Drawable> q, String key) {
            super(db, q);
            _key = key;
        }
    }

    /**
     * 初始化容器和线程池
     */
    private DrawableCache() {
        hashRefs = new Hashtable<String,MySoftRef>();
        q = new ReferenceQueue<Drawable>();
        executor = Executors.newFixedThreadPool(10);
    }

    /**
     * 取得缓存器实例
     */
    public static DrawableCache getInstance() {
        if (cache == null) {
            cache = new DrawableCache();
        }
        return cache;
    }

    /**
     * 以软引用的方式对一个Bitmap对象的实例进行引用并保存该引用
     */
    private void addCacheBitmap(Drawable db, String key) {
        cleanCache();// 每次加入Bitmap时清除垃圾引用
        MySoftRef ref = new MySoftRef(db, q, key);
        hashRefs.put(key, ref);
    }

    /**
     * 取得Bitmap，以《URL》名称为key
     */
    private Drawable getDrawableFromCache(String key, Context context) {
        Drawable db = null;

        if (hashRefs.containsKey(key)) {
            MySoftRef ref = (MySoftRef) hashRefs.get(key);
            db = (Drawable) ref.get();
        }
        return db;
    }

    private void cleanCache() {
        MySoftRef ref = null;
        while ((ref = (MySoftRef) q.poll()) != null) {
            hashRefs.remove(ref._key);
        }
    }

    /**
     * 清除Cache内的全部内容,可以随意调用
     */
    public void clearCache() {
        cleanCache();
        hashRefs.clear();
        System.gc();
        System.runFinalization();
    }

    /**
     * 请求图片的主方法！！！
     */
    public Drawable loadDrawable(final Context context, final String imageUrl, final Integer toSize,
                                 final Integer threadPriority, final ImageCallback imageCallback) {

        if (imageUrl == null)
            return null;

        // 缓存中是否有该Bitmap实例的软引用，如果有，从软引用中取得
        Drawable drawable = getDrawableFromCache(imageUrl, context);
        if (drawable != null && drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
            return drawable;
        }

        // 从URL中截取文件名
        String fileName = null;
        try {
            fileName = URLEncoder.encode(imageUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final File file;

        // 判断可用来存储的位置
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File imageDir = Environment.getExternalStorageDirectory();
            isExist(imageDir);
            file = new File(imageDir, fileName);
        } else
            file = new File(context.getCacheDir(), fileName);

        // 下载图片到手机上
        if (!file.isDirectory() && file.exists()) {
            drawable = getDrawable(file.getAbsolutePath(), toSize);
            if (drawable != null && drawable instanceof BitmapDrawable
                    && ((BitmapDrawable) drawable).getBitmap() != null) {
                addCacheBitmap(drawable, imageUrl);
                return drawable;
            }
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
            }
        };

        if (RequestList.containsKey(imageUrl)) {
            RequestList.get(imageUrl).add(handler);
        } else {
            RequestList.put(imageUrl, new ArrayList<Handler>());
            RequestList.get(imageUrl).add(handler);
            // 如果没有软引用，或者从软引用中得到的实例是null，则新起一个线程，并保存对这个图片的软引用
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (threadPriority != null) {
                        Thread.currentThread().setPriority(threadPriority.intValue());
                    }
                    Drawable drawable = loadImageFromUrl(context, imageUrl, file, toSize);
                    addCacheBitmap(drawable, imageUrl);
                    List<Handler> listHandler = RequestList.remove(imageUrl);
                    for (Handler callbackHandler : listHandler) {
                        Message message = callbackHandler.obtainMessage(0, drawable);
                        callbackHandler.sendMessage(message);
                    }
                    listHandler.clear();
                }
            });
        }

        return null;
    }

    /**
     * 下载并保存图片
     *
     * @param toSize
     */
    public Drawable loadImageFromUrl(Context context, String imageUrl, File file, Integer toSize) {
        Drawable drawable = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            HttpURLConnection conn = null;
            URL url = new URL(imageUrl);
            if (TextUtils.isEmpty(android.net.Proxy.getDefaultHost())) {
                Type type = Type.HTTP;
                SocketAddress sa = new InetSocketAddress(android.net.Proxy.getDefaultHost(),
                        android.net.Proxy.getDefaultPort());
                Proxy proxy = new Proxy(type, sa);
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.connect();
            int responseCode = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            int data = is.read();
            while (data != -1) {
                fos.write(data);
                data = is.read();
            }
            fos.flush();
            fos.close();
            is.close();
            drawable = getDrawable(file.toString(), toSize);
        } catch (Exception e) {
            // 出现任何异常都会删除文件
            file.delete();
        }
        return drawable;
    }

    private Drawable getDrawable(String sourceFileName, Integer toSize) {
        try {
            if (toSize == null) {
                return Drawable.createFromPath(sourceFileName);
            } else {
                Bitmap bitmap = BitmapConvert.resizeBitmap(sourceFileName, toSize);
                return new BitmapDrawable(bitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断文件夹是否存在,如果不存在则创建文件夹
     */
    public static void isExist(File imageDir) {
        File file = imageDir;
        if (!file.exists())
            file.mkdirs();
    }

    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, String imageUrl);
    }
    /**
     * 从网络中获取图片，以流的形式返回
     * @return
     */
    public static InputStream getImageViewInputStream(String URL_PATH) throws IOException {
        InputStream inputStream = null;
        URL url = new URL(URL_PATH);                    //服务器地址
        if (url != null) {
            //打开连接
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);//设置网络连接超时的时间为3秒
            httpURLConnection.setRequestMethod("GET");        //设置请求方法为GET
            httpURLConnection.setDoInput(true);                //打开输入流
            int responseCode = httpURLConnection.getResponseCode();    // 获取服务器响应值
            if (responseCode == HttpURLConnection.HTTP_OK) {        //正常连接
                inputStream = httpURLConnection.getInputStream();        //获取输入流
            }
        }
        return inputStream;
    }
}
