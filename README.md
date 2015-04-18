<h1>Implementation of Raft</h1>

<br>

<h2>About Raft</h2>

Storing data on an single server can be risky, however, replicating and modifying data on multiple servers can lead to consensus issues when server failure or network failure occurs.  

*Raft* is a distributed consensus algorithm. It implements distributed consensus by first electing a leader, then giving the leader complete responsibility for managing the replicated log. The leader accepts log entries from clients, replicates them on other servers, and tells servers when it is safe to apply log entries to their state machines. Having a leader simplifies the management of the replicated log because data flows in a simple way: from the leader to other servers. A leader can fail or become disconnected from the other servers. In this case, the system will have a new leader election and then a new leader.

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

Nodes when receiving a vote request, will respond with either yes or no depending on whether the candidate’s log is up to date. 

Once a candidate get more than half yes response, the candidate will become a leader and start sending out heartbeat messages.
A candidate will timeout and retry when there is a split vote.

<h4>Log Replication</h4>

Log replication is another key part of the Raft consensus algorithm.

The leader will never delete or change its own log entries.

When receiving requests from clients, the leader will send out log replication message (the AppendEntries RPC) to all other nodes and seek the last common index, the last index where the leader and followers has the same term, based on responses, and append leader's log to follower's from the last common index. 

Once one index is successfully replicated over majority of servers, this index is considered committed and will be applied to every nodes’ finite state machine.

If one follower is left behind, the leader will try infinitely to bring this follower up to date.

If one follower’s log entries are inconsistent with the leader’s log entries, the leader will force this follower to duplicate the leader’s log entries.

<br>

<h2>Logic Flow</h2>

<h4>Leader State</h4>







<br>
<h3>Reference</h3>
1. [In Search of an Understandable Consensus Algorithm (Extended Version)](http://db.cs.duke.edu/courses/cps212/spring15/15-744/S07/papers/raft.pdf), Diego Ongaro and John Ousterhout, Stanford University