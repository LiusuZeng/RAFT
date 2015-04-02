package Comm;

import java.util.*;
import java.io.*;
import java.net.*;
import Exec.*;

public class CommProcessThr extends Thread {

	private Object msg;
	private Role myrole;

	public CommProcessThr(Object src_msg, Role src_role)
	{
		this.msg = src_msg;
		this.myrole = src_role;
	}

	public void run()
	{
		this.myrole.recvMsg(this.msg);
		return;
	}
}
