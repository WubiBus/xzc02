package cn.incorner.contrast.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.text.TextUtils;

/**
 * 数据缓存工具类
 * 
 * @since 2016-03-16 15:30
 * @version 2
 * @author yeshimin | ysm@yeshimin.com
 */
public class DataCacheUtil {

	/**
	 * 获取存储的key
	 */
	public static final String getStoreKey(String url, LinkedHashMap<String, Object> param) {
		if (TextUtils.isEmpty(url)) {
			return "";
		}

		StringBuffer sbKey = new StringBuffer();
		sbKey.append(url);

		if (param != null) {
			StringBuffer sbParam = new StringBuffer();
			Iterator<String> it = param.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String value = String.valueOf(param.get(key));
				sbParam.append("&" + key + "=" + value);
			}
			if (sbParam.length() != 0) {
				sbParam.replace(0, 1, "?");
				sbKey.append(sbParam);
			}
		}

		return md5(sbKey.toString());
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
	 * 清除String数据
	 */
	public static final void clearStringData(String path, String key) {
		File file = new File(path, key);

		if (file != null && file.exists()) {
			file.delete();
		}
	}

	/**
	 * 读取String数据
	 */
	public static final String getStringData(String path, String key) {
		File file = new File(path, key);
		BufferedReader reader = null;
		StringBuffer sbString = new StringBuffer();
		if (file.isFile()) {
			try {
				reader = new BufferedReader(new FileReader(file));
				String sLine;
				while ((sLine = reader.readLine()) != null) {
					sbString.append(sLine);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sbString.toString();
	}

	/**
	 * 保存String数据
	 */
	public static final void putStringData(final String path, final String key, final String value) {
		final String TAG = "DataCacheUtil";
		new Thread(new Runnable() {
			@Override
			public void run() {
				File file = new File(path, key);
				if (!file.exists()) {
					new File(path).mkdirs();
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new FileWriter(file), 1024);
					writer.write(value);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (writer != null) {
						try {
							writer.flush();
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

}
