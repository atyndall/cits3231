package sp.tests;


import sp.linkbrokers.linkingserver.LinkBroker;
import sp.linkbrokers.linkingserver.LinkingServer;
import sp.runoptions.RunOptions;

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
