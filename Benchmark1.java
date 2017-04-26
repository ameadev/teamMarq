package qea;

import java.io.File;
import java.io.FileNotFoundException;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.io.LineReader;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.sets.CsvToList;


public class Benchmark1 {

	public static void main(String[] args) throws FileNotFoundException, ConnectorException {
		// TODO Auto-generated method stub
		String filename = "/home/mewena/workspace/BB/Mab/qea/examples/rv16/logForBeepBeepM.csv";
		runAndCollectMarq(new Msf(), filename);
	}
	
	public static void runAndCollectMarq(Processor property, String filename) throws ConnectorException, FileNotFoundException
	{
		LineReader reader = new LineReader(new File(filename));
		//LineReader.s_printStatus = true; // Print status line
		FunctionProcessor feeder = new FunctionProcessor(CsvToList.instance);
		Connector.connect(reader, feeder, property);
		Pullable p = property.getPullableOutput(0);
		long beg = System.currentTimeMillis();
		Object o = null;
		int pull_count = 0;
		while (p.hasNext() != false)
		{
			pull_count++;
			o = p.pull();
		}
		long end = System.currentTimeMillis();
		System.out.println(printStatus(o));
		System.out.println("Duration: " + (end - beg) / 1000 + " s, pulls: " + pull_count);
	}
	
	public static String printStatus(Object o)
	{
		StringBuilder out = new StringBuilder();
		out.append("\nStatus: ");
		if (o == null)
		{
			out.append("GaveUp");
		}
		else if (o == Troolean.Value.TRUE || o == Troolean.Value.INCONCLUSIVE)
		{
			out.append("Satisfied");
			//out.append("lol");
		}
		else if (o == Troolean.Value.FALSE)
		{
			out.append("Violated");
		}
		else
		{
			out.append("GaveUp");
		}
		return out.toString();
	}

}
