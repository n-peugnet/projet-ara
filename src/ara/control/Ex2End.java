package ara.control;

import java.io.IOException;
import java.io.Writer;

public class Ex2End extends CommonControl {

	public Ex2End(String prefix) {
		super(prefix);
	}

	@Override
	public void writeline(Writer file) throws IOException {
		file.write("" + idAsRound + "," + size + "," + messageCount + "," + roundCount + "," + time + "," + chosenValue + "\n");
	}

	@Override
	public String getFileName() {
		return "ex2.dat";
	}
	
}
