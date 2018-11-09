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
