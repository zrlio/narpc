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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;

public class NaRPCDispatcher<R extends NaRPCMessage, T extends NaRPCMessage> implements Runnable {
    static private Logger LOG = NaRPCUtils.getLogger();
//    static final int HEADERSIZE = Integer.BYTES + Long.BYTES;
    
    private NaRPCGroup group;
    private LinkedBlockingQueue<NaRPCServerChannel> incomingChannels;
    private NaRPCService<R,T> service;
    private Selector selector;
    private R request;

    public NaRPCDispatcher(NaRPCGroup group, NaRPCService<R,T> service) throws IOException {
    	this.group = group;
        this.service = service;
        this.selector = Selector.open();
        this.incomingChannels = new LinkedBlockingQueue<NaRPCServerChannel>();
        this.request = service.createRequest();
    }

    public void addChannel(NaRPCServerChannel endpoint) throws IOException {
    	incomingChannels.add(endpoint);
    	selector.wakeup();
	}

	@Override
	public void run() {
		try {
			while (true) {
				int readyChannels = selector.select(1000);
				if (readyChannels > 0){
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (!key.isValid()){
							continue;
						}
						if (key.isReadable()) {
							NaRPCServerChannel channel = (NaRPCServerChannel) key.attachment();
							long ticket = channel.fetch(request);
							if(ticket > 0){
								T response = service.processRequest(request);
								channel.transmit(ticket, response);	
							} else if (ticket < 0){
								key.cancel();
								channel.close();
							} else {
								throw new Exception("ticket number invalid");
							}
						} 
						keyIterator.remove();
					}
				}
				processIncomingChannels();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processIncomingChannels() throws IOException{
		NaRPCServerChannel channel = incomingChannels.poll();
		while(channel != null){
			SocketChannel socket = channel.getSocketChannel();
			socket.configureBlocking(false);
			socket.socket().setTcpNoDelay(group.isNodelay());
			socket.register(selector, SelectionKey.OP_READ, channel);
			LOG.info("adding new channel to selector, from " + socket.getRemoteAddress());
			channel = incomingChannels.poll();
		}		
	}
}
