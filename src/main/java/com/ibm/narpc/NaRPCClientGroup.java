/*
 * NaRPC: An NIO-based RPC library
 *
 * Author: Patrick Stuedi <stu@zurich.ibm.com>
 *
 * Copyright (C) 2016-2018, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ibm.narpc;

import java.nio.channels.SocketChannel;

import org.slf4j.Logger;

public class NaRPCClientGroup<R extends NaRPCMessage, T extends NaRPCMessage> extends NaRPCGroup {
	private static final Logger LOG = NaRPCUtils.getLogger();
	
	public NaRPCClientGroup() {
		super();
		LOG.info("new NaRPC server group v1.0, queueDepth " + this.getQueueDepth() + ", messageSize " + this.getMessageSize() + ", nodealy " + this.isNodelay());
	}	
	
	public NaRPCClientGroup(int queueDepth, int messageSize, boolean nodelay) {
		super(queueDepth, messageSize, nodelay);
		LOG.info("new NaRPC server group v1.0, queueDepth " + this.getQueueDepth() + ", messageSize " + this.getMessageSize() + ", nodealy " + this.isNodelay());
	}

	public NaRPCEndpoint<R,T> createEndpoint() throws Exception{
		return new NaRPCEndpoint<R,T>(this, SocketChannel.open());
	}
}
