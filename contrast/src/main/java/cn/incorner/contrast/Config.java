package cn.incorner.contrast;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 配置类（固定信息）
 * 
 * @author yeshimin
 */
public class Config {

	public static final String ENCODING = "UTF-8";
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final int LOAD_COUNT = 20;
	public static final int LOAD_COUNT2 = 5;
	// 描述，字体大小、字数限制等
	public static final int DESC_TEXT_SIZE_LARGE_SP = 19;
	public static final int DESC_TEXT_SIZE_MEDIUM_SP = 16;
	public static final int DESC_TEXT_SIZE_SMALL_SP = 13;
	public static final int DESC_TEXT_LENGTH_LARGE_LIMIT = 20;
	public static final int DESC_TEXT_LENGTH_MEDIUM_LIMIT = 60;
	public static final int DESC_TEXT_LENGTH_MAX_LIMIT = 100;
	public static final int DESC_COMMENT_TEXT_LENGTH_MAX_LIMIT = 40;
	// 性别
	public static final int MALE = 0;
	public static final int FEMALE = 1;
	// banner自动轮播时间间隔
	public static final long BANNER_INTERVAL = 5000;

	/**
	 * 正则表达式
	 */

	// emoji <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public static final String EMOJI_HREF = new String(Character.toChars(0x261E));
	public static final String EMOJI_CAMERA = new String(Character.toChars(0x1F4F7));
	public static final String EMOJI_WARN = new String(Character.toChars(0x26A0));
	public static final String EMOJI_18_FORBID = new String(Character.toChars(0x1F51E));
	public static final String EMOJI_HORN = new String(Character.toChars(0x1F4E2));
	public static final String EMOJI_PAPER_CLIP = new String(Character.toChars(0x1F4CE));
	// emoji >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// 添加附注
	public static final String PATTERN_ADD_COMMENT = "*";
	// 添加超链接
	public static final String PATTERN_ADD_HREF = EMOJI_HREF;
	// 原创图片
	public static final String PATTERN_ORIGINAL = EMOJI_CAMERA + "原创图片";
	// 前方高能慎点
	public static final String PATTERN_HIGH_ENERGY = EMOJI_WARN + "前方高能慎点";
	// 18禁预警
	public static final String PATTERN_18_FORBID = EMOJI_18_FORBID + "18禁";
	// 活动
	public static final String PATTERN_HORN = EMOJI_HORN;
	// 本周最佳
	public static final String PATTERN_THISWEEK_GRATE = "本周最佳";
	// 回形针
	public static final String PATTERN_PAPER_CLIP = EMOJI_PAPER_CLIP;
	// 标记当前作品为原创
	public static final String PATTERN_MARK_ORIGINAL = "/原创/";
	// 无需掩藏
	public static final String PATTERN_NO_HIDDEN = "/无需遮挡/";
	// 需掩藏
	public static final String PATTERN_HIDDEN = "/需遮挡/";
	// 回答问题
	public static final String PATTERN_QUESTION = "<\\?>.*<\\?>";
	// 徐志超写的 回答问题
	public static final String XZC_PATTERN_QUESTION = "<\\?>";
	// 回答问题 的 <?><?>
	public static final String ANSWER_QUESTION = "<?><?>";
	// 提问
	public static final String ASK = "提问:";
	// 标签
	public static final String PATTERN_TAG = "#(.+?):@\\(0\\)";
	// 标签，样式2
	public static final String PATTERN_TAG_2 = "#.*";
	// 标签，样式3
	public static final String PATTERN_TAG_3 = "(#.+?):@\\(0\\)";
	// 描述，带注释
	public static final String PATTERN_DESC_WITH_COMMENT = "([.\\s\\S]*)\\*([.\\s\\S]*)";
	// 描述，带网址
	public static final String PATTERN_DESC_WITH_URL = "([.\\s\\S]*)" + PATTERN_ADD_HREF
			+ "([.\\s\\S]*)";
	// 描述，注释中有网址
	public static final String PATTERN_DESC_WITH_COMMENT_URL = "([.\\s\\S]*)\\*([.\\s\\S]*)"
			+ PATTERN_ADD_HREF + "([.\\s\\S]*)";
	// 描述，注释中有网址，* 和 @ 前后位置不一样
	public static final String PATTERN_DESC_WITH_COMMENT_URL_2 = "([.\\s\\S]*)" + PATTERN_ADD_HREF
			+ "([.\\s\\S]*)\\*([.\\s\\S]*)";
	// 网址
	public static final String PATTERN_URL = "[.\\s\\S]*(https?://.+)";
	// 颜色索引
	public static final String PATTERN_COLOR_INDEX = "([0-9]+)";
	// 颜色rgb
	public static final String PATTERN_COLOR_RGB = "([0-9]{1,3});([0-9]{1,3});([0-9]{1,3})";
	// 手机号
	public static final String PATTERN_PHONE = "^1[0-9]{10}$";
	// 邮箱
	public static final String PATTERN_EMAIL = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$";
	// 描述，up down
	public static final String PATTERN_DESC_UP_DOWN = "^\\[up\\]([.\\s\\S]*)\\[up\\]\\s+\\[down\\]([.\\s\\S]*)\\[down\\]\\s+\\[colorA\\]([.\\s\\S]*)\\[colorA\\]\\s+\\[colorB\\]([.\\s\\S]*)\\[colorB\\]$";
	// 描述，left right
	public static final String PATTERN_DESC_LEFT_RIGHT = "^\\[left\\]([.\\s\\S]*)\\[left\\]\\s+\\[right\\]([.\\s\\S]*)\\[right\\]\\s+\\[colorA\\]([.\\s\\S]*)\\[colorA\\]\\s+\\[colorB\\]([.\\s\\S]*)\\[colorB\\]$";

