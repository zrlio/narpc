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
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NaRPCServerEndpoint<R extends NaRPCMessage, T extends NaRPCMessage> {
	private NaRPCServerGroup<R,T> serverGroup;
	private ServerSocketChannel serverSocket;
	
	public NaRPCServerEndpoint(NaRPCServerGroup<R,T> serverGroup) throws IOException{
		this.serverGroup = serverGroup;
		this.serverSocket = ServerSocketChannel.open();
	}
	
	public NaRPCServerChannel accept() throws Exception {
		SocketChannel channel = serverSocket.accept();
		NaRPCServerChannel serverChannel = new NaRPCServerChannel(serverGroup, channel);
		this.serverGroup.registerEndpoint(serverChannel);
		return serverChannel;
	}

	public NaRPCServerEndpoint<R,T> bind(InetSocketAddress address) throws IOException {
		serverSocket.bind(address);
		return this;
	}

	public String address() throws IOException {
		return serverSocket.getLocalAddress().toString();
	}
}
