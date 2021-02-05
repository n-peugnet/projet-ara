package ara.paxos;

import org.sar.ppi.events.Message;

public class Messages {
	public static class Query extends Message {
		private static final long serialVersionUID = 1L;
		String val;
		public Query(int src, int dest, String val) {
			super(src, dest);
			this.val = val;
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
		public Promise(int src, int dest, int acceptedValue, int acceptedRound) {
			super(src, dest);
			this.acceptedValue = acceptedValue;
			this.acceptedRound = acceptedRound;
		}
	}

	public static class Reject extends Message {
		private static final long serialVersionUID = 1L;
		int maxReceivedRound;
		public Reject(int src, int dest, int maxReceivedRound) {
			super(src, dest);
			this.maxReceivedRound = maxReceivedRound;
		}
	}

	public static class Accept extends Message {
		private static final long serialVersionUID = 1L;
		public Accept(int src, int dest) {
			super(src, dest);
		}
	}

	public static class Accepted extends Message {
		private static final long serialVersionUID = 1L;
		public Accepted(int src, int dest) {
			super(src, dest);
		}
	}
}
