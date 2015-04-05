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
	private ServerSocket mother;

	public Comm(Role src_role) {
		// Load configuration info...
		File file = new File(Constants.Constants.confFile);
		this.sysConfig = CommUtil.parse(file);
		// get op obj
		this.role = src_role;
		this.motherIP = this.sysConfig.get(this.role.ID).getEntryIP();
		this.motherPort = this.sysConfig.get(this.role.ID).getEntryPort();
		this.mother = null;
		// Start ManagerThr
		Thread ManagerThr = new Thread(this);
		ManagerThr.start();
	}

	public void send(int recvID, Object obj) {
		try {
			String tar_ip = this.sysConfig.get(recvID).getEntryIP();
			int tar_port = this.sysConfig.get(recvID).getEntryPort();
			Socket tell = new Socket(tar_ip, tar_port);
			ObjectOutputStream oos = new ObjectOutputStream(tell.getOutputStream());
			oos.writeObject(obj);
			oos.flush();
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return;
		}
	}

	public void Terminator()
	{
		try {
			this.mother.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Resources already retrieved!");
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			this.mother = new ServerSocket(this.motherPort);
			Object msg = null;
			//
			while(true)
			{
				Socket son = this.mother.accept();
				// get para
				ObjectInputStream ois = new ObjectInputStream(son.getInputStream());
				msg = ois.readObject();
				//
				CommProcessThr Employee = new CommProcessThr(msg, this.role);
				Employee.start();
				//
				ois.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Machine " + this.role.ID + " resources retrieved!");
			return;
		}
	}
}
