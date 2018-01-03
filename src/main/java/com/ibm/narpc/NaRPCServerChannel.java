package com.ibm.narpc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NaRPCServerChannel extends NaRPCChannel {
	private SocketChannel channel;
	private ByteBuffer buffer;
	
	public NaRPCServerChannel(NaRPCGroup group, SocketChannel channel){
		this.channel = channel;
		this.buffer = ByteBuffer.allocate(group.getMessageSize());
	}

	public SocketChannel getSocketChannel() {
		return channel;
	}
	
	public long fetch(NaRPCMessage message) throws IOException {
		long ticket = fetchBuffer(channel, buffer);
		if (ticket > 0){
			message.update(buffer);	
		}
		return ticket;
	}
	
	public void transmit(long ticket, NaRPCMessage message) throws IOException {
		makeMessage(ticket, message, buffer);
		transmitMessage(channel, buffer);
	}
	
	public void close() throws IOException{
		this.channel.close();
	}

	public String address() throws IOException {
		return channel.getRemoteAddress().toString();
	}
}
