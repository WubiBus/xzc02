package cn.incorner.contrast.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class ImageLoader {

	private LruCache<String, Bitmap> mCache;
	private static ImageLoader imageLoader ;
	private ExecutorService executor;
	private ImageLoader(Context mContxt) {
		super();
		executor = Executors.newFixedThreadPool(3);
		//将应用的八分之一作为图片缓存
		ActivityManager am=(ActivityManager)mContxt.getSystemService(Context.ACTIVITY_SERVICE);
		int maxSize = am.getMemoryClass()*1024*1024/8;
		mCache = new LruCache<String, Bitmap>(maxSize){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes()*value.getHeight();
			}
		};
	}
	public static ImageLoader getInstance(Context mContxt){
		if(imageLoader==null){
			imageLoader = new ImageLoader(mContxt);
		}
		return imageLoader;
	}
	

	public void putBitmapInMemey(String path,Bitmap bitmap){
		if(path==null)
			return;
		if(bitmap==null)
			return;
		if(getBitmapFromCache(path)==null){
			this.mCache.put(path, bitmap);
		}
	}
	
	public Bitmap getBitmapFromCache(String path){
		return mCache.get(path);
	}
	
	
	public  Bitmap loadBitmapInBackground(String path,int reqWidth,int reqHeight){
		//
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(path, options);
		//计算图片的缩放比例
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		
		Bitmap bitmap= BitmapFactory.decodeFile(path, options);
		return bitmap;
	}
	
	public void loadImage(final ImageView view,
			final String path,
			final int reqWidth,
			final int reqHeight,
			final onBitmapLoadedListener callback){
		
		final Handler mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					Bitmap bitmap = (Bitmap)msg.obj;
					callback.displayImage(view, bitmap);
					break;

				default:
					break;
				}
			}
			
		};
		
		Bitmap img = getBitmapFromCache(path);

		if (img != null) {
			callback.displayImage(view, img);
		} else {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					String key = view.getTag().toString();
					if (key.equals(path)) {
						Bitmap bitmap = loadBitmapInBackground(path, reqWidth,
								reqHeight);
						putBitmapInMemey(path, bitmap);

						Message msg = mHandler.obtainMessage(1);
						msg.obj = bitmap;
						mHandler.sendMessage(msg);
					}
				}
			});
		}
	}
	
	
	public int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	
	public interface onBitmapLoadedListener{
		public void displayImage(ImageView view,Bitmap bitmap);
	}
}
