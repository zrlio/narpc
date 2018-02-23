/*
 * NaRPC: An NIO-based RPC library
 *
 * Author: Patrick Stuedi <stu@zurich.ibm.com>
 *
 * Copyright (C) 2016, IBM Corporation
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

public class NaRPCServerGroup<R extends NaRPCMessage, T extends NaRPCMessage> extends NaRPCGroup {
	private static final Logger LOG = NaRPCUtils.getLogger();
	public static final int DEFAULT_DISPATCHARRAY = 1;
	
	private ArrayList<NaRPCDispatcher<R,T>> dispatcherArray;
	private AtomicInteger arrayIndex;
	
	public NaRPCServerGroup(NaRPCService<R,T> service) throws IOException{
		this(service, NaRPCGroup.DEFAULT_QUEUE_DEPTH, NaRPCGroup.DEFAULT_MESSAGE_SIZE, NaRPCGroup.DEFAULT_NODELAY, DEFAULT_DISPATCHARRAY);
	}
	
	public NaRPCServerGroup(NaRPCService<R,T> service, int queueDepth, int messageSize, boolean nodelay) throws IOException{
		this(service, queueDepth, messageSize, nodelay, DEFAULT_DISPATCHARRAY);
	}	
	
	public NaRPCServerGroup(NaRPCService<R,T> service, int queueDepth, int messageSize, boolean nodelay, int arraySize) throws IOException{
		super(queueDepth, messageSize, nodelay);
		this.dispatcherArray = new ArrayList<NaRPCDispatcher<R,T>>(arraySize);
		for (int i = 0; i < arraySize; i++) {
			NaRPCDispatcher<R,T> dispatcher = new NaRPCDispatcher<R,T>(this, service, i);
			Thread thread = new Thread(dispatcher);
			thread.start();
			dispatcherArray.add(dispatcher);
		}
		this.arrayIndex = new AtomicInteger(0);
		LOG.info("new NaRPC server group v1.0, queueDepth " + this.getQueueDepth() + ", messageSize " + this.getMessageSize() + ", nodealy " + this.isNodelay() + ", cores " + arraySize);
	}
	
	public NaRPCServerEndpoint<R,T> createServerEndpoint() throws IOException{
		return new NaRPCServerEndpoint<R,T>(this);
	}

	public void registerEndpoint(NaRPCServerChannel endpoint) throws IOException {
		int index = getAndIncrement() % dispatcherArray.size();
		NaRPCDispatcher<R,T> dispatcher = dispatcherArray.get(index);
		dispatcher.addChannel(endpoint);
	}
	
	private int getAndIncrement() {
    	return arrayIndex.getAndIncrement() & Integer.MAX_VALUE;
	}	
}
