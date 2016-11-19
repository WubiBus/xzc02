package cn.incorner.contrast.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.xutils.DbManager;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * 对比度 实体类
 * 
 * @author yeshimin
 */
@Table(name = "paragraph")
public class ParagraphEntity implements Parcelable {

	@Column(name = "paragraph_id", isId = true)
	private String paragraphId;
	@Column(name = "paragraph_content")
	private String paragraphContent;
	@Column(name = "content_attribute")
	private String contentAttribute;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "user_nickname")
	private String userNickname;
	@Column(name = "user_avatar_name")
	private String userAvatarName;
	@Column(name = "create_user_id")
	private int createUserId;
	@Column(name = "original")
	private int original;
	@Column(name = "origin_name")
	private String originName;
	@Column(name = "origin_author")
	private String originAuthor;
	@Column(name = "origin_link")
	private String originLink;
	@Column(name = "note_id")
	private String noteId;
	@Column(name = "original_editing_sort")
	private int originalEditingSort;
	@Column(name = "tags")
	private String tags;
	@Column(name = "content_id")
	private String contentId;
	@Column(name = "is_chapter_title")
	private int isChapterTitle;
	@Column(name = "create_time")
	private String createTime;
	@Column(name = "update_time")
	private String updateTime;
	@Column(name = "pic_name")
	private String picName;
	@Column(name = "like_count")
	private int likeCount;
	@Column(name = "complaint_count")
	private int complaintCount;
	@Column(name = "comment_count")
	private int commentCount;
	@Column(name = "contrast_show_count")
	private int contrastShowCount;
	@Column(name = "paragraph_reply_id")
	private String paragraphReplyId;
	@Column(name = "like_state")
	private int likeState;
	@Column(name = "complaint_state")
	private int complaintState;
	@Column(name = "ower_follower_count")
	private int owerFollowerCount;
	@Column(name = "follow_state")
	private int followState;
	@Column(name = "contrast_select")
	private int contrastSelect;
	@Column(name = "paragraph_score")
	private int paragraphScore;

	@Column(name = "seriesCount")
	private int seriesCount;

	@Column(name = "seriesId")
	private String seriesId;

	private List<ParagraphCommentEntity> comments;

	// 拓展字段
	public boolean hasSeened = false;
	public String url1 = "";
	public String url2 = "";
	//记录滚动条 X 方向的位置
	public int functionScrollLocation;

	public List<ParagraphCommentEntity> getChildren(DbManager db) throws DbException {
		return db.selector(ParagraphCommentEntity.class)
				.where("ex_paragraph_id", "=", this.paragraphId).findAll();
	}

	public int getSeriesCount() {
		return seriesCount;
	}

	public void setSeriesCount(int seriesCount) {
		this.seriesCount = seriesCount;
	}

	public String getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}

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

	public String getContentAttribute() {
		return contentAttribute;
	}

	public void setContentAttribute(String contentAttribute) {
		this.contentAttribute = contentAttribute;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserNickname() {
		return userNickname;
	}

	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}

	public String getUserAvatarName() {
		return userAvatarName;
	}

	public void setUserAvatarName(String userAvatarName) {
		this.userAvatarName = userAvatarName;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public int getOriginal() {
		return original;
	}

	public void setOriginal(int original) {
		this.original = original;
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

	public String getOriginLink() {
		return originLink;
	}

	public void setOriginLink(String originLink) {
		this.originLink = originLink;
	}

	public String getNoteId() {
		return noteId;
	}

	public void setNoteId(String noteId) {
		this.noteId = noteId;
	}

	public int getOriginalEditingSort() {
		return originalEditingSort;
	}

	public void setOriginalEditingSort(int originalEditingSort) {
		this.originalEditingSort = originalEditingSort;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public int getIsChapterTitle() {
		return isChapterTitle;
	}

	public void setIsChapterTitle(int isChapterTitle) {
		this.isChapterTitle = isChapterTitle;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getComplaintCount() {
		return complaintCount;
	}

	public void setComplaintCount(int complaintCount) {
		this.complaintCount = complaintCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getContrastShowCount() {
		return contrastShowCount;
	}

	public void setContrastShowCount(int contrastShowCount) {
		this.contrastShowCount = contrastShowCount;
	}

	public String getParagraphReplyId() {
		return paragraphReplyId;
	}

	public void setParagraphReplyId(String paragraphReplyId) {
		this.paragraphReplyId = paragraphReplyId;
	}

	public int getLikeState() {
		return likeState;
	}

	public void setLikeState(int likeState) {
		this.likeState = likeState;
	}

	public int getComplaintState() {
		return complaintState;
	}

	public void setComplaintState(int complaintState) {
		this.complaintState = complaintState;
	}

	public int getOwerFollowerCount() {
		return owerFollowerCount;
	}

	public void setOwerFollowerCount(int owerFollowerCount) {
		this.owerFollowerCount = owerFollowerCount;
	}

	public int getFollowState() {
		return followState;
	}

	public void setFollowState(int followState) {
		this.followState = followState;
	}

	public int getContrastSelect() {
		return contrastSelect;
	}

	public void setContrastSelect(int contrastSelect) {
		this.contrastSelect = contrastSelect;
	}

	public int getParagraphScore() {
		return paragraphScore;
	}

	public void setParagraphScore(int paragraphScore) {
		this.paragraphScore = paragraphScore;
	}

	public List<ParagraphCommentEntity> getComments() {
		return comments;
	}

	public void setComments(List<ParagraphCommentEntity> comments) {
		this.comments = comments;
	}


	public ParagraphEntity() {
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.paragraphId);
		dest.writeString(this.paragraphContent);
		dest.writeString(this.contentAttribute);
		dest.writeInt(this.userId);
		dest.writeString(this.userNickname);
		dest.writeString(this.userAvatarName);
		dest.writeInt(this.createUserId);
		dest.writeInt(this.original);
		dest.writeString(this.originName);
		dest.writeString(this.originAuthor);
		dest.writeString(this.originLink);
		dest.writeString(this.noteId);
		dest.writeInt(this.originalEditingSort);
		dest.writeString(this.tags);
		dest.writeString(this.contentId);
		dest.writeInt(this.isChapterTitle);
		dest.writeString(this.createTime);
		dest.writeString(this.updateTime);
		dest.writeString(this.picName);
		dest.writeInt(this.likeCount);
		dest.writeInt(this.complaintCount);
		dest.writeInt(this.commentCount);
		dest.writeInt(this.contrastShowCount);
		dest.writeString(this.paragraphReplyId);
		dest.writeInt(this.likeState);
		dest.writeInt(this.complaintState);
		dest.writeInt(this.owerFollowerCount);
		dest.writeInt(this.followState);
		dest.writeInt(this.contrastSelect);
		dest.writeInt(this.paragraphScore);
		dest.writeInt(this.seriesCount);
		dest.writeString(this.seriesId);
		dest.writeTypedList(comments);
		dest.writeByte(hasSeened ? (byte) 1 : (byte) 0);
		dest.writeString(this.url1);
		dest.writeString(this.url2);
		dest.writeInt(this.functionScrollLocation);
	}

	private ParagraphEntity(Parcel in) {
		this.paragraphId = in.readString();
		this.paragraphContent = in.readString();
		this.contentAttribute = in.readString();
		this.userId = in.readInt();
		this.userNickname = in.readString();
		this.userAvatarName = in.readString();
		this.createUserId = in.readInt();
		this.original = in.readInt();
		this.originName = in.readString();
		this.originAuthor = in.readString();
		this.originLink = in.readString();
		this.noteId = in.readString();
		this.originalEditingSort = in.readInt();
		this.tags = in.readString();
		this.contentId = in.readString();
		this.isChapterTitle = in.readInt();
		this.createTime = in.readString();
		this.updateTime = in.readString();
		this.picName = in.readString();
		this.likeCount = in.readInt();
		this.complaintCount = in.readInt();
		this.commentCount = in.readInt();
		this.contrastShowCount = in.readInt();
		this.paragraphReplyId = in.readString();
		this.likeState = in.readInt();
		this.complaintState = in.readInt();
		this.owerFollowerCount = in.readInt();
		this.followState = in.readInt();
		this.contrastSelect = in.readInt();
		this.paragraphScore = in.readInt();
		this.seriesCount = in.readInt();
		this.seriesId = in.readString();
		comments = new ArrayList<>();
		in.readTypedList(comments, ParagraphCommentEntity.CREATOR);
		this.hasSeened = in.readByte() != 0;
		this.url1 = in.readString();
		this.url2 = in.readString();
		this.functionScrollLocation = in.readInt();
	}

	public static final Creator<ParagraphEntity> CREATOR = new Creator<ParagraphEntity>() {
		public ParagraphEntity createFromParcel(Parcel source) {
			return new ParagraphEntity(source);
		}

		public ParagraphEntity[] newArray(int size) {
			return new ParagraphEntity[size];
		}
	};
}
