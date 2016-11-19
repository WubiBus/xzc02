package cn.incorner.contrast.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Reminders;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ADIWebUtils {
	private static final String ZERO = "0";
	//	private static ProgressDialog progressDialog;
	private static ProgressDialog progressDialog;
	/**
	 * 弹出正在加载的对话框
	 */
	public static void showDialog(Context context,String msg) {
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(context,"",msg, true,
					false);
		}
	}

	/**
	 * 关闭dialog
	 */
	public static void closeDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	/**
	 * 弹出信息
	 * 
	 * @param context
	 * @param msg
	 */
	public static void showToast(final Context context, final String msg) {
		if (isNvl(msg)||isNvl(context)) {
			return;
		}
		// Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				Looper.loop();
			}
		}).start();
	}

	// sqlite 中获得的空字符串 有为null 并为 "null"(字符串非空) 的情况 防止
	public static String nvl(Object obj) {	
		return obj == null ? "" : (("null").equals(obj.toString()) == true ? ""
				: obj.toString());
	}

	/**
	 * 判断一个字符串是否为空 或者为null   是则返回默认值   否则返回本身
	 * @param obj
	 * @param def
	 * @return
	 */
	public static String nvl(Object obj, String def) {
		return obj == null ||obj.equals("null")||obj.equals("") ? def : obj.toString();
	}



	/**
	 * 判断object是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNvl(Object obj) {
		if (null == obj || obj.equals("")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	/**
	 * 将obj类型转化为int
	 * 
	 * @param obj
	 * @return
	 */
	public static int toInt(Object obj) {
		if (obj != null && !obj.equals("null")) {
			return nvl(obj).equals("") ? 0 : (new BigDecimal(obj.toString()))
					.intValue();
		}
		return 0;
	}


	public static String toIntString(Object obj) {
		if (obj != null && !obj.equals("null")) {
			return nvl(obj).equals("") ? ZERO : String.valueOf((new BigDecimal(
					obj.toString())).intValue());
		}
		return ZERO;
	}

	/**
	 * 将Object类型转化为double
	 * 
	 * @param obj
	 * @return
	 */
	public static Double toDouble(Object obj) {
		if (obj != null && !obj.equals("")) {
			return nvl(obj).equals("") ? 0.0 : Double.parseDouble(obj
					.toString());
		}
		return 0.0;
	}


	public static int per2Int(Object obj) {
		return obj == null ? 0 : Integer.parseInt(obj.toString().substring(0,
				obj.toString().length() - 1));
	}

	// 如果值小于10，则前面补0
	public static String pad(int c) {
		if (c >= 10) {
			return String.valueOf(c);
		} else {
			return ZERO + String.valueOf(c);
		}
	}

	/**
	 * String[] 转换为List<Map>形式
	 * 
	 * @param keys
	 * @param values
	 * @return List<Map>
	 */
	public static List<Map<String, Object>> singleGenerator(String[] keys,
			Object[] values) {
		List<Map<String, Object>> rtnList = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		rtnList.add(map);
		return rtnList;
	}

	/**
	 * 包装Map为List<Map>形式
	 * 
	 * @param obj
	 * @return List<Map>
	 */
	public static List<Map<String, Object>> wrapperList(Map<String, Object> obj) {
		List<Map<String, Object>> rtnList = new ArrayList<Map<String, Object>>();
		rtnList.add(obj);
		return rtnList;
	}

	/**
	 * wifi模块入口-网络连接判断
	 * 
	 * @param context
	 * @return false情况下不进入下一个模块
	 */
	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return Boolean.TRUE;
					}
				}
			}
		} catch (Exception e) {
			Log.d("error", e.toString());
		}
		return Boolean.FALSE;
	}

	/**
	 * 比较日期大小
	 * 
	 * @param sync
	 * @param last
	 * @return sync > last = true
	 */
	public static boolean compareDate(String sync, String last) {
		if (last == null || last.equals("")) {
			return true;
		}
		java.text.DateFormat df = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		java.util.Calendar c1 = java.util.Calendar.getInstance();
		java.util.Calendar c2 = java.util.Calendar.getInstance();
		try {
			c1.setTime(df.parse(sync));
			c2.setTime(df.parse(last));
		} catch (java.text.ParseException e) {
			return true;
		}
		int result = c1.compareTo(c2);
		if (result > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 取得系统时间
	 * 
	 * @return String
	 */
	public static String getCurrentTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return df.format(date);
	}

	// 取得当前系统时间
	public static String getDateTime() {
		String paramFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(System.currentTimeMillis()).toString();
	}

	// 记录计划日期或实际日期当天该客户的电话总时长（包括来电和回电）单位：秒
	public static long getTelCallDate(Context context, String telid,
			String longTime) {
		Cursor cursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, null);
		long visitCallTime = 0;
		Log.d("TEL---------", cursor.getCount() + "");
		if (cursor.moveToFirst()) {
			do {
				CallLog calls = new CallLog();
				// 呼叫类型
				// String type;
				// switch (Integer.parseInt(cursor.getString(cursor
				// .getColumnIndex(Calls.TYPE)))) {
				// case Calls.INCOMING_TYPE:
				// type = "呼入";
				// break;
				// case Calls.OUTGOING_TYPE:
				// type = "呼出";
				// break;
				// case Calls.MISSED_TYPE:
				// type = "未接";
				// break;
				// default:
				// type = "挂断";// 应该是挂断.根据我手机类型判断出的
				// break;
				// }
				SimpleDateFormat sfd = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = new Date(Long.parseLong(cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.DATE))));
				// 呼叫时间
				String time = sfd.format(date);
				// 电话
				String tel_number = cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.NUMBER));
				// 通话时间,单位:s
				String duration = cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.DURATION));
				// Log.d("TEL---------", getDate(longTime) + "  " + telid +
				// " ==呼叫时间== " + time + " tel_number= " + tel_number + " " +
				// duration);
				// 累积时间
				if (telid.equals(tel_number)
						&& time.contains(getDate(longTime))) {
					visitCallTime += Long.parseLong(duration);
				}
			} while (cursor.moveToNext());

		}
		// Log.d("TEL---visitCallTime-----", "all=" + visitCallTime);
		return visitCallTime; // 秒
	}

	// 取得本机号码
	public static String getLinePhone(Context context) {
		TelephonyManager phoneMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return phoneMgr.getLine1Number();
	}

	// 取得当前系统时间yyyy-mm-dd hh:mm
	public static String getDateTimeHHMM() {
		String paramFormat = "yyyy-MM-dd hh:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(System.currentTimeMillis()).toString();
	}
	// 取得当前系统时间yyyy-mm-dd 
	public static String getDateTimeYYmm() {
		String paramFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(System.currentTimeMillis()).toString();
	}

	public static String getDateTimeHHMM(long time) {
		String paramFormat = "HH:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(time).toString();
	}
	//得到当前时间的分钟
	public static String getDataTimeMM(long time){
		String paramFormat = "mm";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(time).toString();
	}
	//得到当前时间的小时
	public static String getDataTimeHH(long time){
		String paramFormat = "HH";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(time).toString();
	}
	//得到所有时间
	public static String getDataTimeALL(long time){
		String paramFormat = "yyyy-MM-dd HH:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(paramFormat);
		return sdf.format(time).toString();
	}

	// String返回yyyy-MM-dd
	public static String getDate(String sTime) {
		sTime = sTime.replace("年", "-").replace("月", "-");
		String sTimeByDay = null;
		if (sTime == null) {
			return null;
		}
		if (sTime.length() >= 10) {
			sTimeByDay = sTime.substring(0, 10);
		} else {
			sTimeByDay = getDateTime();
		}
		return sTimeByDay;
	}

	// 从Perference文件中取得信息（登录人员） Setting.PREF_USERNAME
	public static String getPrefString(Context context, String key, String def) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(key, def);
	}

	public static String getPrefString(Context context, String key) {
		return getPrefString(context, key, "");
	}

	// 信息保存到Perference文件中
	public static void savePrefString(Context context, String key) {
		savePrefString(context, key, "");
	}

	public static void savePrefString(Context context, String key, String def) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edit = settings.edit();
		edit.putString(key, def);
		edit.commit();
	}

	// 实现月份补0
	public static String getMonth(int month) {
		String mo = "";
		if (month < 9) {
			mo = "0" + (++month);
		} else {
			mo = (++month) + "";
		}
		return mo;
	}

	// 实现天补0
	public static String getDay(int day) {
		String mo = "";
		if (day < 10) {
			mo = "0" + (day);
		} else {
			mo = (day) + "";
		}
		return mo;
	}

	/**
	 * 获得当前时间==推荐
	 * 
	 * @return 2012-10-10 10:10
	 */
	public static String getTime() {
		Time time = new Time();
		time.setToNow();
		int year = time.year;
		int month = time.month;
		int day = time.monthDay;
		int minute = time.minute;
		int hour = time.hour;
		int sec = time.second;
		return year + "-" + getMonth(month) + "-" + getDay(day) + " "
		+ getDay(hour) + ":" + getDay(minute) + ":" + getDay(sec);

	}

	/**
	 * 返回当前年份
	 * @return
	 */
	public static int getCurrentYear(){
		return Calendar.getInstance().get(Calendar.YEAR);

	}

	/**
	 * 返回当前月份
	 * @return
	 */
	public static int getCurrentMonth(){
		return Calendar.getInstance().get(Calendar.MONTH);

	}
	/**
	 * 返回当前月份
	 * @return
	 */
	public static int getCurrentDay(){
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	}

	/**
	 * 根据当前时间获得 ID yyyyMMddHHmms
	 * 
	 * @return
	 */
	public static String getMID() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmms");
		return dateFormat.format(System.currentTimeMillis());
	}

	/**
	 * 类型转换为Date
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static Date string2Date(String format, String date)
			throws ParseException {
		Date rtDate = null;
		if (!("").equals(date)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			rtDate = dateFormat.parse(date);
		}
		return rtDate;
	}

	/**
	 * 通过传入的 List<Map<String, Object>> listMap 根据 该listMap 中的 Map 的子元素 分组
	 * 包装返回一个MAP
	 * 
	 * @param listMap
	 * @param str
	 * @return Map
	 */
	public static Map<String, Object> list2Map(
			List<Map<String, Object>> listMap, String str) {
		Map<String, Object> map = new HashMap<String, Object>(); // 初始化返回的MAP 集合
		List<String> _list = new ArrayList<String>(); // 返回的map 的key 的list 进行比对
		List<Map<String, Object>> list;
		for (Map<String, Object> m : listMap) { // 循环传入的listMap
			String key = ADIWebUtils.nvl(m.get(str)); // 获取listMap中第一个map的 key
			if (key != null) {
				if (_list.contains(key)) { // chck key 的_list 中是否存在
					list = (List<Map<String, Object>>) map.get(key);
					list.add(m); // 如果该key 已经存在就直接放进去
					// }
				} else {
					_list.add(key);
					list = new ArrayList<Map<String, Object>>();
					list.add(m);
				}
				map.put(key, list);
			}
		}
		return map;
	}

	/**
	 * 对List<map>中的数据进行排序
	 * 
	 * @param list
	 * @param orderby
	 *            排序字段
	 * @param desc
	 *            true(从大到小)|false
	 * @return
	 */
	public static List<Map<String, Object>> sortListByTime(
			List<Map<String, Object>> list, final String orderby, boolean desc,
			String format) {
		final SimpleDateFormat df = new SimpleDateFormat(format);
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				// 取出操作时间
				int ret = 0;
				String dt1 = nvl(o1.get(orderby));
				String dt2 = nvl(o2.get(orderby));
				if (dt1.equals("") || dt2.equals("")) {
					return ret;
				}
				try {
					ret = df.parse(dt1).compareTo(df.parse(dt2));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				return ret;
			}
		});
		if (desc) {
			Collections.reverse(list);
		}
		return list;
	}

	/**
	 * 将service 下载下来的 true/false 转化为 0 1
	 */
	public static String boolean2num(Object obj) {
		return obj == null ? ""
				: (("true").equals(obj.toString()) == true ? "1" : (("false")
						.equals(obj.toString()) == true ? "0" : obj.toString()));

	}

	// 数字转Boolean
	public static String num2boolean(String obj) {
		if ("1".equals(obj) || obj.equalsIgnoreCase("true")) {
			return "true";
		} else {
			return "false";
		}

	}

	/**
	 * 正则验证是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		return isNum.matches();
	}

	/**
	 * 正则验证是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean regexIs(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher isNum = pattern.matcher(str);
		return isNum.matches();
	}

	/**
	 * 缩小图片大小 推荐(节约内存)
	 * 
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap resizeImage(String path, int width) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;// 不加载bitmap到内存中
		BitmapFactory.decodeFile(path, options);
		int outWidth = options.outWidth;
		int outHeight = options.outHeight;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inSampleSize = 1;
		if (outWidth != 0 && outHeight != 0 && width != 0) {
			int sampleSize = outWidth / width;
			if (!(sampleSize < 1)) {
				options.inSampleSize = sampleSize;
			}
		}
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * drawableToBitmap 类型转化
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	@SuppressLint("NewApi")
	public Bitmap stringtoBitmap(String string) { // 将字符串转换成Bitmap类型
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	/**
	 * 保存图片
	 * 
	 * @param bitmap
	 * @param p
	 *            save路径
	 * @throws FileNotFoundException
	 */
	public static void saveThePicture(Bitmap bitmap, String p) throws Exception {
		File file = new File(p);

		FileOutputStream fos = new FileOutputStream(file);
		// bitmap.
		if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
			fos.flush();
			fos.close();
		}

	}

	/**
	 * 图片路径转化为 Bitmap
	 * 
	 * @param path
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap convert2Bitmap(String path, int w, int h) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 设置为ture只获取图片大小
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// 返回为空
		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.f, scaleHeight = 0.f;
		if (width > w || height > h) {
			// 缩放
			scaleWidth = ((float) width) / w;
			scaleHeight = ((float) height) / h;
		}
		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int) scale;
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(path, opts));
		return Bitmap.createScaledBitmap(weak.get(), w, h, true);
	}

	/**
	 * copy 文件到 指定的目录下的方法
	 * 
	 * @param con
	 * @param strOutFileName
	 * @param name
	 * @throws IOException
	 */
	public static void copyDataT2TSD(Context con, String strOutFileName,
			String strInFileName) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		myInput = new FileInputStream(strInFileName);
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}
		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	/**
	 * copy Assets目录下的文件到 指定的目录下的方法
	 * 
	 * @param con
	 * @param strOutFileName
	 * @param name
	 * @throws IOException
	 */
	public static void copyBigDataToSD(Context con, String strOutFileName,
			String name) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		myInput = con.getAssets().open(name);
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	/**
	 * 修改日历活动
	 * 
	 * @param conRes
	 *            调用日历活动的上下文
	 * @param id
	 *            时间ID
	 * @param cv
	 *            事件内容
	 * @param cr
	 *            提醒内容
	 */
	@SuppressLint("NewApi")
	public static void UpdateCalendar(ContentResolver conRes, long id,
			ContentValues cv, ContentValues cr) {
		String[] wheArg = { id + "" };
		if (cv != null) {
			String where1 = CalendarContract.Events._ID + "= ? ";
			conRes.update(CalendarContract.Events.CONTENT_URI, cv, where1,
					wheArg);
		}
		if (cr != null) {
			String where2 = Reminders.EVENT_ID + "= ? ";
			conRes.update(Reminders.CONTENT_URI, cr, where2, wheArg);
		}
	}

	/**
	 ** 
	 * 新增日历活动 返回 eventID
	 * 
	 * @param st
	 *            开始时间
	 * @param dt
	 *            结束时间
	 * @param title
	 *            标题
	 * @param desc
	 *            说明
	 * @param loca
	 *            地点
	 * @param i
	 *            提前分钟默认为0
	 * @throws ParseException
	 */
	@SuppressLint("NewApi")
	public static long InsertCalendar(ContentResolver conRes, String st,
			String title, String desc, String loca, Integer i) throws Exception {
		Uri uri = CalendarContract.Events.CONTENT_URI; // 获取URI 位置

		// 提醒事件
		long date = Date2Long(st, "yyyy-MM-dd HH:mm");
		ContentValues cv = new ContentValues();
		cv.put(CalendarContract.Events.EVENT_TIMEZONE, "GMT + 8");
		cv.put(CalendarContract.Events.DTSTART, date);
		cv.put(CalendarContract.Events.DTEND, date);
		cv.put(CalendarContract.Events.TITLE, title); // 主题
		cv.put(CalendarContract.Events.DESCRIPTION, desc); // 描述
		cv.put(CalendarContract.Events.EVENT_LOCATION, loca); // 地点
		cv.put(CalendarContract.Events.CALENDAR_ID, 1);
		Uri ci = conRes.insert(uri, cv);
		long eventID = Long.parseLong(ci.getLastPathSegment());
		// 给时间添加提醒
		ContentValues values = new ContentValues();
		values.put(Reminders.MINUTES, i);
		values.put(Reminders.EVENT_ID, eventID);
		values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		conRes.insert(Reminders.CONTENT_URI, values);

		return eventID;
	}

	/**
	 * 删除指定ID 的日历活动及提醒
	 * 
	 * @param id
	 */
	@SuppressLint("NewApi")
	public static void DeleteCalendar(ContentResolver conRes, String id) {

		String[] wheArg = { id };
		String where1 = CalendarContract.Events._ID + "= ? ";
		conRes.delete(CalendarContract.Events.CONTENT_URI, where1, wheArg);

		String where2 = Reminders.EVENT_ID + "= ? ";
		conRes.delete(Reminders.CONTENT_URI, where2, wheArg);

	}

	/**
	 * 传入String 类型的date 转化为时间戳
	 * 
	 * @param date
	 *            String 日期
	 * @param format
	 *            格式类型
	 * @return Long time
	 * @throws ParseException
	 */
	public static long Date2Long(String date, String format)
			throws ParseException {
		SimpleDateFormat _format = new SimpleDateFormat(format);// 设置格式
		Date _date = _format.parse(date);
		return _date.getTime();

	}

	// 检查版本信息
	public static int getVerCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo("com.tsm", 0).versionCode;
		} catch (Exception e) {
			Log.e("getVerCode", e.getMessage());
		}
		return verCode;
	}

	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo("com.tsm", 0).versionName;
		} catch (Exception e) {
			Log.e("getVerName", e.getMessage());
		}
		return verName;
	}

	public static List<Map<String, Object>> nvlList(
			List<Map<String, Object>> list, String[] isNvl) {
		List<Map<String, Object>> _list = new ArrayList<Map<String, Object>>();
		if (list != null) {
			for (Map<String, Object> m : list) {
				for (String obj : isNvl) {

					m.put(obj, ADIWebUtils.nvl(m.get(obj)));
				}
				_list.add(m);
			}
			list.clear();
			list.addAll(_list);
		}

		return list;
	}

	/**
	 * Bitmap 转化String
	 * 
	 * @param bitmap
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String Bitmap2String(Bitmap bitmap) {
		if (bitmap == null) {
			return "错误图片";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
		bitmap.compress(CompressFormat.PNG, 100, baos);
		byte[] appicon = baos.toByteArray();// 转为byte数组
		return Base64.encodeToString(appicon, Base64.DEFAULT);

	}

	/**
	 * String 转化 Bitmap
	 * 
	 * @param String
	 * @return Bitmap
	 */
	@SuppressLint("NewApi")
	public static Bitmap string2Bitmap(String string) {
		// 将字符串转换成Bitmap类型
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray = string.getBytes();
			// bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
		} catch (Exception e) {
			Log.e("string2Bitmap error", e.getMessage());
		}
		return bitmap;
	}

	/**
	 * 判断是否为图片格式
	 * 
	 * @param path
	 * @return
	 */
	public static boolean CheckPicture(String path) {
		Bitmap bitmap = ADIWebUtils.resizeImage(path, 50);
		if (bitmap == null) {
			return false;
		}
		return true;

	}

	/**
	 * 判断SD是否存在
	 * 
	 * @return
	 */
	public static boolean ExistSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * SD卡剩余空间 MB
	 * 
	 * @return
	 */
	public static long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
	}

	/**
	 * 修改日期格式
	 */
	public static List<Map<String, Object>> date2List(
			List<Map<String, Object>> list, String[] dates) {
		List<Map<String, Object>> _list = new ArrayList<Map<String, Object>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (list != null) {
			for (Map<String, Object> m : list) {
				for (String _s : dates) {
					if (ADIWebUtils.nvl(m.get(_s)).equals("")) {
						m.put(_s, ADIWebUtils.nvl(m.get(_s)));
					} else {
						Date date = new Date(Long.parseLong(ADIWebUtils.nvl(m
								.get(_s))));
						m.put(_s, sdf.format(date));
					}
				}
				_list.add(m);
			}
			list.clear();
			list.addAll(_list);
		}

		return list;
	}

	public static String getDataNormal(int type, boolean from, int num,
			String dfmat) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dfmat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		if (type == Calendar.DAY_OF_WEEK) {
			calendar.add(Calendar.WEEK_OF_MONTH, num);
			calendar.add(Calendar.DAY_OF_WEEK, 1);
		} else if (type == Calendar.DAY_OF_MONTH) {
			calendar.add(Calendar.MONTH, num);
		}
		if (from) {
			calendar.set(type, calendar.getActualMinimum(type));
		} else {
			calendar.set(type, calendar.getActualMaximum(type));
		}
		if (type == Calendar.DAY_OF_WEEK) {
			calendar.add(Calendar.DAY_OF_WEEK, 1);
		}
		return dateFormat.format(calendar.getTime());
	}

	public static boolean compareDate(String first, String last, String dfString) {
		if (last == null || last.equals("") || first == null
				|| first.equals("")) {
			return false;
		}
		java.text.DateFormat df = new java.text.SimpleDateFormat(dfString);
		java.util.Calendar c1 = java.util.Calendar.getInstance();
		java.util.Calendar c2 = java.util.Calendar.getInstance();
		try {
			c1.setTime(df.parse(first));
			c2.setTime(df.parse(last));
		} catch (java.text.ParseException e) {
			return false;
		}
		int i = c1.compareTo(c2);
		if (i != 1) {
			return true;
		}
		return false;
	}

	// 取本机通讯录
	public static List<Map<String, Object>> getPhoneContracts(Context mContext) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ContentResolver resolver = mContext.getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, null, null,
				null, null); // 传入正确的uri
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				int nameIndex = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME); // 获取联系人name
				String name = phoneCursor.getString(nameIndex);
				String phoneNumber = phoneCursor.getString(phoneCursor
						.getColumnIndex(Phone.NUMBER)); // 获取联系人number
				if (TextUtils.isEmpty(phoneNumber)) {
					continue;
				}
				map.put("NAME", name);
				map.put("PhoneNumber", phoneNumber);
				list.add(map);
			}
			phoneCursor.close();
		}
		return list;
	}

	// 接下来看获取sim卡的方法，sim卡的uri有两种可能content://icc/adn与content://sim/adn
	// （一般情况下是第一种） "content://icc/adn"
	public static List<Map<String, Object>> getSimContracts(Context mContext,
			String strcontext) {
		// 读取SIM卡手机号,有两种可能:content://icc/adn与content://sim/adn
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ContentResolver resolver = mContext.getContentResolver();
		Uri uri = Uri.parse(strcontext);
		Cursor phoneCursor = resolver.query(uri, null, null, null, null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				String name = phoneCursor.getString(phoneCursor
						.getColumnIndex("name"));
				String phoneNumber = phoneCursor.getString(phoneCursor
						.getColumnIndex("number"));
				if (TextUtils.isEmpty(phoneNumber)) {
					continue;
				}
				map.put("NAME", name);
				map.put("PhoneNumber", phoneNumber);
				list.add(map);
			}
			phoneCursor.close();
		}
		return list;
	}

	public static Class<?> generateClass(Class<?> clazz) {
		// if (clazz == null) {
		// return null;
		// }
		if (clazz.getCanonicalName().endsWith("_")) {
			return clazz;
		}
		String name = clazz.getCanonicalName() + "_";
		try {
			Class<?> result = Class.forName(name);
			return result;
		} catch (ClassNotFoundException e) {
			new RuntimeException("Cannot find class for" + name, e);
		}
		return null;
	}

	/**
	 * 将array list 转为 list<MAP> //只适用于 array 返回 KEY VALUE 的listMap
	 * 
	 * @param _key
	 * @param _value
	 * @return
	 */
	public static List<Map<String, Object>> getArrayListMap(Context mContext,
			int _key, int _value) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String[] key = null, value = null;
		key = mContext.getResources().getStringArray(_key);
		value = mContext.getResources().getStringArray(_value);
		for (int i = 0; i < key.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("KEY", key[i]);
			map.put("VALUE", value[i]);
			list.add(map);
		}
		return list;
	}

	/**
	 * 排序
	 * 
	 * @param list
	 * @param orderby
	 *            排序的字段
	 * @param desc
	 *            default 升序 false 降序 true
	 * @return
	 */
	public static List<Map<String, Object>> listSort(
			List<Map<String, Object>> list, final String orderby, boolean desc) {
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				int ret = 0;
				String dt1 = nvl(o1.get(orderby));
				String dt2 = nvl(o2.get(orderby));
				if (dt1.equals("") || dt2.equals("")) {
					return ret;
				}
				return ret = dt1.compareTo(dt2);
			}
		});
		if (desc) {
			Collections.reverse(list);
		}
		return list;
	}

	/**
	 * 数字排序
	 * 
	 * @param list
	 * @param orderby
	 *            排序的字段
	 * @param desc
	 *            default 升序 false 降序 true
	 * @return
	 */
	public static List<Map<String, Object>> listNumericSort(
			List<Map<String, Object>> list, final String orderby, boolean desc) {
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Double dt1 = toDouble(o1.get(orderby));
				Double dt2 = toDouble(o2.get(orderby));
				return dt1.compareTo(dt2);
			}
		});
		if (desc) {
			Collections.reverse(list);
		}
		return list;
	}

	public static List<Map<String, Object>> listFilterByMap(
			List<Map<String, Object>> list, Map<String, Object> mapFilter,
			String all) {
		if (list == null) {
			return null;
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		retList.addAll(list);
		for (Map<String, Object> map : list) {
			for (String key : map.keySet()) {
				for (String _key : mapFilter.keySet()) {
					if (key.equals(_key)
							&& !nvl(mapFilter.get(_key)).equals(all)) {
						if (!ADIWebUtils.nvl(map.get(key)).equals(
								ADIWebUtils.nvl(mapFilter.get(_key)))) {
							retList.remove(map);
						}
					}
				}
			}
		}
		return retList;
	}

	public static List<Map<String, Object>> listFilterByKey(
			List<Map<String, Object>> list, String key, String value) {
		if (list == null) {
			return null;
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : list) {
			if (nvl(map.get(key)).equals(value)) {
				retList.add(map);
			}

		}
		return retList;
	}

	public static Map<Integer, Object> getArrayIntgerMap(Context mContext,
			int _value) {
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		String[] value = mContext.getResources().getStringArray(_value);
		int i = 0;
		for (String s : value) {
			map.put(i, s);
			i++;
		}
		return map;
	}

	public static void ClearViewProperties(Object[] orig) {
		for (int i = 0; i < orig.length; i++) {
			if (orig[i] instanceof TextView) {
				((TextView) orig[i]).setText("");
			} else if (orig[i] instanceof EditText) {
				((EditText) orig[i]).setText("");
			}
		}
	}

	/**
	 * 比较日期大小
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isDateBefore(String date1) {
		if (date1 == null || date1.equals("")) {
			return false;
		}

		if (date1.compareTo(getDateTime()) < 0) {
			return false;
		} else {
			return true;
		}

	}

	public static String ReadTxtFile(String strFilePath) {
		String path = strFilePath;
		String content = ""; // 文件内容字符串
		try {
			InputStream instream = ADIWebUtils.class
					.getResourceAsStream(strFilePath);
			if (instream != null) {
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);
				String line;
				// 分行读取
				while ((line = buffreader.readLine()) != null) {
					content += line + "\n";
				}
				instream.close();
			}
		} catch (java.io.FileNotFoundException e) {
			Log.d("TestFile", "The File doesn't not exist.");
		} catch (IOException e) {
			Log.d("TestFile", e.getMessage());
		}

		return content;
	}

	public static void setTextWithLine(TextView textView,String  text){
		textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
		textView.setText(text);
		textView.setTextColor(Color.BLACK);
	}

	/**
	 * 将时间戳转换为"yyyy-MM-dd hh:mm"时间格式
	 * @param time
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getLongTimeToDate(String time) {
		if(time==null||time.equals("")){
			return "";
		}
		long timeLong=0;
		try {
			timeLong= Long.parseLong(time);
		} catch (Exception e) {
			// TODO: handle exception
		}
		Date d = new Date(timeLong*1000l);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		return sf.format(d);
	}
	/**
	 * 将时间戳转换为"yyyy-MM-dd hh:mm"时间格式
	 * @param time
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getLongTimeToTime(String time) {
		if(time==null){
			return "";
		}
		long timeLong = Long.parseLong(time);
		Date d = new Date(timeLong*1000l);

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sf.format(d);
	}
	/**
	 * 判断是否是手机号码
	 * @param str   输入的手机号
	 * @return   true是  false不是
	 */
	public static boolean isPhone(String str){
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(str);
		boolean b = m.matches();
		return b;

	}
	/**
	 * 判断邮箱是否合法
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){  
		if (null==email || "".equals(email)) return false;	
		//Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配  
		Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配  
		Matcher m = p.matcher(email);  
		return m.matches();  
	}
	/**
	 * 判断邮政编码
	 * @param paramString
	 * @return
	 */
	public static boolean isZipNO(String zipString){
		String str = "^[1-9][0-9]{5}$";
		return Pattern.compile(str).matcher(zipString).matches();
	}
	/**
	 * 判断输入是否有空格
	 * @param paramString
	 * @return
	 */
	public static boolean isNull(String zipString){
		String str = "^[^ ]{6,16}$";
		return Pattern.compile(str).matcher(zipString).matches();
	}
	/**
	 * 只能有数字 和字母组成的密码
	 * @param password
	 * @return
	 */
	public static boolean isPassword(String password){
		String str = "[0-9A-Za-z]{6,12}";
		return Pattern.compile(str).matcher(password).matches();
	}


	/** 
	 * 根据原图和变长绘制圆形图片 
	 *  
	 * @param source 
	 * @param min 
	 * @return 
	 */  
	public static  Bitmap createCircleImage(Bitmap source, int min)  
	{  
		final Paint paint = new Paint();  
		paint.setAntiAlias(true);  
		Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);  
		/** 
		 * 产生一个同样大小的画布 
		 */  
		Canvas canvas = new Canvas(target);  
		/** 
		 * 首先绘制圆形 
		 */  
		canvas.drawCircle(min / 2, min / 2, min / 2, paint);  
		/** 
		 * 使用SRC_IN 
		 */  
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
		/** 
		 * 绘制图片 
		 */  
		canvas.drawBitmap(source, 0, 0, paint);  
		return target;  
	}

	public static long toLong(String obj) {
		long l =Long.parseLong(obj);
		return l;
	}
	/**
	 * 转换图片成圆形
	 * @param bitmap 传入对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width,height, Config.ARGB_4444);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
		final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
	 * double保留两位有效数字
	 * @param value
	 * @return
	 */
	public static String doubleSave2(double value){
		DecimalFormat   df   =new  DecimalFormat("#0.00"); 
		return df.format(value);
	}

	
	
	
	/** 
     * 获得指定文件的byte数组 
     */  
    public  static byte[] getBytesFromeFile(String filePath){  
        byte[] buffer = null;  
        try {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);  
            byte[] b = new byte[1000];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return buffer;  
    }
    /**
     * 设置大图
     */
    public static void setBigImageView(ImageView iv,int id,Context context){
    	BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		
		opt.inSampleSize=2;
		

		InputStream is = context.getResources().openRawResource(id);
		Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
		Log.e("=========大小========", bm.getRowBytes()*bm.getHeight()/1024.0/1024+"M");
	    iv.setImageBitmap(bm);
    }
   
}
