package ara.control;

import java.io.IOException;
import java.io.Writer;

public class Ex1BackoffEnd extends CommonControl {

	public Ex1BackoffEnd(String prefix) {
		super(prefix);
	}

	@Override
	public void writeline(Writer file) throws IOException {
		file.write("" + idAsRound + "," + size + "," + timeout + "," + backoff + "," + backoffCoef + "," + retry + "," + messageCount + "," + roundCount + "," + time + "\n");
	}

	@Override
	public String getFileName() {
		return "ex1backoff.dat";
	}

}
