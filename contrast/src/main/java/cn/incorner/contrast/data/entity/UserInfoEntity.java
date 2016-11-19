package cn.incorner.contrast.data.entity;

/**
 * 用户信息 实体类
 * 
 * @author yeshimin
 */
public class UserInfoEntity {

	private int status;
	private String nickname;
	private String userSignature;
	private String jobTitle;
	private String birthday;
	private int userSex;
	private String email;
	private int followCount;
	private int friendCount;
	private int followerCount;
	private int followBefore;
	private int paragraphCount;
	private String avatarName;
	private int score;
	private int isAnonymous;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUserSignature() {
		return userSignature;
	}

	public void setUserSignature(String userSignature) {
		this.userSignature = userSignature;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getUserSex() {
		return userSex;
	}

	public void setUserSex(int userSex) {
		this.userSex = userSex;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getFollowCount() {
		return followCount;
	}

	public void setFollowCount(int followCount) {
		this.followCount = followCount;
	}

	public int getFriendCount() {
		return friendCount;
	}

	public void setFriendCount(int friendCount) {
		this.friendCount = friendCount;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public int getFollowBefore() {
		return followBefore;
	}

	public void setFollowBefore(int followBefore) {
		this.followBefore = followBefore;
	}

	public int getParagraphCount() {
		return paragraphCount;
	}

	public void setParagraphCount(int paragraphCount) {
		this.paragraphCount = paragraphCount;
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getIsAnonymous() {
		return isAnonymous;
	}

	public void setIsAnonymous(int isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

}