	/**
	 * 标签
	 */
	public static final String SELECTED_TAG = "有趣";
	public static final String SELECTED_RICHINCONTENT = "有料";
	public static final String ALL_FRIEND = "关注";
	public static final String[] ARR_TAG = new String[] { "", "", "真相大揭秘", "萌翻全场", "撞脸大赛", "长姿势",
			"万万没想到", "时光机", "在路上", "看完想shi", "内牛满面", "神奇的脑洞", "康帅博", "pos机" };

	/**
	 * Pref
	 */
	public static final String PREF_IS_LOGINED = "isLogined";
	public static final String PREF_ACCESS_TOKEN = "accessToken";
	public static final String PREF_USER_ID = "userId";
	public static final String PREF_USERNAME = "username";
	public static final String PREF_NICKNAME = "nickname";
	public static final String PREF_USER_SIGNATURE = "userSignature";
	public static final String PREF_JOB_TITLE = "jobTitle";
	public static final String PREF_BIRTHDAY = "birthday";
	public static final String PREF_USER_SEX = "userSex";
	public static final String PREF_EMAIL = "email";
	public static final String PREF_FOLLOW_COUNT = "followCount";
	public static final String PREF_FRIEND_COUNT = "friendCount";
	public static final String PREF_FOLLOWER_COUNT = "followerCount";
	public static final String PREF_PARAGRAPH_COUNT = "paragraphCount";
	public static final String PREF_AVATAR_NAME = "avatarName";
	public static final String PREF_SCORE = "score";
	public static final String PREF_IS_ANONYMOUS = "isAnonymous";
	public static final String PREF_USER_CITY = "cityName";
	public static final String PREF_USER_DISTRICT = "district";
	// 第三方相关
	public static final String PREF_THIRD_PARTY_UID = "thirdPartyUid";
	public static final String PREF_THIRD_PARTY_AVATAR = "thirdPartyAvatar";
	public static final String PREF_THIRD_PARTY_TYPE = "thirdPartyType";
	public static final int THIRD_PARTY_TYPE_QQ = 1;
	public static final int THIRD_PARTY_TYPE_WEIXIN = 2;
	public static final int THIRD_PARTY_TYPE_WEIBO = 3;
	// 发布，还原上一次编辑的数据 相关
	public static final String PREF_LAST_ORIENTATION = "lastOrientation";
	public static final String PREF_LAST_IMAGE_PATH_1 = "lastImagePath1";
	public static final String PREF_LAST_IMAGE_PATH_2 = "lastImagePath2";
	public static final String PREF_LAST_DESC_1 = "lastDesc1";
	public static final String PREF_LAST_DESC_2 = "lastDesc2";
	public static final String PREF_LAST_TAG = "lastTag";
	public static final String PREF_SAVE_TAG = "saveTag";
	// 服务器地址及端口
	public static final String SERVER_AND_PORT = "http://api.honeyshare.cn:8888/"; // 正式的
	// public static final String SERVER_AND_PORT = "http://121.40.127.231:8888/"; // 正式的
	// public static final String SERVER_AND_PORT = "http://115.29.246.3:8888/"; // test
	// API基地址
	public static final String API_BASE_PATH = SERVER_AND_PORT + "contrast/";
	// 头像基地址
	public static final String HEAD_BASE_PATH = SERVER_AND_PORT + "avatar_image/";
	// 对比度图片基地址
	public static final String CONTRAST_BASE_PATH = SERVER_AND_PORT + "paragraph_b/";
	// 对比度图片基地址（小图）
	public static final String CONTRAST_SMALL_BASE_PATH = SERVER_AND_PORT + "paragraph_s/";
	// banner图片基地址
	public static final String BANNER_BASE_PATH = SERVER_AND_PORT + "banner/";
	// 下载图片的目录
	public static final String DOWNLOAD_IMAGE_FILE_PATH = getDownloadImageFilePath(BaseApplication.context);
	// json数据缓存路径
	public static final String JSON_CACHE_PATH = getJsonCachePath(BaseApplication.context);
	public static final String MESSAGE_CACHE_KEY = "messages";

