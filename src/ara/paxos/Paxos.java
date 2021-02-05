package ara.paxos;

import org.sar.ppi.dispatch.MessageHandler;

import ara.paxos.Messages.Query;
import ara.paxos.Messages.Leader;
import ara.paxos.Messages.Prepare;
import ara.paxos.Messages.Promise;
import ara.paxos.Messages.Accept;
import ara.paxos.Messages.Reject;

import org.sar.ppi.Infrastructure;
import org.sar.ppi.NodeProcess;

public class Paxos extends NodeProcess {
	public static String FIND_LEADER = "findLeader";

	public static class Proposer {
		/** -1 means no leader */
		int leader = -1;
		int round = 0;
	}

	public static class Acceptor {
		int acceptedValue = -1;
		int acceptedRound = -1;
		int maxReceivedRound = -1;
	}

	public static class Learner {
		int value = -1;
	}

	Proposer proposer = new Proposer();
	Acceptor acceptor = new Acceptor();
	Learner learner = new Learner();

	@Override
	public void init(String[] args) {
		if (infra.getId() == 0)
			infra.send(new Query(infra.getId(), infra.getId(), FIND_LEADER));
		System.out.println("Node " + infra.getId() + " is leader: " + (proposer.leader == infra.getId() ? "yes" : "no"));
	}

	@MessageHandler
	public void processQuery(Query m) {
		if (proposer.leader != -1) {
			infra.send(new Leader(infra.getId(), m.getIdsrc(), proposer.leader));
		} else {
			for (int i = 0; i < infra.size(); i++) {
				infra.send(new Prepare(infra.getId(), i, proposer.round));
			}
		}
	}

	@MessageHandler
	public void processLeader(Leader m) {
		System.out.println("Node " + infra.getId() + " leader is: " + m.leader );
	}

	@MessageHandler
	public void processPrepare(Prepare m) {
		System.out.println("Node " + infra.getId() + " receive prepare: " + m.round );
		if (m.round > acceptor.maxReceivedRound) {
			acceptor.maxReceivedRound = m.round;
			infra.send(new Promise(
				infra.getId(),
				m.getIdsrc(),
				acceptor.acceptedValue,
				acceptor.acceptedRound
			));
		} else {
			infra.send(new Reject(
				infra.getId(),
				m.getIdsrc(),
				acceptor.maxReceivedRound
			));
		}
	}

	@MessageHandler
	public void processPromise(Promise m) {
		System.out.println("Node " + infra.getId() + " promise acceptedValue: " + m.acceptedValue + ", acceptedRound: " + m.acceptedRound);
	}

	@MessageHandler
	public void processReject(Reject m) {
		System.out.println("Node " + infra.getId() + " reject maxReceivedRound: " + m.maxReceivedRound);
	}
}
