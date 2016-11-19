package cn.incorner.contrast.data.entity;

import java.util.List;

public class PrivateMessageResultEntity {
	private int status;
	private String nickname;
	private String avatarName;
	private List<PrivateMessageEntity> messageList;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	public List<PrivateMessageEntity> getMessageList() {
		return messageList;
	}

	public void setMessageList(List<PrivateMessageEntity> messageList) {
		this.messageList = messageList;
	}

}
