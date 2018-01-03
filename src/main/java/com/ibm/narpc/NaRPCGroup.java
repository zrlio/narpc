package com.ibm.narpc;

import org.slf4j.Logger;

public class NaRPCGroup {
	private static final Logger LOG = NaRPCUtils.getLogger();
	
	public static int DEFAULT_QUEUE_DEPTH = 16;
	public static int DEFAULT_MESSAGE_SIZE = 512;
	
	private int queueDepth;
	private int messageSize;
	private boolean nodelay;
	
	public NaRPCGroup(){
		this(DEFAULT_QUEUE_DEPTH, DEFAULT_MESSAGE_SIZE, false);
//		this.queueDepth = DEFAULT_QUEUE_DEPTH;
//		this.messageSize = DEFAULT_MESSAGE_SIZE;
//		this.nodelay = false;
	}	
	
	public NaRPCGroup(int queueDepth, int messageSize, boolean nodelay){
		this.queueDepth = queueDepth;
		this.messageSize = messageSize;
		this.nodelay = nodelay;
		LOG.info("new NaRPC group, queueDepth " + this.queueDepth + ", messageSize " + messageSize);
	}

	public int getQueueDepth() {
		return queueDepth;
	}

	public int getMessageSize() {
		return messageSize;
	}

	public boolean isNodelay() {
		return nodelay;
	}
}
