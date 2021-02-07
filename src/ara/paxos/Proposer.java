package ara.paxos;

import java.util.ArrayList;
import java.util.List;

import org.sar.ppi.events.Message;

import ara.paxos.Messages.Promise;
import ara.paxos.Messages.Reject;
import peersim.core.CommonState;

public class Proposer {
	public static final int NULL = -1;

	/** ?? (pas dans mon algo) valeur finale du leader, décidée et immuable */
	public int leader = NULL;

	/** MaValeur, valeur proposée */
	public int value = NULL;

	/** MonNuméroRound = (nombre à définir : (0) ou (identifiant noeud)) */
	public int round = 0;

	/** Delai avant l'abandon de l'attente de Promise (ou de Reject) */
	public int timeout = 1000;

	/** Delai avant le nouvel essai d'envoi de Prepare */
	public int backoff = 1000;

	/** Coeficient multiplicateur du temps de backoff, 0 = backoff désactivé, 1 = constant evidemment */
	public int backoffCoef = 1;

	/** Nombre de fois où on a essayé d'envoyer à nouveau un Prepare */
	public int retry = 0;

	/** Nombre maximum d'essais d'envoi de Prepare */
	public int maxRetry = 100;

	/** ListePromiseReçus = (liste (vide) de (IdAcceptor, Valeur, NuméroRound)) ; */
	public List<Message> received = new ArrayList<>(); // ListePromiseReçus = (liste (vide) de (IdAcceptor, Valeur, NuméroRound)) ;

	public int backoffDelay() {
		return (backoff + CommonState.r.nextInt(backoff)) * (backoffCoef * retry);
	}

	/** Retourne le nombre de Promise reçus */
	public int promiseCount() {
		int count = 0;
		for (Message message : received) {
			if (message instanceof Promise) {
				// Ne comptabiliser que les promise correspondant à mon numéro de round actuel
				if (Paxos.ONLY_COUNT_MY_ROUND) {
					if (((Promise) message).responseRound == round)
						count++;
				} else {
					// Comptabiliser tous les promise
					count++;
				}
				
			}
		}
		return count;
	}

	/** Retourne le nombre de Reject reçus */
	public int rejectCount() {
		int count = 0;
		for (Message message : received) {
			if (message instanceof Reject) {
				// Ne comptabiliser que les promise correspondant à mon numéro de round actuel
				if (Paxos.ONLY_COUNT_MY_ROUND) {
					if (((Reject) message).responseRound == round)
						count++;
				} else {
					// Comptabiliser tous les promise
					count++;
				}
				
			}
		}
		return count;
	}

	public int rejectsMaxRound() {
		int max = NULL;
		Reject r;
		for (Message message : received) {
			if (message instanceof Reject) {
				r = (Reject) message;
				if (r.maxReceivedRound > max) max = r.maxReceivedRound;
			}
		}
		return max;
	}

	public int chooseNextRound() {
		int max = rejectsMaxRound();
		if (round > max) max = round;
		max++;
		return max;
	}

	/** Récupérer tous les promise reçus */
	public List<Promise> getPromises() {

		List<Promise> promises = new ArrayList<>();

		for (Message message : received) {

			if (message instanceof Promise) {

				Promise promise = (Promise) message;
				
				// Ne comptabiliser que les promise correspondant à mon numéro de round actuel
				if (Paxos.ONLY_COUNT_MY_ROUND) {
					if ((promise).responseRound == round)
						promises.add(promise);
				} else {
					// Comptabiliser tous les promise
					promises.add(promise);
				}
			}
		}
		return promises;
	}
}
