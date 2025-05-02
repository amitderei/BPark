package common;

public class ServerResponse {
	private boolean succeed;
	private Object data;
	private String msg;
	
	public ServerResponse(boolean succeed, Object data, String msg) {
		this.succeed=succeed;
		this.data=data;
		this.msg=msg;
	}

	public boolean isSucceed() {
		return succeed;
	}

	public Object getData() {
		return data;
	}

	public String getMsg() {
		return msg;
	}
}
