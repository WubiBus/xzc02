package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 话题列表接口响应实体类
 * 
 * @author yeshimin
 */
public class TopicResultEntity {

	private String status;
	private List<TopicEntity> topics;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<TopicEntity> getTopics() {
		return topics;
	}

	public void setTopics(List<TopicEntity> topics) {
		this.topics = topics;
	}

}
