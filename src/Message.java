import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1234L;

	public static final	int USERNAME = 1;
	public static final int GROUPMSG = 2;
	public static final int DIRECT = 3;
	public static final int ACCEPT = 4;
	public static final int REJECT = 5;
	public static final int DISCONNECT = 0;
	private int header;
	private String data;	
	private Direct_message dm;

	public Message(int header, String data) {
		this.header = header;
		this.data = data;
	}

	public int get_header() {
		return header;
	}

	public String get_data() {
		return data;
	}

	public Direct_message get_dm() {
		return dm;
	}

	public void set_dm(Direct_message dm) {
		this.dm = dm;
	}

	@Override
	public String toString() {
		return String.format("MSG = (header: %s || data: %s)", header, data);
	}

}
