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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;


public abstract class NaRPCChannel {
	private static final Logger LOG = NaRPCUtils.getLogger();
	static final int HEADERSIZE = Integer.BYTES + Long.BYTES;
	
	public void makeMessage(long ticket, NaRPCMessage message, ByteBuffer buffer) throws IOException {
		buffer.clear().position(HEADERSIZE);
		int size = message.write(buffer);
		buffer.flip();
		if ((size + HEADERSIZE) != buffer.remaining()) {
			throw new IOException("Error in serialization");
		}

		buffer.clear();
		buffer.putInt(size);
		buffer.putLong(ticket);
		buffer.clear().limit(HEADERSIZE + size);
	}
	
	public long fetchBuffer(SocketChannel channel, ByteBuffer buffer) throws IOException{
		buffer.clear().limit(HEADERSIZE);
		while (buffer.hasRemaining()) {
			if (channel.read(buffer) < 0){
				return -1;
			}
		}
		buffer.flip();
		int size = buffer.getInt();
		long ticket = buffer.getLong();
		buffer.clear().limit(size);
		while (buffer.hasRemaining()) {
			if (channel.read(buffer) < 0) {
				throw new IOException("error when reading header from socket");
			}
			
		}
		buffer.flip();
//		LOG.info("fetching message with ticket " + ticket + ", threadid " + Thread.currentThread().getName());
		return ticket;
	}
	
	public void transmitMessage(SocketChannel channel, ByteBuffer buffer) throws IOException {
//		LOG.info("transmitting message with ticket " + buffer.getLong(4) + ", threadid " + Thread.currentThread().getName());
		while(buffer.hasRemaining()){
			channel.write(buffer);
		}		
	}
	
}
