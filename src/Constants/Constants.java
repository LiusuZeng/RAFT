package Constants;

public class Constants {
	public static final String confFile = "configuration.in";
	public static final String logFile = "log.out";
	
	public static final long backOffTimeout = 3000; // ms origin: 3000
	public static final long heartbeatTimeout = 5000; // ms origin: 5000
	public static final long requestTimeout = 3000; // ms
	
	public static final long refreshRate = 1000;
	public static final long heartbeatRate = 1000; // origin: 1000
	
	public static final int numServer = 5; // total 5 server
	
	//CX
	public static final int appendFrequency = 1;
	// LZ
	public static final int udpBufferSize = 65536;
}
