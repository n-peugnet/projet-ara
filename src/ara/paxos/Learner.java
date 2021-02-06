package ara.paxos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ara.paxos.Messages.Accepted;

public class Learner {
	public static final int NULL = -1;

	public int value = NULL;
	public Map<Integer, List<Accepted>> accepted = new HashMap<>();

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
