package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 对比度结果列表 实体类
 * 
 * @author yeshimin
 */
public class ParagraphResultEntity {

	private String status;
	private List<ParagraphEntity> paragraphs;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ParagraphEntity> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<ParagraphEntity> paragraphs) {
		this.paragraphs = paragraphs;
	}

}
