package ara.paxos;

import org.sar.ppi.events.Message;

public class Messages {
	public static class FindLeader extends Message {
		private static final long serialVersionUID = 1L;
		public FindLeader(int src, int dest) {
			super(src, dest);
		}
	}

	public static class Leader extends Message {
		private static final long serialVersionUID = 1L;
		int leader;
		public Leader(int src, int dest, int leader) {
			super(src, dest);
			this.leader = leader;
		}
	}

	public static class Error extends Message {
		private static final long serialVersionUID = 1L;
		int leader;
		public Error(int src, int dest, int leader) {
			super(src, dest);
			this.leader = leader;
		}
	}

	public static class Prepare extends Message {
		private static final long serialVersionUID = 1L;
		int round;
		public Prepare(int src, int dest, int round) {
			super(src, dest);
			this.round = round;
		}
	}

	public static class Promise extends Message {
		private static final long serialVersionUID = 1L;
		int acceptedValue;
		int acceptedRound;
		int responseRound;
		public Promise(int src, int dest, int acceptedValue, int acceptedRound, int responseRound) {
			super(src, dest);
			this.acceptedValue = acceptedValue;
			this.acceptedRound = acceptedRound;
			this.responseRound = responseRound;
		}
	}

	public static class Reject extends Message {
		private static final long serialVersionUID = 1L;
		int maxReceivedRound;
		int responseRound;
		public Reject(int src, int dest, int maxReceivedRound, int responseRound) {
			super(src, dest);
			this.maxReceivedRound = maxReceivedRound;
			this.responseRound = responseRound;
		}
	}

	public static class Accept extends Message {
		private static final long serialVersionUID = 1L;
		int value;
		int round;
		public Accept(int src, int dest, int value, int round) {
			super(src, dest);
			this.value = value;
			this.round = round;
		}
	}

	public static class Accepted extends Message {
		private static final long serialVersionUID = 1L;
		int value;
		public Accepted(int src, int dest, int value) {
			super(src, dest);
			this.value = value;
		}
	}
}
