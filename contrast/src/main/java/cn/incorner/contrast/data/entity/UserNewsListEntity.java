package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 用户消息接口响应 实体类
 * 
 * @author yeshimin
 */
public class UserNewsListEntity {

	private String status;
	private List<NewsInfoEntity> newsInfos;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<NewsInfoEntity> getNewsInfos() {
		return newsInfos;
	}

	public void setNewsInfos(List<NewsInfoEntity> newsInfos) {
		this.newsInfos = newsInfos;
	}

}
