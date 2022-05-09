import java.io.Serializable;
import java.util.ArrayList;

public class Client_obj implements Serializable {

	private final long serialVersionUID = 1234L;
	private ArrayList<Direct_message> dm;
	private ArrayList<String> users;
	private ArrayList<String> group_msgs;

	public Client_obj() {
		this.dm = new ArrayList<Direct_message>();
		this.users = new ArrayList<String>();
		this.group_msgs = new ArrayList<String>();
	}
	public synchronized void update_direct(Direct_message msg) {
		dm.add(msg);
	}

	public synchronized void update_group(String msg) {
		group_msgs.add(msg);
	}


	public synchronized boolean username_unique(String username) {
		if (users.contains(username)) return false;
		else return true;
	}

	public synchronized void update_users(String username) {
		users.add(username);
	}

	public synchronized void remove_user(String username) {
		users.remove(username);
	}

	public synchronized ArrayList<String> get_groupmsg() {
		return this.group_msgs;
	}

	public synchronized ArrayList<Direct_message> get_dm() {
		return this.dm;
	}

	public synchronized ArrayList<String> get_users() {
		return this.users;
	}

	public synchronized void clear_direct() {
		this.dm.clear();
	}

	public synchronized String toString() {
		return "GRP: " + this.group_msgs.toString() + "\n" + "DIR: " + this.dm.toString() + "\n" + "USR: " + this.users.toString()+ "\n";
	}
}
