/*
    BeepBeep processor chains for the CRV 2016
    Copyright (C) 2016 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package qea;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.tmf.StateSlicer;
import ca.uqac.lif.cep.fol.PredicateGet;
import ca.uqac.lif.cep.fol.PredicateGetNumber;
import ca.uqac.lif.cep.fol.PredicateTupleReader;
import ca.uqac.lif.cep.fsm.FunctionTransition;
import ca.uqac.lif.cep.fsm.MooreMachine;
import ca.uqac.lif.cep.fsm.MooreMachine.TransitionOtherwise;
import ca.uqac.lif.cep.functions.ContextPlaceholder;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.ContextAssignment;
import ca.uqac.lif.cep.functions.Equals;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.Negation;
import ca.uqac.lif.cep.io.LineReader;
import ca.uqac.lif.cep.functions.ArgumentPlaceholder;
import ca.uqac.lif.cep.ltl.ArrayAnd;
import ca.uqac.lif.cep.ltl.BooleanFunction;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.numbers.IsGreaterOrEqual;
import ca.uqac.lif.cep.numbers.IsGreaterThan;
import ca.uqac.lif.cep.numbers.IsLessThan;
import ca.uqac.lif.cep.numbers.NumberCast;
import ca.uqac.lif.cep.numbers.Subtraction;

public class Msf extends GroupProcessor
{
	public StateSlicer slicer;
	ArrayList <String> listSend = new ArrayList();
	
	public void addToListSend (String msg)
	{
		this.listSend.add(msg);
	}
	
	public Msf() throws ConnectorException, FileNotFoundException
	{
		super(1, 1);
		final int NO_AUCTION = 0;
		final int A = 0;
		final int AUCTION_OPEN = 1;
		final int B = 1;
		final int ERROR = 2;
		final int C = 2;
		final int OK = 3;
		final int MIN_PRICE_REACHED = 3;
		final int FINISHED = 4;
		//InputStream stream = new FileInputStream("/home/mewena/workspace/BB/Mab/qea/examples/rv16/logForBeepBeep.csv");
		//LineReader reader = new LineReader(stream); 
		PredicateTupleReader reader = new PredicateTupleReader();
		MooreMachine item_machine = new MooreMachine(1, 1);
		// A:  -> A si c'est un send 
		item_machine.addTransition(A, new FunctionTransition(
				new FunctionTree(Equals.instance,
						new PredicateGet(0),
						new Constant("send")),A, new ContextAssignment("sendA",new PredicateGet(1)) //,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
				));
		//item_machine.addTransition(C, new TransitionOtherwise(ERROR));
		
		
		// A:  -> B si c'est un ask complet 
				item_machine.addTransition(A, new FunctionTransition(
						new FunctionTree(BooleanFunction.AND_FUNCTION,
							new FunctionTree(Equals.instance,
									new PredicateGet(0),
									new Constant("ack")),							
							new FunctionTree(Equals.instance,
									new PredicateGet(1),
									new ContextPlaceholder("sendA"))
							), B//, new ContextAssignment("sendA",null)//,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
							));
				
		// B:  -> B si c'est un send 
		item_machine.addTransition(B, new FunctionTransition(
				new FunctionTree(Equals.instance,
						new PredicateGet(0),
						new Constant("send")),B, new ContextAssignment("sendA",new PredicateGet(1)) //,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
				));
		
		// B:  -> A si c'est un ask complet 
		item_machine.addTransition(B, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
					new FunctionTree(Equals.instance,
							new PredicateGet(0),
							new Constant("ack")),							
					new FunctionTree(Equals.instance,
							new PredicateGet(1),
							new ContextPlaceholder("sendA"))
					), A//, new ContextAssignment("sendA",null)//,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
					));
		
		// A:  -> ERROR si c'est un ask pas conforme 
		item_machine.addTransition(A, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
					new FunctionTree(Equals.instance,
							new PredicateGet(0),
							new Constant("ack")),
					new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(1),
								new ContextPlaceholder("sendA"))
					)
					), ERROR
							));
		
		// B:  -> ERROR si c'est un ask pas conforme 
				item_machine.addTransition(B, new FunctionTransition(
						new FunctionTree(BooleanFunction.AND_FUNCTION,
							new FunctionTree(Equals.instance,
									new PredicateGet(0),
									new Constant("ack")),
							new FunctionTree(Negation.instance,
								new FunctionTree(Equals.instance,
										new PredicateGet(1),
										new ContextPlaceholder("sendA"))
							)
							), ERROR
									));
	
		
				
		item_machine.addSymbol(C, "false");
		item_machine.addSymbol(ERROR, Troolean.Value.FALSE);
		item_machine.addSymbol(A, Troolean.Value.INCONCLUSIVE);
		item_machine.addSymbol(B, Troolean.Value.INCONCLUSIVE);
		
		Function cleanup = new FunctionTree(BooleanFunction.OR_FUNCTION,
				new FunctionTree(Equals.instance, new Constant("true"), new ArgumentPlaceholder(0)),
				new FunctionTree(Equals.instance, new Constant("false"), new ArgumentPlaceholder(0)));
		StateSlicer slicer = new StateSlicer(new PredicateGet(1), item_machine, cleanup);
		Connector.connect(reader, slicer);
		FunctionProcessor and = new FunctionProcessor(ArrayAnd.instance);
		Connector.connect(slicer, and);
		addProcessors(reader, slicer, and);
		associateInput(0, reader, 0);
		associateOutput(0, and, 0);
		this.slicer = slicer;
	}


	
}
