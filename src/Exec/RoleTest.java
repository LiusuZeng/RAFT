package Exec;

import java.io.IOException;

public class RoleTest {
	public Role myrole;
	
	public RoleTest(int ID) throws IOException {
		myrole = new Role(ID);
		// TODO Auto-generated constructor stub
	}

	public void setAlive (boolean value) {
		myrole.alive = value;
	}
}
