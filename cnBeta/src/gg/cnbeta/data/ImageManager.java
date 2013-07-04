package gg.cnbeta.data;

import gg.cnbeta.activity.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

/*
 * A singleton class to manager the images.
 * It takes care of loading images from web, saving them to local files, and
 * also manages a memory-cache for fast image loading.
 */
public class ImageManager {

    private static final String PREFIX = "IMG_";
    
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private static ImageManager mInstance;

    /* memory-cache */
    private Map<String, Bitmap> mCache;
    
    private BlockingQueue<Runnable> mQueue;
    private ThreadPoolExecutor mExecutor;
    
    private Handler mHandler;
    
    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        mInstance = new ImageManager();
    }

    public static ImageManager getInstance() {
        return mInstance;
    }
    @SuppressLint("HandlerLeak")
    private ImageManager() {
        mCache = new WeakHashMap<String, Bitmap>();
        mQueue = new LinkedBlockingQueue<Runnable>();
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mQueue);
        
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                LoadImageTask task = (LoadImageTask) inputMessage.obj;
                ImageView imageView = task.getImageView();
                Bitmap bitmap = task.getBitmap();
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }

    /*
     * Set imageView with the image with the given imageId.
     * This would be done asynchronously if the image requested is not available
     * in the memory-cache.
     */
    public void SetImageView(ImageView imageView, String imageId) {                
        Bitmap bm = mCache.get(imageId);
        if(bm != null) {
            imageView.setImageBitmap(bm);
            return;
        } else {
            imageView.setImageResource(R.drawable.icon);   // set to default icon
            mExecutor.execute(new LoadImageTask(imageView, imageId, this));
        }
    }
    
    class LoadImageTask implements Runnable {
        
        private WeakReference<ImageView> mImageView;
        private String mImageId;
        private ImageManager mImageManager;
        private Bitmap mBitmap;
        
        public LoadImageTask(ImageView imageView, String imageId, ImageManager imageManager) {
            mImageView = new WeakReference<ImageView>(imageView);
            mImageId = imageId;
            mImageManager = imageManager;
        }
        
        /* 
         * Might return null as WeakReference is used here
         */
        public ImageView getImageView() {
            return mImageView.get();
        }
        
        public Bitmap getBitmap() {
            return mBitmap;
        }
        
        public String getImageId() {
            return mImageId;
        }

        @Override
        public void run() {
            if (loadFromFile() || loadFromUrl()) {
                mImageManager.handleTaskDone(this);
            }
        }
        
        private boolean loadFromUrl() {
            InputStream is = null;
            try {
                URL url = new URL(Const.URL_CNBETA_IMAGE + mImageId);
                URLConnection conn = url.openConnection();
                conn.connect();
                is = conn.getInputStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            mBitmap = BitmapFactory.decodeStream(is);
            try {
                is.close();
            } catch (IOException e) {
                // Ignore
            }
            return mBitmap != null;
        }
        
        private boolean loadFromFile() {
            InputStream is = null;
            ImageView imageView = getImageView();
            if (imageView == null) {
                return false;
            }
            try {
                is = imageView.getContext().openFileInput(getImageFileName(mImageId));
            } catch (FileNotFoundException e) {
                return false;
            }
            mBitmap = BitmapFactory.decodeStream(is);
            try {
                is.close();
            } catch (IOException e) {
                // Ignore
            }
            return mBitmap != null;
        }
    }
    
    private void handleTaskDone(LoadImageTask task) {
        Message message = mHandler.obtainMessage(0, task);
        message.sendToTarget();
        
        ImageView imageView = task.getImageView();
        Bitmap bitmap = task.getBitmap();
        if (imageView == null || bitmap == null) {
            return;
        }
        saveImage(imageView.getContext(), task.getImageId(), bitmap);
    }   

    private String getImageFileName(String imageId) {
        return PREFIX + imageId;
    }

    /* 
     * Save image to local file.
     * @return true if success
     */
    private boolean saveImage(Context context, String imageId, Bitmap bm) {
        mCache.put(imageId, bm);
        OutputStream os = null;
        try {
            os = context.openFileOutput(getImageFileName(imageId), 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        boolean success = bm.compress(Bitmap.CompressFormat.PNG, 90, os);
        try {
            os.close();
        } catch (IOException e) {
            // Ignore
        }
        return success;
    }
}
