package cn.incorner.contrast.data.entity;

import java.util.List;

public class RecommendListEntity {
	
	private String status;
	private List<RecommendEntity> users;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<RecommendEntity> getUsers() {
		return users;
	}
	public void setUsers(List<RecommendEntity> users) {
		this.users = users;
	}
	

}
