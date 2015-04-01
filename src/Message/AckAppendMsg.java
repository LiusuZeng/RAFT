package Message;

import java.io.Serializable;

public class AckAppendMsg extends AckBase implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int lastIndex;
	
	public AckAppendMsg(int leaderID, int currTerm, int ID, 
			boolean success, int lastAppliedIndex) {
		super(leaderID, currTerm, ID, success);
		this.lastIndex = lastAppliedIndex; 
	}
	
	public int getLastIndex() {
		return lastIndex;
	}
}
