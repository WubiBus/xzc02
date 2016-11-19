package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * 评论列表接口 响应
 * 
 * @author yeshimin
 */
public class CommentResultEntity {

	private String status;
	private List<ParagraphCommentEntity> comments;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ParagraphCommentEntity> getComments() {
		return comments;
	}

	public void setComments(List<ParagraphCommentEntity> comments) {
		this.comments = comments;
	}

}
