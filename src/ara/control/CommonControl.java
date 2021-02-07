package ara.control;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.sar.ppi.peersim.PeerSimInfrastructure;

import ara.paxos.Paxos;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;

public abstract class CommonControl implements Control {
	public static final int NULL = -1;

	private static final String PAR_PROTO_INFRA="infra";
	
	private final int pid_infra;
	int idAsRound = 0;
	int timeout = 0;
	int backoff = 0;
	int backoffCoef = 0;
	int retry = 0;
	int size = Network.size();
	int messageCount = 0;
	int roundCount = 0;
	long time = 0;
	int chosenValue = NULL;
	
	public CommonControl(String prefix) {
		pid_infra=Configuration.getPid(prefix+"."+PAR_PROTO_INFRA);
	}
	
	@Override
	public boolean execute() {
		
		System.out.println("################################# AFFICHAGE DES VALEURS ###########################");

		for(int i=0;i<Network.size();i++){
			Node node = Network.get(i);
			PeerSimInfrastructure infra = (PeerSimInfrastructure) node.getProtocol(pid_infra);
			Paxos process = (Paxos)infra.getProcess();
			boolean isAlive = node.getFailState()==Fallible.OK;
			System.out.println("On node " + node.getID() + " variable = " + process.learner.value + "  (" + ( isAlive ? "alive" : "dead") + ")");
			idAsRound = process.idAsRound ? 1 : 0;
			timeout = process.proposer.timeout;
			backoff = process.proposer.backoff;
			backoffCoef = process.proposer.backoffCoef;
			retry = process.proposer.maxRetry;
			messageCount += process.messageCount;
			roundCount += process.proposer.round; // TODO: choose and find the real number of rounds.
			if (chosenValue == NULL) {
				chosenValue = process.learner.value;
			} else {
				if (isAlive && chosenValue != process.learner.value) {
					throw new RuntimeException("Learners values do not match !");
				}
			}
		}
		roundCount /= size;
		time = CommonState.getTime();

		try (Writer file = new FileWriter(getFileName(), true)) {
			writeline(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract void writeline(Writer file) throws IOException;
	public abstract String getFileName();

}
