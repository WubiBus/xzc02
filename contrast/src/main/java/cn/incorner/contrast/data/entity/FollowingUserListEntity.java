package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 关注的人列表 实体类
 * 
 * @author yeshimin
 */
public class FollowingUserListEntity {

	private String status;
	private List<FollowingEntity> followings;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<FollowingEntity> getFollowings() {
		return followings;
	}

	public void setFollowings(List<FollowingEntity> followings) {
		this.followings = followings;
	}

}
