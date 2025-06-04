package common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Subscriber implements Serializable {
	private int subscriberCode;
	private String userId;
	private String firstName;
	private String lastName;
	private String phoneNum;
	private String email;
	private String username;
	private String tagId;

	public Subscriber(int subscriberCode, String userId, String firstName, String lastName, String phoneNum,
			String email, String username, String tagId) {
		this.subscriberCode = subscriberCode;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNum = phoneNum;
		this.email = email;
		this.username = username;
		this.tagId = tagId;
	}

	public Subscriber() {

	}

	public Subscriber(String username) {
		this.username = username;
	}

	public int getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(int subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public static boolean equals(Subscriber subscriber1, Subscriber subscriber2) {
		if (subscriber1.getEmail().equals(subscriber2.getEmail())
				&& subscriber1.getFirstName().equals(subscriber2.getFirstName())
				&& subscriber1.getLastName().equals(subscriber2.getLastName())
				&& subscriber1.getPhoneNum().equals(subscriber2.getPhoneNum())
				&& subscriber1.getUsername().equals(subscriber2.getUsername())
				&& subscriber1.getUserId().equals(subscriber2.getUserId())
				&& subscriber1.getSubscriberCode() == subscriber2.getSubscriberCode()) {
			return true;
		}
		return false;
	}
}
