package Comm;

import java.util.*;
import java.io.*;

import Exec.*;

import java.net.*;

public class Comm implements Runnable {

	private HashMap<Integer, CommInfo> sysConfig = null;
	private Role role;
	private String motherIP;
	private int motherPort;
	// Thread control
	private volatile boolean StopSign = false;

	public Comm(Role src_role) {
		// Load configuration info...
		File file = new File(Constants.Constants.confFile);
		this.sysConfig = CommUtil.parse(file);
		// get op obj
		this.role = src_role;
		this.motherIP = this.sysConfig.get(this.role.ID).getEntryIP();
		this.motherPort = this.sysConfig.get(this.role.ID).getEntryPort();
		// Start ManagerThr
		Thread ManagerThr = new Thread(this);
		ManagerThr.start();
	}

	public void send(int recvID, Object obj) {
		try {
			Socket tell = new Socket(this.motherIP, this.motherPort);
			ObjectOutputStream oos = new ObjectOutputStream(tell.getOutputStream());
			oos.writeObject("SEND");
			oos.writeObject(recvID);
			oos.writeObject(obj);
			oos.flush();
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	public void Terminator()
	{
		this.StopSign = false;
	}

	public void run() {
		// TODO Auto-generated method stub
		ServerSocket mother = null;
		try {
			mother = new ServerSocket(this.motherPort);
			int recvID = -1;
			Object msg = null;
			String type = null;
			//
			while(!this.StopSign)
			{
				Socket son = mother.accept();
				// get para
				recvID = -1;
				msg = null;
				//
				ObjectInputStream ois = new ObjectInputStream(son.getInputStream());
				type = (String)ois.readObject();
				if(type.equals("SEND"))
				{
					recvID = (int)ois.readInt();
					msg = ois.readObject();
				}
				else msg = ois.readObject();
				//
				CommProcessThr Employee = new CommProcessThr(recvID, msg, this.sysConfig, this.role);
				Employee.start();
				//
				ois.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		// Resources retrieve
		try {
			mother.close();
			System.out.println("Resources of ID# " + this.role.ID + "retrieved.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
}