	/**
	 * 接口地址
	 */
	// 匿名注册
	public static final String PATH_ANONYMOUS_REG = getApiFullPath("anonymousreg");
	// 获取自己的列表
	public static final String PATH_GET_PARAGRAPH_LIST = getApiFullPath("getparagraphlist");
	// 根据tag获取列表
	public static final String PATH_GET_PARAGRAPH_BY_TAG = getApiFullPath("getparagraphbytag");
	//我的关注(作品列表)
	public static final String PATH_GET_FRIEND = getApiFullPath("getfriendparagraph");
	// 喜欢
	public static final String PATH_LIKE_PARAGRAPH = getApiFullPath("likeparagraph");
	// 讨厌
	public static final String PATH_HATE_PARAGRAPH = getApiFullPath("hateparagraph");
	// 举报
	public static final String PATH_COMPLAINT_PARAGRAPH = getApiFullPath("complaintparagraph");
	// 根据给的ID获取对比度
	public static final String PATH_GET_PARAGRAPHS = getApiFullPath("getparagraphs");
	// 添加评论
	public static final String PATH_ADD_COMMENT = getApiFullPath("addcomment");
	// 加载评论
	public static final String PATH_GET_COMMENTS_BY_PARAGRAPH_REPLY_IDS = getApiFullPath("getcommentsbyparagraphreplyids");
	// 获取喜欢的对比度
	public static final String PATH_GET_LIKE_PARAGRAPH = getApiFullPath("getlikeparagraph");
	// 获取关注列表
	public static final String PATH_FOLLOW_LIST = getApiFullPath("followlist");
	// 关注我的人的列表
	public static final String PATH_FOLLOWER_LIST = getApiFullPath("followerlist");
	// 获取用户信息
	public static final String PATH_GET_INFO = getApiFullPath("getinfo");
	// 设置用户信息
	public static final String PATH_SET_INFO = getApiFullPath("setinfo");
	// 设置头像
	public static final String PATH_SET_AVATAR_IMAGE = getApiFullPath("setavatarimage");
	// 根据用户ID获取对比度列表
	public static final String PATH_GET_PARAGRAPH_BY_USER_ID = getApiFullPath("getparagraphbyuserid");
	//根据seriseId获得用户评论版本
	public static final String PATH_GET_MULIT_VERSION = getApiFullPath("getParagraphBySeriesId");
	// 注销
	public static final String PATH_LOGOUT = getApiFullPath("logout");
	// 登录
	public static final String PATH_GET_ACCESS = getApiFullPath("getaccess");
	// 注册
	public static final String PATH_REGISTER = getApiFullPath("register");
	// 忘记密码
	public static final String PATH_GET_FINDPWD = getApiFullPath("findpassword");
	// 使用第三方平台注册
	public static final String PATH_THIRD_PARTY_REGISTER = getApiFullPath("thirdpartyregister");
	// 使用第三方平台登录
	public static final String PATH_THIRD_PARTY_LOGIN = getApiFullPath("thirdpartylogin");
	// 获取推荐用户信息
	public static final String PATH_GET_RECOMMEND_USER = getApiFullPath("getRecommendUser");
	// 发布对比度
	// 添加关注
	public static final String PATH_ADD_FOLLOW = getApiFullPath("addfollow");
	// 取消关注
	public static final String PATH_CANCEL_FOLLOW = getApiFullPath("cancelfollow");
	// 手机号验证（发送短信验证码）
	public static final String PATH_CHECK_PHONE_NUMBER = getApiFullPath("checkphonenumber");
	// 获取用户消息
	public static final String PATH_GET_USER_NEWS = getApiFullPath("getusernews");
	// 发布对比度
	public static final String PATH_UPLOAD_PARAGRAPHS = getApiFullPath("uploadparagraphs");
	// 上传对比度图片
	public static final String PATH_UPLOAD_PARAGRAPH_PIC = getApiFullPath("uploadparagraphpic");
	// 上传对比度（附图片）
	public static final String PATH_UPLOAD_CONTRAST = getApiFullPath("uploadContrast");
	// 更新密码
	public static final String PATH_SET_PASSWORD = getApiFullPath("setpassword");
	// 删除对比度
	public static final String PATH_DELETE_PARAGRAPH_BY_PARAGRAPH_ID = getApiFullPath("delparagraphbyparagraphid");
	// 获取所有话题列表
	public static final String PATH_GET_TOPIC = getApiFullPath("gettopic");
	// 查找用户
	public static final String PATH_FIND_USER = getApiFullPath("finduser");
	// 获取横幅数据
	public static final String PATH_GET_BANNER = getApiFullPath("getbanner");
	// 分享接口
	public static final String PATH_SHARE = "http://api.honeyshare.cn/index.html?id=";
	// 新的话题分享接口
	public static final String PATH_TOP_SHARE = "http://api.honeyshare.cn/webshare/?tags=";
	// 发送验证码 （上面已经有该接口 “手机号验证（发送短信验证码）”）
	public static final String PATH_GET_SENDCODE = getApiFullPath("checkphonenumber");
	// 获取用户私信列表
	public static final String PATH_GET_USER_PRIVATE = getApiFullPath("getmessagebyuserid");
	//用户发私信
	public static final String PATH_ADD_USER_PRIVATE = getApiFullPath("addcomment");
	//个推注册clientid
	public static final String PATH_ADD_CLIENTIDE = getApiFullPath("binddevicetoken");
	

