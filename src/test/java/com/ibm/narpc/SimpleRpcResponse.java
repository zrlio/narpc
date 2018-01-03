package com.ibm.narpc;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleRpcResponse implements NaRPCMessage {
	private int result;
	
	public SimpleRpcResponse(){
		this.result = -1;
	}	
	
	public SimpleRpcResponse(int result){
		this.result = result;
	}
	
	@Override
	public int write(ByteBuffer buffer) throws IOException {
		buffer.putInt(result);
		return size();
	}

	@Override
	public void update(ByteBuffer buffer) throws IOException {
		this.result = buffer.getInt();
	}

	public int size() {
		return Integer.BYTES;
	}

	public int getResult() {
		return result;
	}

	public void setValue(int command) {
		this.result = command;
	}

}
