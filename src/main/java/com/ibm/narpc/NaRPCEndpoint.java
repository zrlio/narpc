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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class NaRPCEndpoint<R extends NaRPCMessage, T extends NaRPCMessage> {
	private NaRPCGroup group;
	private ConcurrentHashMap<Long, NaRPCFuture<R,T>> pendingRPCs;
	private ArrayBlockingQueue<ByteBuffer> bufferQueue;	
	private AtomicLong sequencer;
	private SocketChannel channel;
	private ReentrantLock readLock;
	private ReentrantLock writeLock;

	public NaRPCEndpoint(NaRPCGroup group, SocketChannel channel) throws Exception {
		this.group = group;
		this.channel = channel;
		this.channel.setOption(StandardSocketOptions.TCP_NODELAY, group.isNodelay());
		this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		this.pendingRPCs = new ConcurrentHashMap<Long, NaRPCFuture<R,T>>();
		this.bufferQueue = new ArrayBlockingQueue<ByteBuffer>(group.getQueueDepth());
		for (int i = 0; i < group.getQueueDepth(); i++){
			ByteBuffer buffer = ByteBuffer.allocate(group.getMessageSize());
			bufferQueue.put(buffer);
		}	
		this.sequencer = new AtomicLong(1);
		this.readLock = new ReentrantLock();
		this.writeLock = new ReentrantLock();
	}

	public void connect(InetSocketAddress address) throws IOException {
		this.channel.connect(address);
		this.channel.configureBlocking(false);
	}

	public void close() throws IOException{
		this.channel.close();
	}

	public NaRPCFuture<R,T> issueRequest(R request, T response) throws IOException {
		ByteBuffer buffer = getBuffer();
		while(buffer == null){
			buffer = getBuffer();
		}
		long ticket = sequencer.getAndIncrement();
		NaRPCProtocol.makeMessage(ticket, request, buffer);
		NaRPCFuture<R,T> future = new NaRPCFuture<R,T>(this, request, response, ticket);
		pendingRPCs.put(ticket, future);

		boolean wlocked = writeLock.tryLock();
		if (wlocked){
			channel.write(buffer);
			while(buffer.hasRemaining()){
				pollResponse();
				channel.write(buffer);
			}
			writeLock.unlock();
		}

		putBuffer(buffer);
		return future;
	}

	void pollResponse() throws IOException {
		ByteBuffer buffer = getBuffer();
		if (buffer == null){
			return;
		}
		boolean rlocked = readLock.tryLock();
		long ticket = 0;
		if (rlocked){
			ticket = NaRPCProtocol.fetchBuffer(channel, buffer);
			readLock.unlock();
		}
		if (ticket > 0){
			NaRPCFuture<R, T> future = pendingRPCs.remove(ticket);
			future.getResponse().update(buffer);
			future.signal();
		}
		putBuffer(buffer);
	}
	
	public String address() throws IOException {
		return channel.getRemoteAddress().toString();
	}
	
	private ByteBuffer getBuffer(){
		ByteBuffer buffer = bufferQueue.poll();
		return buffer;
	}
	
	private void putBuffer(ByteBuffer buffer){
		bufferQueue.add(buffer);
	}

	//----------- Old stuff

//	public NaRPCFuture<R,T> issueRequestOld(R request, T response) throws IOException {
//		ByteBuffer buffer = getBuffer();
//		long ticket = sequencer.getAndIncrement();
//		makeMessage(ticket, request, buffer);
//		NaRPCFuture<R,T> future = new NaRPCFuture<R,T>(this, request, response, ticket);
//		pendingRPCs.put(ticket, future);
//		while(!tryTransmittingOld(buffer)){
//		}
//		putBuffer(buffer);
//		return future;
//	}

//	private boolean tryTransmittingOld(ByteBuffer buffer) throws IOException{
//		boolean locked = writeLock.tryLock();
//		if (locked) {
//			transmitMessageOld(channel, buffer);
//			writeLock.unlock();
//			return true;
//		}
//		return false;
//	}
}
