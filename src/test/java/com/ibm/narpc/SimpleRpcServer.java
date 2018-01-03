package com.ibm.narpc;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.narpc.NaRPCEndpoint;
import com.ibm.narpc.NaRPCServerEndpoint;
import com.ibm.narpc.NaRPCServerGroup;
import com.ibm.narpc.NaRPCService;

public class SimpleRpcServer implements NaRPCService<SimpleRpcRequest, SimpleRpcResponse> {
	private SimpleRpcResponse fakeResponse;
	private int queueDepth;
	
	public SimpleRpcServer(int queueDepth){
		this.fakeResponse = new SimpleRpcResponse(100);
		this.queueDepth = queueDepth;
	}
	
	private void run() {
		try {
			NaRPCServerGroup<SimpleRpcRequest, SimpleRpcResponse> serverGroup = new NaRPCServerGroup<SimpleRpcRequest, SimpleRpcResponse>(this, queueDepth, NaRPCGroup.DEFAULT_MESSAGE_SIZE, true);
			NaRPCServerEndpoint<SimpleRpcRequest, SimpleRpcResponse> serverEndpoint = serverGroup.createServerEndpoint();
			InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 1234);
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
		
		if (args != null) {
			Option queueOption = Option.builder("q").desc("queue length").hasArg().build();
			Options options = new Options();
			options.addOption(queueOption);
			CommandLineParser parser = new DefaultParser();

			try {
				CommandLine line = parser.parse(options, Arrays.copyOfRange(args, 0, args.length));
				if (line.hasOption(queueOption.getOpt())) {
					queueDepth = Integer.parseInt(line.getOptionValue(queueOption.getOpt()));
				}
			} catch (ParseException e) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("TCP RPC", options);
				System.exit(-1);
			}
		}		
		
		SimpleRpcServer server = new SimpleRpcServer(queueDepth);
		server.run();
	}

	@Override
	public SimpleRpcResponse processRequest(SimpleRpcRequest request) {
//		System.out.println("got request, value " + request.getCommand());
		fakeResponse.setValue(request.getCommand());
		return fakeResponse;
	}
	
	public SimpleRpcRequest createRequest(){
		return new SimpleRpcRequest();
	}
}
