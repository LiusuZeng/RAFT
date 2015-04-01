package Exec;

public enum State {
	Leader(0), Follower(2), Candidate(1);
	private int value;
	private State(int value) {
		this.setValue(value);
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}
