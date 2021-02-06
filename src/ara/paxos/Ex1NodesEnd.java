package ara.paxos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.sar.ppi.peersim.PeerSimInfrastructure;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;

public class Ex1NodesEnd implements Control {

	private static final String PAR_PROTO_INFRA="infra";
	
	private final int pid_infra;
	
	public Ex1NodesEnd(String prefix) {
		pid_infra=Configuration.getPid(prefix+"."+PAR_PROTO_INFRA);
	}
	
	@Override
	public boolean execute() {
		
		System.out.println("################################# AFFICHAGE DES VALEURS ###########################");
		int size = Network.size();
		int messageCount = 0;
		int roundCount = 0;
		long time = CommonState.getTime();

		for(int i=0;i<Network.size();i++){
			Node node = Network.get(i);
			PeerSimInfrastructure infra = (PeerSimInfrastructure) node.getProtocol(pid_infra);
			Paxos process = (Paxos)infra.getProcess();
			// System.out.println("On node " + node.getID() + " variable = " + process.learner.value + "  (" + (node.getFailState()==Fallible.OK ? "alive" : "dead") + ")");
			messageCount += process.messageCount;
			roundCount = process.proposer.round; // TODO: choose and find the real number of rounds.
		}

		try (Writer file = new FileWriter("ex1nodes.dat", true)) {
			file.write("" + size + "," + messageCount + "," + roundCount + "," + time + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
