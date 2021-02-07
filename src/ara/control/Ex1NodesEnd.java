package ara.control;

import java.io.IOException;
import java.io.Writer;

public class Ex1NodesEnd extends CommonControl {

	public Ex1NodesEnd(String prefix) {
		super(prefix);
	}

	@Override
	public void writeline(Writer file) throws IOException {
		file.write("" + idAsRound + "," + size + "," + messageCount + "," + roundCount + "," + time + "\n");
	}

	@Override
	public String getFileName() {
		return "ex1nodes.dat";
	}

}
