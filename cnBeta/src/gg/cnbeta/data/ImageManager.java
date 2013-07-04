package gg.cnbeta.data;

import gg.cnbeta.activity.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/*
 * A singleton class to manager the images.
 * It takes care of loading images from web, saving them to local files, and
 * also manages a memory-cache for fast image loading.
 */
public class ImageManager {

    private static final int CACHE_SIZE = 100;
    private static final String PREFIX = "IMG_";

    private static ImageManager instance;

    /* memory-cache */
    private Map<String, Bitmap> cache;

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }
    private ImageManager() {
        cache = new HashMap<String, Bitmap>();
    }

    /*
     * Set imageView with the image with the given imageId.
     * This would be done asynchronously if the image requested is not available
     * in the memory-cache.
     */
    public void SetImageView(ImageView imageView, String imageId) {                
        Bitmap bm = loadImage(imageView.getContext(), imageId);
        if(bm != null) {
            imageView.setImageBitmap(bm);
            return;
        } else {
            imageView.setImageResource(R.drawable.icon);   // set to default icon
            AsyncSetImageView(imageView, imageId);
        }
    }

    private void AsyncSetImageView(final ImageView imageView, final String imageId) {
        Thread t = new Thread(){
            public void run(){          
                InputStream is = null;
                try {
                    URL url = new URL(Const.URL_CNBETA_IMAGE + imageId);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    is = conn.getInputStream();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                final Bitmap bm2 = BitmapFactory.decodeStream(is);
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
                if (bm2 == null) {
                    return;
                }
                saveImage(imageView.getContext(), imageId, bm2);
                imageView.post(new Runnable() {
                    @Override
                    public void run(){
                        imageView.setImageBitmap(bm2);
                    }
                });
            }
        };
        t.start();
    }

    private void addImageToCache(String picId, Bitmap bm) {
        if (cache.size() > CACHE_SIZE) {
            cache.clear();
        }
        cache.put(picId, bm);
    }

    private String getImageFileName(String imageId) {
        return PREFIX + imageId;
    }

    /* 
     * @return null in case error
     */
    private Bitmap loadImage(Context context, String imageId) {
        if (cache.containsKey(imageId)) {
            return cache.get(imageId);
        }
        InputStream is = null;
        try {
            is = context.openFileInput(getImageFileName(imageId));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Bitmap bm = BitmapFactory.decodeStream(is);
        try {
            is.close();
        } catch (IOException e) {
            // Ignore
        }
        if (bm == null) {
            return null;
        }
        addImageToCache(imageId, bm);
        return bm;
    }

    /* 
     * Save image to local file.
     * @return true if success
     */
    private boolean saveImage(Context context, String imageId, Bitmap bm) {
        addImageToCache(imageId, bm);
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
