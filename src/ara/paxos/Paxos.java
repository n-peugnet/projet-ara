package ara.paxos;

import org.sar.ppi.dispatch.MessageHandler;
import org.sar.ppi.events.Message;

import ara.paxos.Messages.FindLeader;
import ara.paxos.Messages.Leader;
import ara.paxos.Messages.Prepare;
import ara.paxos.Messages.Promise;
import ara.paxos.Messages.Accept;
import ara.paxos.Messages.Accepted;
import ara.paxos.Messages.Reject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sar.ppi.NodeProcess;

public class Paxos extends NodeProcess {
	public static int NULL = -1;

	public static class Proposer {
		int leader = NULL;
		int value = NULL;
		int round = 0;
		List<Message> received = new ArrayList<>();

		public int promiseCount() {
			int count = 0;
			for (Message message : received) {
				if (message instanceof Promise) {
					count++;
				}
			}
			return count;
		}

		public List<Promise> getPromises() {
			List<Promise> promises = new ArrayList<>();
			for (Message message : received) {
				if (message instanceof Promise) {
					promises.add((Promise) message);
				}
			}
			return promises;
		}
	}

	public static class Acceptor {
		int acceptedValue = NULL;
		int acceptedRound = NULL;
		int maxReceivedRound = NULL;
	}

	public static class Learner {
		int value = NULL;
		Map<Integer, List<Accepted>> accepted = new HashMap<>();

		public void addAccepted(Accepted m) {
			if (!accepted.containsKey(m.value)) {
				accepted.put(m.value, new ArrayList<>());
			}
			accepted.get(m.value).add(m);
		}

		public void reinit() {
			accepted = new HashMap<>();
			value = NULL;
		}
	}

	Proposer proposer = new Proposer();
	Acceptor acceptor = new Acceptor();
	Learner learner = new Learner();
	Thread learnerThread, proposerThread;

	@Override
	public void init(String[] args) {
		// proposer.round = infra.getId();
		learnerThread = infra.serialThreadRun(() -> waitForAccepteds());
		proposerThread = infra.serialThreadRun(() -> selfFindLeader());
	}

////////////////////////////////// PROPOSER ///////////////////////////////////////

	public void selfFindLeader() {
		infra.send(new FindLeader(infra.getId(), infra.getId()));
	}

	@MessageHandler
	public void processFindLeader(FindLeader m) {
		proposer.value = infra.getId();
		if (proposer.leader != NULL) {
			infra.send(new Leader(infra.getId(), m.getIdsrc(), proposer.leader));
			return;
		}
		for (int i = 0; i < infra.size(); i++) {
			infra.send(new Prepare(infra.getId(), i, proposer.round));
		}
		try {
			infra.wait(() -> proposer.promiseCount() > infra.size() / 2);
		} catch (InterruptedException e) {
			System.out.println("Proposer " + infra.getId() + " did not have enough promises");
		}
		System.out.println("Proposer " + infra.getId() + " had enough promises");
		List<Promise> promises = proposer.getPromises();
		int maxRound = NULL;
		int value = NULL;
		for (Promise promise : promises) {
			if (promise.acceptedValue != NULL && promise.acceptedRound > maxRound) {
				value = promise.acceptedValue;
				maxRound = promise.acceptedRound;
			}
		}
		if (value != NULL) {
			proposer.value = value;
			// proposer.round = maxRound;
		}
		System.out.println("Proposer " + infra.getId() + " proposer value: " + proposer.value);
		for (int i = 0; i < infra.size(); i++) {
			infra.send(new Accept(infra.getId(), i, proposer.value, proposer.round));
		}
	}

	@MessageHandler
	public void processLeader(Leader m) {
		System.out.println("Proposer " + infra.getId() + " leader is: " + m.leader);
	}

	@MessageHandler
	public void processPromise(Promise m) {
		System.out.println("Proposer " + infra.getId() + " promise acceptedValue: " + m.acceptedValue + ", acceptedRound: " + m.acceptedRound);
		proposer.received.add(m);
	}

	@MessageHandler
	public void processReject(Reject m) {
		System.out.println("Proposer " + infra.getId() + " reject maxReceivedRound: " + m.maxReceivedRound);
	}


////////////////////////////////// ACCEPTOR ///////////////////////////////////////

	@MessageHandler
	public void processPrepare(Prepare m) {
		System.out.println("Acceptor " + infra.getId() + " receive prepare: " + m.round);
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
	public void processAccept(Accept m) {
		System.out.println("Acceptor " + infra.getId() + " accept value: " + m.value + ", round: " + m.round);
		if (m.round >= acceptor.maxReceivedRound) {
			acceptor.acceptedValue = m.value;
		}
		for (int i = 0; i < infra.size(); i++) {
			infra.send(new Accepted(infra.getId(), i, acceptor.acceptedValue));
		}
	}

////////////////////////////////// LEARNER ////////////////////////////////////////

	@MessageHandler
	public void processAccepted(Accepted m) {
		System.out.println("Learner " + infra.getId() + " accepted value: " + m.value + ", from: " + m.getIdsrc());
		learner.addAccepted(m);
		for (Integer val : learner.accepted.keySet()) {
			if (learner.accepted.get(val).size() > infra.size() / 2) {
				learner.value = val;
				break;
			}
		}
	}

	public void waitForAccepteds() {
		try {
			infra.wait(() -> learner.value != NULL);
		} catch (InterruptedException e) {
			System.out.println("Learner " + infra.getId() + " did not have enough accepted");
		}
		System.out.println("Learner " + infra.getId() + " had enough accepted, value: " + learner.value);
		proposerThread.interrupt();
	}
}
