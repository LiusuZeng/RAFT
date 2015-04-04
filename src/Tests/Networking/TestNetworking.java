package Tests.Networking;

import java.io.IOException;

public class TestNetworking {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// Initialize different roles, which are actually dummy servers
			TestRole tr1 = new TestRole(1);
			TestRole tr2 = new TestRole(2);
			//
			tr1.sendVoteMsg(2);
			tr2.sendVoteMsg(1);
			//
			Thread.sleep(3000);
			tr1.comm.Terminator();
			tr2.comm.Terminator();
			System.out.println("Resources retrieved.");
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
