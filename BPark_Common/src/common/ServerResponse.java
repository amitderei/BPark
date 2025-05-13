package common;

import java.io.Serializable;

/**
 * Represents a standardized response object sent from the server to the client.
 * Used to communicate the result of an operation, any associated data, and a
 * user-readable message. Implements Serializable for network transmission via
 * OCSF.
 */
public class ServerResponse implements Serializable {

	
	private static final long serialVersionUID = 1L;
	private boolean succeed; // Indicates whether the operation succeeded
	private Object data; // Data returned by the server
	private String msg; // Message to display to the user (success or error message)

	/**
	 * Constructs a ServerResponse object with specified fields.
	 *
	 * @param succeed Indicates if the operation was successful.
	 * @param data    The data payload to return (can be null).
	 * @param msg     A message describing the result (used in GUI or logs).
	 */
	public ServerResponse(boolean succeed, Object data, String msg) {
		this.succeed = succeed;
		this.data = data;
		this.msg = msg;
	}

	/**
	 * @return true if the operation succeeded, false otherwise.
	 */
	public boolean isSucceed() {
		return succeed;
	}

	/**
	 * @return The data object returned from the server (can be null).
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return A message string describing the result of the operation.
	 */
	public String getMsg() {
		return msg;
	}
}
