package cn.incorner.contrast.data.entity;

/**
 * 注册接口结果 实体类
 * 
 * @author yeshimin
 */
public class RegisterResultEntity {

	private String status;
	private int userId;
	private String accessToken;
	private String tag;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
