package cn.incorner.contrast.data.entity;

/**
 * 发布对比度的实体类
 * 
 * @author yeshimin
 */
public class PostParagraphEntity {

	private String paragraphId;
	private String paragraphContent;
	private int createUserId;
	private String originName;
	private String originAuthor;
	private String tags;
	private String createTime;
	private String picName;
	private String paragraphReplyId;

	public String getParagraphId() {
		return paragraphId;
	}

	public void setParagraphId(String paragraphId) {
		this.paragraphId = paragraphId;
	}

	public String getParagraphContent() {
		return paragraphContent;
	}

	public void setParagraphContent(String paragraphContent) {
		this.paragraphContent = paragraphContent;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public String getOriginName() {
		return originName;
	}

	public void setOriginName(String originName) {
		this.originName = originName;
	}

	public String getOriginAuthor() {
		return originAuthor;
	}

	public void setOriginAuthor(String originAuthor) {
		this.originAuthor = originAuthor;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
	}

	public String getParagraphReplyId() {
		return paragraphReplyId;
	}

	public void setParagraphReplyId(String paragraphReplyId) {
		this.paragraphReplyId = paragraphReplyId;
	}

}
