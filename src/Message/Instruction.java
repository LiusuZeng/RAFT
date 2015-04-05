package Message;

import java.io.Serializable;

public class Instruction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int value;
	
	public Instruction(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
