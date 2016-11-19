package cn.incorner.contrast.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 关注 实体类
 * 
 * @author yeshimin
 */
public class FollowingEntity implements Parcelable {

	private int score;
	private String companyName;
	private String createTime;
	private String latestPicName;
	private String jobTitle;
	private int followBefore;
	private String latestParagraph;
	private int userLevelId;
	private int isFriend;
	private String updateTime;
	private String nickname;
	private String groupIds;
	private int special;
	private String userSignature;
	private String avatarName;
	private int userId;

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getLatestPicName() {
		return latestPicName;
	}

	public void setLatestPicName(String latestPicName) {
		this.latestPicName = latestPicName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public int getFollowBefore() {
		return followBefore;
	}

	public void setFollowBefore(int followBefore) {
		this.followBefore = followBefore;
	}

	public String getLatestParagraph() {
		return latestParagraph;
	}

	public void setLatestParagraph(String latestParagraph) {
		this.latestParagraph = latestParagraph;
	}

	public int getUserLevelId() {
		return userLevelId;
	}

	public void setUserLevelId(int userLevelId) {
		this.userLevelId = userLevelId;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}

	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
	}

	public String getUserSignature() {
		return userSignature;
	}

	public void setUserSignature(String userSignature) {
		this.userSignature = userSignature;
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(score);
		dest.writeString(companyName);
		dest.writeString(createTime);
		dest.writeString(latestPicName);
		dest.writeString(jobTitle);
		dest.writeInt(followBefore);
		dest.writeString(latestParagraph);
		dest.writeInt(userLevelId);
		dest.writeInt(isFriend);
		dest.writeString(updateTime);
		dest.writeString(nickname);
		dest.writeString(groupIds);
		dest.writeInt(special);
		dest.writeString(userSignature);
		dest.writeString(avatarName);
		dest.writeInt(userId);
	}

	public final static Parcelable.Creator<FollowingEntity> CREATOR = new Creator<FollowingEntity>() {

		@Override
		public FollowingEntity[] newArray(int size) {
			return new FollowingEntity[size];
		}

		@Override
		public FollowingEntity createFromParcel(Parcel source) {
			FollowingEntity entity = new FollowingEntity();
			entity.score = source.readInt();
			entity.companyName = source.readString();
			entity.createTime = source.readString();
			entity.latestPicName = source.readString();
			entity.jobTitle = source.readString();
			entity.followBefore = source.readInt();
			entity.latestParagraph = source.readString();
			entity.userLevelId = source.readInt();
			entity.isFriend = source.readInt();
			entity.updateTime = source.readString();
			entity.nickname = source.readString();
			entity.groupIds = source.readString();
			entity.special = source.readInt();
			entity.userSignature = source.readString();
			entity.avatarName = source.readString();
			entity.userId = source.readInt();
			return entity;
		}
	};

}
