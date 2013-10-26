package sp.tests;


import sp.common.RunOptions;
import sp.linkbrokers.linkingserver.LinkBroker;
import sp.linkbrokers.linkingserver.LinkingServer;

public class RunLinkServer implements Runnable{
	LinkingServer linkingServer;
	RunOptions runOptions;
	
	public RunLinkServer(RunOptions runOptions){
		this.runOptions = runOptions;
	}
	
	@Override
	public void run() {
		this.linkingServer = new LinkBroker(runOptions).getLinkingServer();
	}

	public LinkingServer getLinkingServer(){
		return linkingServer;
	}
}
