package Message;

import java.io.Serializable;

public class AckVoteMsg extends AckBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AckVoteMsg(int leaderID, int currTerm, int ID, boolean success) {
		super(leaderID, currTerm, ID, success);		
	}

}
