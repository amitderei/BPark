package common;

import java.io.Serializable;

/**
 * Represents a generic response sent from the server to the client.
 * This object is used across the system to report whether an operation succeeded,
 * return any related data (if applicable), and provide a user-friendly message
 * to display in the UI or logs.
 * Implements Serializable so it can be transferred over the network via OCSF.
 */
public class ServerResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Whether the server-side operation succeeded */
	private boolean succeed;

	/** Optional data returned as part of the response (can be null) */
	private Object data;

	/** Message for the client (e.g., error, success info, etc.) */
	private String msg;

	/**
	 * Full constructor for ServerResponse.
	 *
	 * @param succeed whether the operation was successful
	 * @param data    the object returned (can be null)
	 * @param msg     message to display to user
	 */
	public ServerResponse(boolean succeed, Object data, String msg) {
		this.succeed = succeed;
		this.data = data;
		this.msg = msg;
	}

	/**
	 * Returns whether the operation was successful.
	 *
	 * @return true if succeeded, false otherwise
	 */
	public boolean isSucceed() {
		return succeed;
	}

	/**
	 * Returns the data returned by the server, if any.
	 *
	 * @return the returned object (can be null)
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Returns a message summarizing the result of the operation.
	 *
	 * @return user-readable message
	 */
	public String getMsg() {
		return msg;
	}
}

