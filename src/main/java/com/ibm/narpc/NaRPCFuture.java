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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NaRPCFuture<R extends NaRPCMessage,T extends NaRPCMessage> implements Future<T> {
	private NaRPCEndpoint<R,T> endpoint;
	private R request;
	private T response;
	private long ticket;
	private AtomicBoolean done;
	
	public NaRPCFuture(NaRPCEndpoint<R,T> endpoint, R request, T response, long ticket) {
		this.endpoint = endpoint;
		this.request = request;
		this.response = response;
		this.ticket = ticket;
		this.done = new AtomicBoolean(false);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		try {
			if (!done.get()){
				endpoint.pollResponse(done);
			}
		} catch(Exception e){
		}
		return done.get();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			while (!done.get()){
				endpoint.pollResponse(done);
			}
		} catch(Exception e){
			throw new ExecutionException(e);
		}
		return response;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		try {
			while (!done.get()){
				endpoint.pollResponse(done);
			}
		} catch(Exception e){
			throw new ExecutionException(e);
		}
		return response;
	}

	public R getRequest() {
		return request;
	}

	public T getResponse() {
		return response;
	}

	public long getTicket() {
		return ticket;
	}

	void signal() {
		this.done.set(true);
	}
}
