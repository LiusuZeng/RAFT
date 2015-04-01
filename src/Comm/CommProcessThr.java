package Comm;

import java.util.*;
import java.io.*;
import java.net.*;
import Exec.*;

public class CommProcessThr extends Thread {

	private int recvID;
	private Object msg;
	private HashMap<Integer, CommInfo> mymap;
	private Role myrole;

	public CommProcessThr(int src_recvID, Object src_msg, HashMap<Integer, CommInfo> dirmap, Role src_role)
	{
		this.recvID = src_recvID;
		this.msg = src_msg;
		this.mymap = dirmap;
		this.myrole = src_role;
	}

	public void run()
	{
		if(this.recvID == -1)
		{
			this.myrole.recvMsg(this.msg);
			return;
		}
		String tar_ip = this.mymap.get(this.recvID).getEntryIP();
		int tar_port = this.mymap.get(this.recvID).getEntryPort();
		try {
			Socket transfer = new Socket(tar_ip, tar_port);
			ObjectOutputStream oos = new ObjectOutputStream(transfer.getOutputStream());
			oos.writeObject("RECV");
			oos.writeObject(this.msg);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
}
