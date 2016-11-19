package cn.incorner.contrast.data.entity;

/**
 * 用户消息 实体类
 * 
 * @author yeshimin
 */
public class NewsInfoEntity {

	public static final int INFO_TYPE_LIKE = 11;//赞
	public static final int INFO_TYPE_DISLIKE = 12;// 讨厌
	public static final int INFO_TYPE_COMMENT = 13;//评论
	public static final int INFO_TYPE_NEW_WORK= 14;//好友新作品通知
	public static final int INFO_TYPE_PRIVATEMESSAGE = 15;//留言
	public static final int INFO_TYPE_BE_CONCERNED = 16;//被关注消息
	public static final int INFO_TYPE_BE_SELECTED = 17;//标为有趣
	public static final int INFO_TYPE_BE_RECHINCONTENT = 18;//标为有料

	private String createTime;
	private String shareContent;
	private String fromUserNickname;
	private int fromUserLevelId;
	private String fromUserAvatarName;
	private int fromUserId;
	//新增地址
	private String location;
	private int newsId;
	private int infoType;
	private InfoEntity info;


	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getShareContent() {
		return shareContent;
	}

	public void setShareContent(String shareContent) {
		this.shareContent = shareContent;
	}

	public String getFromUserNickname() {
		return fromUserNickname;
	}

	public void setFromUserNickname(String fromUserNickname) {
		this.fromUserNickname = fromUserNickname;
	}

	public int getFromUserLevelId() {
		return fromUserLevelId;
	}

	public void setFromUserLevelId(int fromUserLevelId) {
		this.fromUserLevelId = fromUserLevelId;
	}

	public String getFromUserAvatarName() {
		return fromUserAvatarName;
	}

	public void setFromUserAvatarName(String fromUserAvatarName) {
		this.fromUserAvatarName = fromUserAvatarName;
	}

	public int getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(int fromUserId) {
		this.fromUserId = fromUserId;
	}

	//新增
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNewsId() {
		return newsId;
	}

	public void setNewsId(int newsId) {
		this.newsId = newsId;
	}

	public int getInfoType() {
		return infoType;
	}

	public void setInfoType(int infoType) {
		this.infoType = infoType;
	}

	public InfoEntity getInfo() {
		return info;
	}

	public void setInfo(InfoEntity info) {
		this.info = info;
	}

}