	// 接口
	private static final String getApiFullPath(String subPath) {
		return API_BASE_PATH + subPath;
	}

	// 头像
	public static final String getHeadFullPath(String subPath) {
		return HEAD_BASE_PATH + subPath + ".jpg";
	}

	// 多个评论版本
	public static final String getMulitVersionPath(String subPath) {
		return API_BASE_PATH + subPath;
	}

	// contrast
	public static final String getContrastFullPath(String subPath) {
		return CONTRAST_BASE_PATH + subPath + ".jpg";
	}

	// contrast缩略图
	public static final String getContrastSmallFullPath(String subPath) {
		return CONTRAST_SMALL_BASE_PATH + subPath + ".jpg";
	}

	// banner
	public static final String getBannerFullPath(String subPath) {
		return BANNER_BASE_PATH + subPath + ".jpg";
	}

	public static final String getGender(int gender) {
		if (gender == MALE) {
			return "男";
		} else {
			return "女";
		}
	}

	public static final int getGenderCode(String gender) {
		if ("男".equals(gender)) {
			return MALE;
		} else {
			return FEMALE;
		}
	}

	public static final String getDownloadImageFilePath(Context context) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
					.getAbsolutePath() + File.separator + "Contrast";
		}
		return "";
	}

	private static final String getJsonCachePath(Context context) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return context.getExternalCacheDir().getAbsolutePath() + File.separator + "json";
		}
		return context.getCacheDir().getAbsolutePath() + File.separator + "json";
	}

}
