package cn.incorner.contrast.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.R.integer;

/**
 * Created with IntelliJ IDEA.
 * User: bobo
 * Date: 13-5-13
 * Time: 下午1:38
 */
public class StringUtils {
    //斜杠
    public static final String SPLIT_XG = "/";
    //反斜杠
    public static final String SPLIT_FXG = "\\";
    //分号
    public static final String SPLIT_FH = ";";
    //冒号
    public static final String SPLIT_MH = ":";
    //横杠
    public static final String SPLIT_HG = " -- ";
    //竖杠
    public static final String SPLIT_SG = "\\|";
    //换行符
    public static final String SPLIT_HHF = "\n";
    //逗号
    public static final String SPLIT_COMMA = ",";

    /**
     * 分列字符串
     *
     * @param s
     * @return
     */
    public static String[] split(String s) {
        return s.split(SPLIT_XG);
    }

    /**
     * 分列字符串
     *
     * @param s
     * @return
     */
    public static String[] split(String s, String regular) {
        return s.split(regular);
    }

    /**
     * 字符串为空
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return !isNotEmpty(s);
    }

    /**
     * 字符串不为空
     *
     * @param s
     * @return
     */
    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }

    
    /**
     * 判断是否为null或空值
     *
     * @param str String
     * @return true or false
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    
    
    /**
     * 去除字符中间空格
     * @param s
     * @return
     */
    public static String repalceEmptyStr(String s){
   		  return s.replaceAll(" ", "");
    }

    /**
     * 字符串转成List集合
     *
     * @param texts
     * @return
     */
    public static List<String> stringToList(String[] texts) {
        List<String> ss = new ArrayList<String>();
        for (int i = 0; i < texts.length; i++) {
            ss.add(texts[i]);
        }
        return ss;
    }

    /**
     * 在原来的数组中添加第一条字符串
     * @param olds
     * @param s
     * @return
     */
    public static String[] addFirstValue(String[] olds, String s){
        String[] news = new String[olds.length + 1];
        news[0] = s;
        for (int i = 0; i < olds.length; i++) {
            news[i + 1] = olds[i];
        }
        return news;
    }

    /**
     * 使double不用科学计数法显示
     * @param d
     * @return
     */
    public static String double2String(Double d) {
        if(null==d)
            return "";
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return decimalFormat.format(d);
    }

    /**
     * 获取UUID值
     * @return
     */
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

    public static boolean isExistForList (List<String> stringList, String str) {
        for (String s : stringList) {
            if(s.equals(str)){
                return true;
            }
        }
        return false;
    }

    public static <T> boolean contains( final T[] array, final T v ) {
        for ( final T e : array ) {
            if (e == v || v != null && v.equals(e))
                return true;
        }
        return false;
    }
    
    @SuppressWarnings("unused")
    public static String string2Demical(String str) {
    	// 将传进数字反转
    	String reverseStr = new StringBuilder(str).reverse().toString();
    	String strTemp = "";
    	for (int i=0; i<reverseStr.length(); i++) {
    	if (i*3+3 > reverseStr.length()) {
    	strTemp += reverseStr.substring(i*3, reverseStr.length());
    	break;
    	}
    	strTemp += reverseStr.substring(i*3, i*3+3)+",";
    	}
    	// 将 【789,456,】 中最后一个【,】去除
    	if (strTemp.endsWith(",")) {
    	strTemp = strTemp.substring(0, strTemp.length()-1);
    	}
    	// 将数字重新反转
    	String resultStr = new StringBuilder(strTemp).reverse().toString();
    	return resultStr;
    }

    /**
     * 逗号分隔显示数字
     * @param str
     * @return
     */
    public static String split3Number(String str) {
        if(isEmpty(str))
            return "";
        String[]  ss = str.split("\\.");
        str = ss[0];
        int i = ss[0].length();
        while (i > 3) {
            str = str.substring(0, i - 3) + "," + str.substring(i - 3);
            i -= 3;
        }
        if(ss.length > 1){
            str = str + "." + ss[1];
        }
//        if (str.length() > 5) {
//			str = str.substring(0, str.length() - 5) + "万"+ str.substring((str.length() - 5) , str.length());
//		}
        return str;
    }
    
	public static boolean compareAge(int minAge, int maxAge,String certificateNum) {
		int realAge = 0;
		String yy1 = certificateNum.substring(6,10);          //出生的年份
        String mm1 = certificateNum.substring(10,12);       //出生的月份
        String dd1 = certificateNum.substring(12,14);         //出生的日期
        String birthday = yy1.concat("-").concat(mm1).concat("-").concat(dd1);
        try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
			String s1 = sdf.format(date);
			Date today = sdf.parse(s1);    
			/*parse方法可以自己查api，他就是将文档（此处是String）格式转成sdf（自己定义的日期格式）。*/   
			Date birth = sdf.parse(birthday);
		    realAge= today.getYear() - birth.getYear();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        if (realAge < minAge || realAge > maxAge) {
        	return false;
		}else {
			return true;
		}
	}


    //    检测是否有emoji表情
    /*
     * @param source
    * @return
    * */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }
    
}
