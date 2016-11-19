package cn.incorner.contrast.data.entity;

/**
 * banner 实体类
 * 
 * @author yeshimin
 */
public class BannerEntity implements Comparable<BannerEntity> {

	private String paragraphReplyId;
	private String picName;
	private int sortId;
	private String tagName;
	private String webUrl;

	public String getparagraphReplyId() {
		return paragraphReplyId;
	}

	public void setparagraphReplyId(String paragraphReplyId) {
		this.paragraphReplyId = paragraphReplyId;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	@Override
	public int compareTo(BannerEntity another) {
		if (this.sortId > another.sortId) {
			return 1;
		} else if (this.sortId < another.sortId) {
			return -1;
		}
		return 0;
	}

}
