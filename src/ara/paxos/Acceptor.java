package ara.paxos;

public class Acceptor {
	public static final int NULL = -1;
	/** Valeur déjà acceptée */
	public int acceptedValue = NULL;
	/** Round associé à la valeur acceptée */
	public int acceptedRound = NULL;
	/** Round maximal reçu dans un message */
	public int maxReceivedRound = NULL;
}
