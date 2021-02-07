package ara.paxos;

import org.sar.ppi.dispatch.MessageHandler;

import ara.paxos.Messages.FindLeader;
import ara.paxos.Messages.Leader;
import ara.paxos.Messages.Prepare;
import ara.paxos.Messages.Promise;
import ara.paxos.Messages.Accept;
import ara.paxos.Messages.Accepted;
import ara.paxos.Messages.Reject;
import peersim.core.CommonState;

import java.util.List;

import org.sar.ppi.NodeProcess;

/**
 * Un promise n'est valide que s'il a le même numéro de round que moi
 */

public class Paxos extends NodeProcess {
	public static final int NULL = -1;
	public static final boolean PRINT_LOGS = false;


	public static void print(String str) {
		if (PRINT_LOGS)
			System.out.println(str);
	}

	public static final boolean ONLY_COUNT_MY_ROUND = true;

	public Proposer proposer = new Proposer();
	public Acceptor acceptor = new Acceptor();
	public Learner learner = new Learner();
	public int messageCount = 0;
	public boolean idAsRound = false;

	@Override
	public void init(String[] args) {
		idAsRound = Boolean.valueOf(args[0]);
		proposer.timeout = Integer.valueOf(args[1]);
		proposer.backoff = Integer.valueOf(args[2]);
		proposer.maxRetry = Integer.valueOf(args[3]);
		proposer.backoffCoef = Integer.valueOf(args[4]);

		proposer.round = idAsRound ? infra.getId() : 0;
		infra.serialThreadRun(() -> waitForAccepteds());
		infra.serialThreadRun(() -> selfFindLeader());
	}

	////////////////////////////////// PROPOSER ///////////////////////////////////////

	public void selfFindLeader() {
		messageCount++;
		infra.send(new FindLeader(infra.getId(), infra.getId()));
	}

	@MessageHandler
	// { Réception d'un message d'un client contenant la valeur v }
	// = proposition d'une valeur, je soumets ma valeur
	public void processFindLeader(FindLeader m) {

		/** Je propose ma valeur */
		proposer.value = infra.getId();

		/** Si le leader est connu, on renvoie simplment sa valeur */
		if (proposer.leader != NULL) {
			messageCount++;
			infra.send(new Leader(infra.getId(), m.getIdsrc(), proposer.leader));
			return;
		}

		/** Etape 1a - Envoi d'un message prepare à tous les Acceptors */
		for (int i = 0; i < infra.size(); i++) {
			messageCount++;
			infra.send(new Prepare(infra.getId(), i, proposer.round));
		}

		/**
		 * Etape 2a - Attente de la réception d'une majorité de promise TODO : Une
		 * majorité de Reject ou un timeout doivent générer soit un arrêt du proposer,
		 * soit une mise à jour du numéro de round + backoff + tenter à nouveau.
		 */
		int maj = infra.size() / 2;
		boolean success = false;
		try {
			success = infra.waitFor(() -> proposer.promiseCount() > maj || proposer.rejectCount() > maj,
					proposer.timeout);
		} catch (InterruptedException e) {
			print("Proposer " + infra.getId() + " was interrupted while waiting");
			return;
		}
		if (learner.value != NULL) {
			infra.send(new Leader(infra.getId(), m.getIdsrc(), learner.value));
			return;
		}
		if (!success || proposer.rejectCount() > maj) {
			if (proposer.retry == proposer.maxRetry) {
				return;
			}
			proposer.round = proposer.chooseNextRound();
			proposer.retry++;
			infra.scheduleCall("processFindLeader", new Object[] { m }, proposer.backoffDelay());
			return;
		}

		/** Le proposer a assez de promesses, il peut continuer */
		print("Proposer " + infra.getId() + " had enough promises");

		/** On détermine la valeur retenue */
		List<Promise> promises = proposer.getPromises();
		int maxRound = NULL;
		int value = NULL;
		for (Promise promise : promises) {
			/** Je prends la valeur associée au numéro de round le plus élevé */
			if (promise.acceptedValue != NULL && promise.acceptedRound > maxRound) {
				value = promise.acceptedValue;
				maxRound = promise.acceptedRound;
			}
		}
		/** Si aucune valeur déjà connue, j'impose ma valeur. */
		/** S'il y a une valeur reçue dans un promise, je mets à jour ma valeur */
		if (value != NULL) {
			proposer.value = value;
			// proposer.round = maxRound;
			// je conserve mon round actuel
		}
		print("Proposer " + infra.getId() + " proposer value: " + proposer.value);
		/** Etape 2a - Envoyer à tous les Acceptors Accept(e, n = NuméroRound) */
		for (int i = 0; i < infra.size(); i++) {
			messageCount++;
			infra.send(new Accept(infra.getId(), i, proposer.value, proposer.round));
		}

		try {
			infra.wait(() -> learner.value != NULL);
		} catch (InterruptedException e) {
			print("Proposer " + infra.getId() + " interrupted while waiting learner value to be present.");
		}
		
		infra.send(new Leader(infra.getId(), m.getIdsrc(), learner.value));
		return;
	}

