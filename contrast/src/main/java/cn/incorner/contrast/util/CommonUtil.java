package cn.incorner.contrast.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import cn.incorner.contrast.Config;

/**
 * 通用工具
 * 
 * @author yeshimin
 */
public class CommonUtil {

	/**
	 * 根据格式将时间戳转换成字符串
	 */
	public static String getStringTime(long timestamp, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String result = sdf.format(new Date(timestamp));
		return result;
	}

	/**
	 * 获取当前时间，默认格式 "yyyy-MM-dd HH:mm:ss"
	 */
	public static String getDefaultFormatCurrentTime() {
		return getStringTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 从Uri获取文件路径
	 */
	public static String getFilePathFromUri(final Context context, final Uri uri) {
		if (null == uri) {
			return null;
		}
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/**
	 * 保存bitmap图片到本地
	 */
	public static void saveBitmap(File file, Bitmap bitmap) {
		if (file == null || !file.exists() || bitmap == null) {
			return;
		}

		FileOutputStream fos = null;
		try {
			byte[] bImage = bitmap2Bytes(bitmap);
			fos = new FileOutputStream(file);
			fos.write(bImage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (fos == null) {
				return;
			}
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * bitmap to bytes[]
	 */
	private static byte[] bitmap2Bytes(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * md5
	 */
	public static final String md5(String content) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
				'E', 'F' };

		try {
			byte[] btInput = content.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 插入到系统图库，并更新
	 */
	public static void updateImageToMediaStore(Context context, File file) {
		if (context == null || file == null || !file.exists()) {
			return;
		}

		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), file.getName(), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
				+ file.getAbsolutePath())));
	}

	/**
	 * 将输入的年、月、日按照“yyyy-MM-dd”格式输出
	 */
	public static String getYYYYMMDDDate(int year, int month, int day) {
		String formattedMonth = month >= 10 ? String.valueOf(month) : "0" + month;
		return year + "-" + formattedMonth + "-" + day;
	}

	public static String getShortFormatDateString(String sDateTime) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat dfOutput = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		try {
			d = df.parse(sDateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (d == null) {
			return "";
		}
		return dfOutput.format(d);
	}

	/**
	 * 获取年份
	 */
	public static int getYear(String sDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		try {
			d = df.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (d == null) {
			return 1971;
		}
		return d.getYear() + 1900;
	}

	/**
	 * 获取月份
	 */
	public static int getMonth(String sDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		try {
			d = df.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (d == null) {
			return 0;
		}
		return d.getMonth();
	}

	/**
	 * 获取日期
	 */
	public static int getDay(String sDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		Date d = null;
		try {
			d = df.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (d == null) {
			return 1;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 组合地理信息 "{city} {district}"
	 */
	public static String getUserLocation() {
		return PrefUtil.getStringValue(Config.PREF_USER_CITY) + " "
				+ PrefUtil.getStringValue(Config.PREF_USER_DISTRICT);
	}
	
	private static final long ONE_MINUTE = 60000L;  
    private static final long ONE_HOUR = 3600000L;  
    private static final long ONE_WEEK = 604800000L;  
  
    private static final String ONE_SECOND_AGO = "刚刚";  
    private static final String ONE_MINUTE_AGO = "分钟前";  
    private static final String ONE_HOUR_AGO = "小时前"; 
    
	public static String compareToDate(String dateStr) { 
		Date date = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:m:s");
			date = format.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date != null) {
			long delta = new Date().getTime() - date.getTime();
			if (delta < 1L * ONE_MINUTE) {
				return ONE_SECOND_AGO;
			}
			if (delta < 45L * ONE_MINUTE) {
				long minutes = toMinutes(delta);
				return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
			}
			if (delta < 24L * ONE_HOUR) {
				long hours = toHours(delta);
				return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
			}
			if (delta < 12L * 4L * ONE_WEEK) {
				if (dateStr.length() > 11) {
					return dateStr.substring(5, 10);
				}
			} else {
				if (dateStr.length() > 11) {
					return dateStr.substring(2, 10);
				}
			}
		}else{
			return null;
		}
		return null;
    }  
  
    private static long toSeconds(long date) {  
        return date / 1000L;  
    }  
  
    private static long toMinutes(long date) {  
        return toSeconds(date) / 60L;  
    }  
  
    private static long toHours(long date) {  
        return toMinutes(date) / 60L;  
    }  
}
