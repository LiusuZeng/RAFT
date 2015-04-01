package Message;

import java.io.Serializable;

public class VoteMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int currTerm;
	
	private int lastAppliedTerm;
	private int lastAppliedIndex;
	
	private int candidateID;

	public VoteMsg(int currTerm, int lastAppliedTerm, int lastAppliedIndex,
			int candidateID) {
		super();
		this.currTerm = currTerm;
		this.lastAppliedTerm = lastAppliedTerm;
		this.lastAppliedIndex = lastAppliedIndex;
		this.candidateID = candidateID;
	}

	public int getCurrTerm() {
		return currTerm;
	}

	public int getLastAppliedTerm() {
		return lastAppliedTerm;
	}

	public int getLastAppliedIndex() {
		return lastAppliedIndex;
	}
	
	public int getCandidateID() {
		return candidateID;
	}
}
