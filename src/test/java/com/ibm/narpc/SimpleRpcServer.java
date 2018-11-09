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
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class SimpleRpcServer implements NaRPCService<SimpleRpcRequest, SimpleRpcResponse> {
	private SimpleRpcResponse fakeResponse;
	private String address;
	private int port;
	private int queueDepth;
	private int cores;
	
	public SimpleRpcServer(String address, int port, int queueDepth, int cores){
		this.fakeResponse = new SimpleRpcResponse(100);
		this.address = address;
		this.port = port;
		this.queueDepth = queueDepth;
		this.cores = cores;
	}
	
	private void run() {
		try {
			NaRPCServerGroup<SimpleRpcRequest, SimpleRpcResponse> serverGroup = new NaRPCServerGroup<SimpleRpcRequest, SimpleRpcResponse>(this, queueDepth, NaRPCGroup.DEFAULT_MESSAGE_SIZE, true, cores);
			NaRPCServerEndpoint<SimpleRpcRequest, SimpleRpcResponse> serverEndpoint = serverGroup.createServerEndpoint();
			InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
			serverEndpoint.bind(inetSocketAddress);			
			
			while(true){
				NaRPCServerChannel endpoint = serverEndpoint.accept();
				System.out.println("new RPC connection, address " + endpoint.address());
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		int queueDepth = NaRPCGroup.DEFAULT_QUEUE_DEPTH;
		int cores = NaRPCServerGroup.DEFAULT_DISPATCHARRAY;
		int port = 1234;
		String address = "localhost";
		
		if (args != null) {
			Option queueOption = Option.builder("q").desc("queue length").hasArg().build();
			Option coresOption = Option.builder("c").desc("#cores").hasArg().build();
			Option addressOption = Option.builder("a").desc("address").hasArg().build();
			Option portOption = Option.builder("p").desc("port").hasArg().build();
			Options options = new Options();
			options.addOption(queueOption);
			options.addOption(coresOption);
			options.addOption(addressOption);
			options.addOption(portOption);
			CommandLineParser parser = new DefaultParser();

			try {
				CommandLine line = parser.parse(options, Arrays.copyOfRange(args, 0, args.length));
				if (line.hasOption(queueOption.getOpt())) {
					queueDepth = Integer.parseInt(line.getOptionValue(queueOption.getOpt()));
				}
				if (line.hasOption(coresOption.getOpt())) {
					cores = Integer.parseInt(line.getOptionValue(coresOption.getOpt()));
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
		
		SimpleRpcServer server = new SimpleRpcServer(address, port, queueDepth, cores);
		server.run();
	}

	@Override
	public SimpleRpcResponse processRequest(SimpleRpcRequest request) {
//		System.out.println("got request, value " + request.getCommand());
		fakeResponse.setValue(request.getCommand());
		return fakeResponse;
	}

	public void addEndpoint(NaRPCServerChannel newConnection) {
		// no-op for now
	}

	public void removeEndpoint(NaRPCServerChannel closedConnection) {
		try {
			System.out.println(" RPC connection closed from " + closedConnection.address());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SimpleRpcRequest createRequest(){
		return new SimpleRpcRequest();
	}
}
