package cn.incorner.contrast.data.entity;

import org.xutils.DbManager;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.ex.DbException;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 对比度评论 实体类
 * 
 * @author yeshimin
 */
@Table(name = "paragraph_comment")
public class ParagraphCommentEntity implements Parcelable {

	@Column(name = "reply_to_user_id")
	private int replyToUserId;
	@Column(name = "create_time")
	private String createTime;
	@Column(name = "reply_user_user_level_id")
	private int replyUserUserLevelId;
	@Column(name = "parent_id")
	private int parentId;
	@Column(name = "reply_to_user_nickname")
	private String replyToUserNickname;
	@Column(name = "location")
	private String location;
	@Column(name = "reply_user_id")
	private int replyUserId;
	@Column(name = "parent_content")
	private String parentContent;
	@Column(name = "reply_content")
	private String replyContent;
	@Column(name = "reply_user_pic")
	private String replyUserPic;
	@Column(name = "paragraph_reply_id")
	private String paragraphReplyId;
	@Column(name = "reply_id", isId = true)
	private String replyId;
	@Column(name = "reply_user_nickname")
	private String replyUserNickname;
	@Column(name = "reply_user_is_anonymous")
	private int replyUserisAnonymous;

	// ex，拓展字段，用户 xUtils3 db持久化
	@Column(name = "ex_paragraph_id")
	private String exParagraphId;

	public String getExParagraphId() {
		return exParagraphId;
	}

	public void setExParagraphId(String exParagraphId) {
		this.exParagraphId = exParagraphId;
	}

	public ParagraphEntity getExParent(DbManager db) throws DbException {
		return db.selector(ParagraphEntity.class).where("paragraph_id", "=", this.exParagraphId)
				.findFirst();
	}

	public int getReplyToUserId() {
		return replyToUserId;
	}

	public void setReplyToUserId(int replyToUserId) {
		this.replyToUserId = replyToUserId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getReplyUserUserLevelId() {
		return replyUserUserLevelId;
	}

	public void setReplyUserUserLevelId(int replyUserUserLevelId) {
		this.replyUserUserLevelId = replyUserUserLevelId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getReplyToUserNickname() {
		return replyToUserNickname;
	}

	public void setReplyToUserNickname(String replyToUserNickname) {
		this.replyToUserNickname = replyToUserNickname;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getReplyUserId() {
		return replyUserId;
	}

	public void setReplyUserId(int replyUserId) {
		this.replyUserId = replyUserId;
	}

	public String getParentContent() {
		return parentContent;
	}

	public void setParentContent(String parentContent) {
		this.parentContent = parentContent;
	}

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}

	public String getReplyUserPic() {
		return replyUserPic;
	}

	public void setReplyUserPic(String replyUserPic) {
		this.replyUserPic = replyUserPic;
	}

	public String getParagraphReplyId() {
		return paragraphReplyId;
	}

	public void setParagraphReplyId(String paragraphReplyId) {
		this.paragraphReplyId = paragraphReplyId;
	}

	public String getReplyId() {
		return replyId;
	}

	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}

	public String getReplyUserNickname() {
		return replyUserNickname;
	}

	public void setReplyUserNickname(String replyUserNickname) {
		this.replyUserNickname = replyUserNickname;
	}

	public int getReplyUserisAnonymous() {
		return replyUserisAnonymous;
	}

	public void setReplyUserisAnonymous(int replyUserisAnonymous) {
		this.replyUserisAnonymous = replyUserisAnonymous;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(replyToUserId);
		dest.writeString(createTime);
		dest.writeInt(replyUserUserLevelId);
		dest.writeInt(parentId);
		dest.writeString(replyToUserNickname);
		dest.writeString(location);
		dest.writeInt(replyUserId);
		dest.writeString(parentContent);
		dest.writeString(replyContent);
		dest.writeString(replyUserPic);
		dest.writeString(paragraphReplyId);
		dest.writeString(replyId);
		dest.writeString(replyUserNickname);
		dest.writeInt(replyUserisAnonymous);

		// ex
		dest.writeString(exParagraphId);
	}

	public final static Parcelable.Creator<ParagraphCommentEntity> CREATOR = new Creator<ParagraphCommentEntity>() {

		@Override
		public ParagraphCommentEntity[] newArray(int size) {
			return new ParagraphCommentEntity[size];
		}

		@Override
		public ParagraphCommentEntity createFromParcel(Parcel source) {
			ParagraphCommentEntity entity = new ParagraphCommentEntity();
			entity.replyToUserId = source.readInt();
			entity.createTime = source.readString();
			entity.replyUserUserLevelId = source.readInt();
			entity.parentId = source.readInt();
			entity.replyToUserNickname = source.readString();
			entity.location = source.readString();
			entity.replyUserId = source.readInt();
			entity.parentContent = source.readString();
			entity.replyContent = source.readString();
			entity.replyUserPic = source.readString();
			entity.paragraphReplyId = source.readString();
			entity.replyId = source.readString();
			entity.replyUserNickname = source.readString();
			entity.replyUserisAnonymous = source.readInt();

			// ex
			entity.exParagraphId = source.readString();
			return entity;
		}
	};

}
