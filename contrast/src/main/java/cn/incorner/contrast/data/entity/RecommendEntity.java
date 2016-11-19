package cn.incorner.contrast.data.entity;

/**
 *推荐实体类
 * 
 * @author yeshimin
 */
public class RecommendEntity {

	private String nickname;
	
	private String avatarName;
	
	private long userId;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

}
