package common;

import java.io.Serializable;

public class ServerResponse implements Serializable{
	private boolean succeed;
	private Object data;
	private String msg;
	
	/**
	 * constructor for server response
	 * @param succeed
	 * @param data
	 * @param msg
	 */
	public ServerResponse(boolean succeed, Object data, String msg) {
		this.succeed=succeed;
		this.data=data;
		this.msg=msg;
	}
	
	/**
	 * (Getter) return if the action succeed
	 * @return true if succeed, else false
	 */
	public boolean isSucceed() {
		return succeed;
	}

	/**
	 * (Getter) return the data of the action
	 * @return data of action, or null (if there is no data)
	 */
	public Object getData() {
		return data;
	}

	/**
	 * (Getter) return the message for client
	 * @return message for client
	 */
	public String getMsg() {
		return msg;
	}
}
