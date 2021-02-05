package ara.paxos;

import org.sar.ppi.peersim.PeerSimInfrastructure;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;

public class EndController implements Control {

	private static final String PAR_PROTO_INFRA="infra";
	
	private final int pid_infra;
	
	public EndController(String prefix) {
		pid_infra=Configuration.getPid(prefix+"."+PAR_PROTO_INFRA);
	}
	
	@Override
	public boolean execute() {
		
		System.out.println("################################# AFFICHAGE DES VALEURS ###########################");
		for(int i=0;i<Network.size();i++){
			Node node = Network.get(i);
			PeerSimInfrastructure infra = (PeerSimInfrastructure) node.getProtocol(pid_infra);
			Paxos process = (Paxos)infra.getProcess();
			System.out.println("On node " + node.getID() + " variable = " + process.learner.value + "  (" + (node.getFailState()==Fallible.OK ? "alive" : "dead") + ")");
		}
		return false;
	}

}
