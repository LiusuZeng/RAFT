package Message;

import java.io.Serializable;

public class AckBase implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int leaderID;
	private int term;
	private int ID;
	private boolean success;
	
	public AckBase(int leaderID, int term, int ID, boolean success) {
		//super();
		this.leaderID = leaderID;
		this.term = term;
		this.ID = ID;
		this.success = success;
	}
	
	public int getLeaderID() {
		return leaderID;
	}
	
	public int getTerm() {
		return term;
	}
	
	public int getID() {
		return ID;
	}
	
	public boolean getSuccess() {
		return success;
	}
}
