package cn.incorner.contrast.page;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import cn.incorner.contrast.Constant;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.HorizontalScrollViewAdapter;
import cn.incorner.contrast.util.ADIWebUtils;
import cn.incorner.contrast.view.MyHorizontalScrollView;
import cn.incorner.contrast.view.MyHorizontalScrollView.OnItemClickListener;

/**
 * 选择照片
 * 
 * @author yangke
 */
public class PopWindowSelectCaremer extends Activity implements OnClickListener {
	private Button btn_take;
	private Button btn_pick;
	private Uri imageUri;
	private Intent intent;

	private MyHorizontalScrollView mHorizontalScrollView;
	private HorizontalScrollViewAdapter mAdapter;

	public static ArrayList<String> photosList = new ArrayList<String>();
	public static ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.popwindow_select_caremer);
		init();
		setParams();
		setlistener();
		
		mHorizontalScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHorizontalScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(
								this);
						if (photosList.size() == 0) {
							getPhotoThumbnail();
						} else {
							mAdapter = new HorizontalScrollViewAdapter(PopWindowSelectCaremer.this,
									photosList);
							// 设置适配器
							mHorizontalScrollView.initDatas(mAdapter);
						}
					}
				});

	}

	private void setParams() {
		android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
		p.width = (int) (Constant.SCREEN_WIDTH); //
		getWindow().setAttributes(p);
	}

	private void setlistener() {
		btn_take.setOnClickListener(this);
		btn_pick.setOnClickListener(this);

	}

	private void init() {
		btn_take = (Button) findViewById(R.id.btn_take);
		btn_pick = (Button) findViewById(R.id.btn_pick);
		mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.horizontalScrollView1);
		intent = new Intent();
		File temp = new File(Constant.PICTURE_TMP_PATH);
		if (!temp.exists()) {
			temp.mkdirs();
		}
		// 添加点击回调
		mHorizontalScrollView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onClick(View view, int position) {
				Intent intent = new Intent();
				intent.putExtra("position", position);
				setResult(3, intent);
				finish();
			}
		});

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_take:
			takePhoto();// 拍照
			break;
		case R.id.btn_pick:
			pickPhoto();
			break;
		default:
			break;
		}

	}

	private void pickPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(intent, 2);
	}

	private void takePhoto() {
		if (ADIWebUtils.getSDFreeSize() < 50) {
			ADIWebUtils.showToast(this, "内存剩余不足，请清理后使用");
			return;
		}
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		// imageUri = Uri.fromFile(new File(Constant.PICTURE_TMP_PATH));
		File tempFile = new File(Constant.PICTURE_TMP_PATH, getPhotoFileName());
		imageUri = Uri.fromFile(tempFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, 1);

	}

	// 使用系统当前日期加以调整作为照片的名称
	@SuppressLint("SimpleDateFormat")
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	// 实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			intent.setData(imageUri);
			setResult(1, intent);
		} else if (requestCode == 2 && data != null) {
			if (data.getData() != null) {
				intent.setData(data.getData());
				setResult(2, intent);
			}
		}
		finish();
	}

	private void getPhotoThumbnail() {
		photosList.clear();
		bitmapList.clear();
		// 查询的列
		String[] projection = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.BUCKET_ID, // 直接包含该图片文件的文件夹ID，防止在不同下的文件夹重名
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
				MediaStore.Images.Media.DISPLAY_NAME, // 图片文件名
				MediaStore.Images.Media.DATA, // 图片绝对路径
				"count(" + MediaStore.Images.Media._ID + ")"// 统计当前文件夹下共有多少张图片
		};
		// 这种写法是为了进行分组查询，详情可参考http://yelinsen.iteye.com/blog/836935
		String selection = " 0==0) group by bucket_display_name --(";

		ContentResolver cr = this.getContentResolver();
		// Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
		// selection,null, "");
		Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Images.Media.DATE_MODIFIED);
		cursor.moveToLast();
		while (cursor.moveToPrevious()) {
			String folderId = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
			String folder = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
			long fileId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
			String finaName = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			String size = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
			// if (!ADIWebUtils.isNvl(size)) {
			// if (Integer.parseInt(size) < 1024 * 50) {// 小于50K的过滤掉
			// Log.e("======MediaStore.Images.Media.SIZE======", size);
			// continue;
			// }
			// }

			int count = cursor.getInt(5);// 该文件夹下一共有多少张图片
			BitmapFactory.Options options = new BitmapFactory.Options();
			// Bitmap thumbnail = Thumbnails.getThumbnail(cr, fileId, Thumbnails.MICRO_KIND,
			// options);//获取指定图片缩略片
			// if(photosList.size()>30){//最多加载30张
			// break;
			// }
			photosList.add(path);
			// bitmapList.add(thumbnail);
		}
		if (null != cursor && !cursor.isClosed()) {
			cursor.close();
		}
		mAdapter = new HorizontalScrollViewAdapter(this, photosList);
		// 设置适配器
		mHorizontalScrollView.initDatas(mAdapter);
	}
}