	@MessageHandler
	public void processLeader(Leader m) {
		print("Proposer " + infra.getId() + " leader is: " + m.leader);
	}

	@MessageHandler
	public void processPromise(Promise m) {
		print("Proposer " + infra.getId() + " promise acceptedValue: " + m.acceptedValue + ", acceptedRound: " + m.acceptedRound);
		proposer.received.add(m);
	}

	@MessageHandler
	public void processReject(Reject m) {
		print("Proposer " + infra.getId() + " reject maxReceivedRound: " + m.maxReceivedRound);
	}


////////////////////////////////// ACCEPTOR ///////////////////////////////////////

	@MessageHandler
	/** Etape 1b - Réception d'un message Prepare(NuméroRound n) depuis un Proposer p  */
	public void processPrepare(Prepare m) {
		print("Acceptor " + infra.getId() + " receive prepare: " + m.round);
		/** si n > NumeroDeRoundMaxReçu */
		if (m.round > acceptor.maxReceivedRound) {
			acceptor.maxReceivedRound = m.round;
			/** Réponse au Proposer qui nous a envoyé le message */
			messageCount++;
			infra.send(new Promise(
				infra.getId(),
				m.getIdsrc(),
				acceptor.acceptedValue, // ValeurDéjàAcceptée
				acceptor.acceptedRound, // NuméroDeRoundAssocié
				m.round                 // NuméroDeRoundAuquelOnRépond
			));
		} else {
			/** Son numéro de round n'est pas valide,
			 *  s'il veut participer il devra mettre à jour sa valeur et passer
			 *  au minimum à acceptor.maxReceivedRound + 1. */
			messageCount++;
			infra.send(new Reject(
				infra.getId(),
				m.getIdsrc(),
				acceptor.maxReceivedRound, // NuméroDeRoundMaxReçu
				m.round                   // NuméroDeRoundAuquelOnRépond
			));
		}
	}

	@MessageHandler
	/** Réception d’un message Accept(NuméroRound n, Valeur e) depuis un Proposer p */
	public void processAccept(Accept m) {
		print("Acceptor " + infra.getId() + " accept value: " + m.value + ", round: " + m.round);
		/** Si nReçu >= NuméroDeRoundMaxReçu */
		if (m.round >= acceptor.maxReceivedRound) {
			/** Mise à jour de ma valeur acceptée */
			acceptor.acceptedValue = m.value;
			/** Mise à jour de la valeur de round associée */
			acceptor.acceptedRound = m.round;
			/** Envoi de Accepted(m.value) à tous les Learners */
			for (int i = 0; i < infra.size(); i++) {
				messageCount++;
				infra.send(new Accepted(infra.getId(), i, acceptor.acceptedValue));
			}
		/** Question :
		 *  est-ce qu'on envoie aussi un message au proposer pour lui dire que sa requête a été validée ? */
		} else {
			/** On ignore le message si le numéro de round est trop faible */
		}
	}

////////////////////////////////// LEARNER ////////////////////////////////////////

	@MessageHandler
	public void processAccepted(Accepted m) {
		print("Learner " + infra.getId() + " accepted value: " + m.value + ", from: " + m.getIdsrc());
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
			print("Learner " + infra.getId() + " did not have enough accepted");
		}
		print("Learner " + infra.getId() + " had enough accepted, value: " + learner.value);
	}
}
