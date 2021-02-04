package ara.paxos;

import org.sar.ppi.dispatch.MessageHandler;
import org.sar.ppi.events.Message;
import org.sar.ppi.NodeProcess;

public class Paxos extends NodeProcess {

	public static class ExampleMessage extends Message{
		private static final long serialVersionUID = 1L;
		public String content;
		public ExampleMessage(int src, int dest, String content) {
			super(src, dest);
			this.content = content;
		}
	}

	@MessageHandler
	public void processExampleMessage(ExampleMessage message) {
		int host = infra.getId();
		System.out.printf(
			"%d Received '%s' from %d\n",
			host, message.content, message.getIdsrc()
		);
		if (host != 0) {
			int dest = (host + 1) % infra.size();
			infra.send(new ExampleMessage(infra.getId(), dest, "hello"));
		}
		infra.exit();
	}

	@Override
	public void init(String[] args) {
		if (infra.getId() == 0) {
			infra.send(new ExampleMessage(infra.getId(), 1, "hello"));
		}
	}
}
