package com.ibm.narpc;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleRpcRequest implements NaRPCMessage {
	private int command;
	
	public SimpleRpcRequest(){
		this.command = -1;
	}	
	
	public SimpleRpcRequest(int command){
		this.command = command;
	}

	@Override
	public int write(ByteBuffer buffer) throws IOException {
		buffer.putInt(command);
		return size();
	}

	@Override
	public void update(ByteBuffer buffer) throws IOException {
		this.command = buffer.getInt();
	}

	public int size() {
		return Integer.BYTES;
	}

	public int getCommand() {
		return command;
	}

}
