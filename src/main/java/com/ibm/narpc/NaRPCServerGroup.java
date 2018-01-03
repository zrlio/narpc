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

public class NaRPCServerGroup<R extends NaRPCMessage, T extends NaRPCMessage> extends NaRPCGroup {
	private NaRPCService<R,T> service;
	private NaRPCDispatcher<R,T> dispatcher;
	
	public NaRPCServerGroup(NaRPCService<R,T> service) throws IOException{
		super();
		this.service = service;
		this.dispatcher = new NaRPCDispatcher<R,T>(this, service);
		Thread thread = new Thread(dispatcher);
		thread.start();
	}	
	
	public NaRPCServerGroup(NaRPCService<R,T> service, int queueDepth, int messageSize, boolean nodelay) throws IOException{
		super(queueDepth, messageSize, nodelay);
		this.service = service;
		this.dispatcher = new NaRPCDispatcher<R,T>(this, service);
		Thread thread = new Thread(dispatcher);
		thread.start();
	}
	
	public NaRPCServerEndpoint<R,T> createServerEndpoint() throws IOException{
		return new NaRPCServerEndpoint<R,T>(this);
	}

	public void registerEndpoint(NaRPCServerChannel endpoint) throws IOException {
		this.dispatcher.addChannel(endpoint);
	}
}
