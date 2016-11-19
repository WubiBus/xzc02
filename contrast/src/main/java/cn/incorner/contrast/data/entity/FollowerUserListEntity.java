package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 关注我的人的列表 实体类
 * 
 * @author yeshimin
 */
public class FollowerUserListEntity {

	private String status;
	private List<FollowerEntity> followers;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<FollowerEntity> getFollowers() {
		return followers;
	}

	public void setFollowers(List<FollowerEntity> followers) {
		this.followers = followers;
	}

}
