package cn.incorner.contrast.data.entity;

/**
 * 匿名注册接口返回数据 实体类
 * 
 * @author yeshimin
 */
public class AnonymousRegEntity {

	private String status;
	private String accessToken;
	private int userId;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}
