package bench2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.fol.PredicateGet;
import ca.uqac.lif.cep.fol.PredicateTupleReader;
import ca.uqac.lif.cep.fsm.FunctionTransition;
import ca.uqac.lif.cep.fsm.MooreMachine;
import ca.uqac.lif.cep.functions.And;
import ca.uqac.lif.cep.functions.ArgumentPlaceholder;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.ContextAssignment;
import ca.uqac.lif.cep.functions.ContextPlaceholder;
import ca.uqac.lif.cep.functions.Equals;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.Negation;
import ca.uqac.lif.cep.functions.Or;
import ca.uqac.lif.cep.io.LineReader;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.numbers.Addition;
import ca.uqac.lif.cep.numbers.IsGreaterOrEqual;
import ca.uqac.lif.cep.sets.CsvToList;
import ca.uqac.lif.cep.tmf.StateSlicer;

public class Benchmark2 {
	public static void main(String[] args) throws IOException, ConnectorException{
		// Send, P1, P2, S1
		final int START = 0;
		final int ONESITE = 1;
		final int TWOSITE = 2;
		final int ANNOYING = 3;
		
		
		
		PredicateTupleReader readerFSM = new PredicateTupleReader();
		MooreMachine item_machine = new MooreMachine(1, 1);
		
		item_machine.addTransition(START, new FunctionTransition(
				new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new Constant(""))), ONESITE,
				new ContextAssignment("firstSite", new PredicateGet(3)),
				new ContextAssignment("nbrMsg", new Constant(1))));
		
		item_machine.addTransition(ONESITE, new FunctionTransition(
				new FunctionTree(Equals.instance,
						new PredicateGet(3),
						new ContextPlaceholder("firstSite")), ONESITE,
				new ContextAssignment("nbrMsg", new FunctionTree(Addition.instance,
						new ContextPlaceholder("nbrMsg"), new Constant(1)))));
		
		item_machine.addTransition(ONESITE, new FunctionTransition(
				new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new ContextPlaceholder("firstSite"))), TWOSITE,
				new ContextAssignment("secondSite", new PredicateGet(3)),
				new ContextAssignment("nbrMsg", new FunctionTree(Addition.instance,
					new ContextPlaceholder("nbrMsg"), new Constant(1)))));
		
		
		item_machine.addTransition(ONESITE, new FunctionTransition(
					new FunctionTree(IsGreaterOrEqual.instance,
							new ContextPlaceholder("nbrMsg"),
							new Constant(10)), ANNOYING));
		
		item_machine.addTransition(TWOSITE, new FunctionTransition(
				new FunctionTree(Or.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new ContextPlaceholder("FirstSite")),
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new ContextPlaceholder("secondSite"))
						), TWOSITE,
				new ContextAssignment("nbrMsg", new FunctionTree(Addition.instance,
						new ContextPlaceholder("nbrMsg"), new Constant(1)))));
		
		item_machine.addTransition(TWOSITE, new FunctionTransition(
				new FunctionTree(Or.instance,
						new FunctionTree(IsGreaterOrEqual.instance,
								new ContextPlaceholder("nbrMsg"),
								new Constant(10)),
						new FunctionTree(And.instance,
								new FunctionTree(Negation.instance,
										new FunctionTree(Equals.instance,
												new PredicateGet(3),
												new ContextPlaceholder("secondSite"))),
								new FunctionTree(Negation.instance,
										new FunctionTree(Equals.instance,
												new PredicateGet(3),
												new ContextPlaceholder("firstSite"))))), ANNOYING));

		// ANSWER
		item_machine.addTransition(ONESITE, new FunctionTransition(
				new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new ContextPlaceholder("firstSite"))), START,
				new ContextAssignment("secondSite", new PredicateGet(3)),
				new ContextAssignment("nbrMsg", new FunctionTree(Addition.instance,
					new ContextPlaceholder("nbrMsg"), new Constant(1)))));
		
		item_machine.addTransition(TWOSITE, new FunctionTransition(
				new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(3),
								new ContextPlaceholder("firstSite"))), START,
				new ContextAssignment("secondSite", new PredicateGet(3)),
				new ContextAssignment("nbrMsg", new FunctionTree(Addition.instance,
					new ContextPlaceholder("nbrMsg"), new Constant(1)))));
		
		item_machine.addSymbol(START, Troolean.Value.INCONCLUSIVE);
		item_machine.addSymbol(ONESITE, Troolean.Value.INCONCLUSIVE);
		item_machine.addSymbol(TWOSITE, Troolean.Value.INCONCLUSIVE);
		item_machine.addSymbol(ANNOYING, Troolean.Value.FALSE);
		
		StateSlicer slicer = new StateSlicer(new PredicateGet(1), item_machine);
		Connector.connect(readerFSM, slicer);
		
		LineReader reader = new LineReader(new File("logForBeepBeep.csv"));
		//LineReader.s_printStatus = true; // Print status line
		FunctionProcessor feeder = new FunctionProcessor(CsvToList.instance);
		
		Connector.connect(reader, feeder, slicer);

		Pullable p = item_machine.getPullableOutput(0);
		Object o = null;
		while (p.hasNext() != false)
		{
			o = p.pull();
		}
		System.out.println(printStatus(o));
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
