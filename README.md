<h1>Implementation of Raft</h1>

<br>

<h2>About Raft</h2>

Storing data on an single server can be risky, however, replicating and modifying data on multiple servers can lead to consensus issues when server failure or network failure occurs.  

*Raft* is a distributed consensus algorithm. It implements distributed consensus by first electing a leader, then giving the leader complete responsibility for managing the replicated log. The leader accepts log entries from clients, replicates them on other servers, and tells servers when it is safe to apply log entries to their state machines. Having a leader simplifies the management of the replicated log because data flows in a simple way: from the leader to other servers. A leader can fail or become disconnected from the other servers. In this case, the system will have a new leader election and then a new leader. [1]

<br>

<h2>Objective</h2>

This project is aimed at implementing two main pieces of Raft: **leader election** and **log replication**. In detail, the project could be divided into 3 main parts:

1. Leader election

	* Transition between leader, candidate and follower
	
	* Leader election restriction

2. Log replication and recovery

	* Replicating logs to all servers

	* Determining the commit index for finite state machines

	* Log recovery when server failure occurs

3. Communication
	* Establishing and maintaining communication between servers
	
<h4>Leader Election</h4>

Leader election is the foundation of the Raft consensus algorithm. Each node will be in one of the three modes, leader mode, candidate mode or follower mode.

A node will start as follower, when this node cannot receive the heartbeat message sent by the leader, this node will timeout and become a candidate and send out vote requests to every node. 

Nodes, when receiving a vote request, will respond with either yes or no depending on whether the candidate’s log is up to date. 

Once a candidate get more than half yes responses, the candidate will become a leader and start sending out heartbeat messages.
A candidate will timeout and retry when there is a split vote.

<h4>Log Replication</h4>

Log replication is another key part of the Raft consensus algorithm.

The leader will never delete or change its own log entries.

When receiving requests from clients, the leader will send out log replication message (the AppendEntries RPC) to all other nodes and seek the last common index, the last index where the leader and followers share the same term, based on responses, and append leader's log to follower's from the last common index. 

Once one index is successfully replicated over majority of servers, this index is considered committed and will be applied to every nodes’ finite state machine.

If one follower is left behind, the leader will try infinitely to bring this follower up to date.

If one follower’s log entries are inconsistent with the leader’s log entries, the leader will force this follower to duplicate the leader’s log entries.

<br>

<h2>Server States</h2>

**Followers** only respond to requests from other servers. If a follower receives no communication, it becomes a candidate and initiates an election.

A **candidate** continues in its state until one of three things happens: (1) it wins the election, (2) another server establishes itself as leader.
Because of random time out, it is very likely that a new leader will emerge, which means (1) or (2) will happen. However, it is still possible the third thing happens: (3) if many followers become candidates at the same time, votes could be split so that no candidate obtains a majority. When this happens, each candidate will time out and start a new election by incrementing its term and initiating another round of RequestVote. 

**Leaders** typically operate until they fail.

<br>

<h2>Our Implementation</h2>

We simulate a system consists of 5 servers. Their IDs are from 0 to 4. They communicate through socket like the communication in Chord. The socket addresses for these 5 servers are written in **configuration.in** file.

Instead of using a client to send command (i.e. new log entry), we let the current leader to generate a random new log entry and try to append it to all other servers every 40 ms. If the system works well, all the servers should have the same log as leader's.

<br>

<h2>How to Compile</h2>
Go to **/raft** directory, type `make` to compile. 

A new directory called **bin** should be generated.

<br>

<h2>How to Run</h2>
You have two options. One is to run all 5 servers manually through UI, the other one is to run a pre-defined stress test.

<h4>Manual running</h4>
1.	Open five new terminal windows, Window 0 to Window 4. For each of them, go to the directory where the executable files exist by typing 

		cd .../RAFT/bin

2.	
	<br>
	In Window 0, type 
	
		java test/UI 0 
	
	to run No.0 server;
	
	<br>
	In Window 1, type
	
		java test/UI 1
	
	to run No.1 server;
	
	<br>
	...	
	<br>

	In Window 4, type
	
		java test/UI 4
	
	to run No.4 server.

3.	Hopefully you will have your first leader within a short time.

4.	Assume the leader is No.2. You can disconnect(block) this leader by typing `b` in Window 2. You will see "**Machine 2 networking is blocked**". Note that No.2 doesn't know it's disconnected and continues to generate/ append log entries in its own window. These new entries generated after its "death" will be deleted when it goes back to the system later.

5.	Now the system has only 4 servers, and there will be a new leader. Assume the new leader is No.3.

6.	You can recover the disconnected server No.2 by typing `r` in Window 2. No.2 will come back to the system as a follower. It will commmunicate with current leader No.3 and try to find its last common index with the leader. The leader will ask No.3 to delete its wrong log entries that were generated when it was blocked from the system, and add the correct log entries to No.3.

7. ctrl+C in all windows to stop the system.

8. Now feel free to check the logs in **/bin** directory.

<h4>Stress test</h4>

In order to do a further examination for the implemented Raft algorithm, a stress test is designed to check the functionality. It realized a automatic tool to check the system behavior for leader election and log replication when an accidental leader absence happens.

It starts the server groups and waits for the first leader to be elected. It then blocks the leader and forces the server group to select a new leader. After the new leader has been on for a while, the original leader is recovered from the blocking. Finally, logs from each member of the server group are checked for their consistency.

Just type 
	
	java test/StressTest











<br>
<h2>Reference</h2>
1. [In Search of an Understandable Consensus Algorithm (Extended Version)](http://db.cs.duke.edu/courses/cps212/spring15/15-744/S07/papers/raft.pdf), Diego Ongaro and John Ousterhout, Stanford University