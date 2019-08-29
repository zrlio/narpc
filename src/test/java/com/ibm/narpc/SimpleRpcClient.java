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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.narpc.NaRPCFuture;
import com.ibm.narpc.NaRPCClientGroup;
import com.ibm.narpc.NaRPCEndpoint;

public class SimpleRpcClient implements Runnable {
	private int id;
	private NaRPCEndpoint<SimpleRpcRequest, SimpleRpcResponse> endpoint;
	private int queueDepth;
	private int batchCount;
	private int loopCount;
	
	public SimpleRpcClient(int id, NaRPCEndpoint<SimpleRpcRequest, SimpleRpcResponse> endpoint, int queueDepth, int batchCount, int loopCount){
		this.id = id;
		this.endpoint = endpoint;
		this.queueDepth = queueDepth;
		this.batchCount = batchCount;
		this.loopCount = loopCount;
	}

	public void run() {
		try {
			System.out.println("SimpleRPCClient, queueDepth " + queueDepth + ", batchCount " + batchCount + ", loopCount " + loopCount);
			ArrayList<NaRPCFuture<SimpleRpcRequest, SimpleRpcResponse>> futureList = new ArrayList<NaRPCFuture<SimpleRpcRequest, SimpleRpcResponse>>(batchCount);
			for(int i = 0; i < loopCount; i++){
				futureList.clear();
				for (int j = 0; j < batchCount; j++){
					SimpleRpcRequest request = new SimpleRpcRequest(i*batchCount + j);
					SimpleRpcResponse response = new SimpleRpcResponse();
					NaRPCFuture<SimpleRpcRequest, SimpleRpcResponse> future = endpoint.issueRequest(request, response);
					futureList.add(j, future);
				}
				for (NaRPCFuture<SimpleRpcRequest, SimpleRpcResponse> future: futureList){
					SimpleRpcResponse resp = future.get();
					SimpleRpcRequest req = future.getRequest();
					System.out.println("id " + id + " request " + req.getCommand() + ", response " + resp.getResult());
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		int queueDepth = NaRPCGroup.DEFAULT_QUEUE_DEPTH;
		int loop = queueDepth;
		int batchCount = queueDepth;
		int threadCount = 1;
		int port = 1234;
		String address = "localhost";		
		
		if (args != null) {
			Option queueOption = Option.builder("q").desc("queue length").hasArg().build();
			Option loopOption = Option.builder("k").desc("loop").hasArg().build();
			Option threadOption = Option.builder("n").desc("number of threads").hasArg().build();
			Option batchOption = Option.builder("b").desc("batch of RPCs").hasArg().build();
			Option addressOption = Option.builder("a").desc("address").hasArg().build();
			Option portOption = Option.builder("p").desc("port").hasArg().build();			
			Options options = new Options();
			options.addOption(queueOption);
			options.addOption(loopOption);
			options.addOption(threadOption);
			options.addOption(batchOption);
			options.addOption(addressOption);
			options.addOption(portOption);			
			CommandLineParser parser = new DefaultParser();

			try {
				CommandLine line = parser.parse(options, Arrays.copyOfRange(args, 0, args.length));
				if (line.hasOption(queueOption.getOpt())) {
					queueDepth = Integer.parseInt(line.getOptionValue(queueOption.getOpt()));
				}
				if (line.hasOption(loopOption.getOpt())) {
					loop = Integer.parseInt(line.getOptionValue(loopOption.getOpt()));
				}	
				if (line.hasOption(threadOption.getOpt())) {
					threadCount = Integer.parseInt(line.getOptionValue(threadOption.getOpt()));
				}	
				if (line.hasOption(batchOption.getOpt())) {
					batchCount = Integer.parseInt(line.getOptionValue(batchOption.getOpt()));
				}	
				if (line.hasOption(addressOption.getOpt())) {
					address = line.getOptionValue(addressOption.getOpt());
				}	
				if (line.hasOption(portOption.getOpt())) {
					port = Integer.parseInt(line.getOptionValue(portOption.getOpt()));
				}				
			} catch (ParseException e) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("TCP RPC", options);
				System.exit(-1);
			}
		}	
		
		NaRPCClientGroup<SimpleRpcRequest, SimpleRpcResponse> clientGroup = new NaRPCClientGroup<SimpleRpcRequest, SimpleRpcResponse>(queueDepth, NaRPCGroup.DEFAULT_MESSAGE_SIZE, true);
		NaRPCEndpoint<SimpleRpcRequest, SimpleRpcResponse> endpoint = clientGroup.createEndpoint();
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		endpoint.connect(socketAddress);	
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++){
			SimpleRpcClient client = new SimpleRpcClient(i, endpoint, queueDepth, batchCount, loop);
			threads[i] = new Thread(client);
			threads[i].start();
		}
		for (int i = 0; i < threadCount; i++){
			threads[i].join();
		}
	}
}
