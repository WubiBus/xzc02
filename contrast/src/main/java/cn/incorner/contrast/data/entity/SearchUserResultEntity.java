package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 搜索用户的结果 实体类
 * 
 * @author yeshimin
 */
public class SearchUserResultEntity {

	private String status;
	private List<SearchUserEntity> userInfos;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<SearchUserEntity> getUserInfos() {
		return userInfos;
	}

	public void setUserInfos(List<SearchUserEntity> userInfos) {
		this.userInfos = userInfos;
	}

}
