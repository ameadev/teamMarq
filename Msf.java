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
						new Constant("send")),A, new ContextAssignment("sendA",new PredicateGet(2)) //,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
				));
		
		// A:  -> B si c'est un ask complet 
				item_machine.addTransition(A, new FunctionTransition(
						new FunctionTree(BooleanFunction.AND_FUNCTION,
							new FunctionTree(Equals.instance,
									new PredicateGet(0),
									new Constant("ask")),							
							new FunctionTree(Equals.instance,
									new PredicateGet(2),
									new ContextPlaceholder("sendA"))
							), B, new ContextAssignment("sendA",null)//,listSend.addAll(new PredicateGet(2)) //ajouter dans une variable de type liste
							));
		
		/*// A:  -> ERROR si c'est un ask pas conforme 
		item_machine.addTransition(A, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
					new FunctionTree(Equals.instance,
							new PredicateGet(0),
							new Constant("ask")),
					new FunctionTree(Negation.instance,
						new FunctionTree(Equals.instance,
								new PredicateGet(2),
								new ContextPlaceholder("sendA"))
					)
					), ERROR
							));
		*/
		
				
		/*		
		// 1: createAuction(_,X,Y) -> 1 / lastPrice := 0, minPrice := X, daysLeft := Y 
		item_machine.addTransition(NO_AUCTION, new FunctionTransition(
				new FunctionTree(Equals.instance,
						new PredicateGet(0),
						new Constant("create_auction")), AUCTION_OPEN,
				new ContextAssignment("lastPrice", new Constant(0)),
				new ContextAssignment("minPrice", new FunctionTree(NumberCast.instance, new PredicateGet(2))),
				new ContextAssignment("daysLeft", new FunctionTree(NumberCast.instance, new PredicateGet(3)))));
		item_machine.addTransition(NO_AUCTION, new TransitionOtherwise(ERROR));
		// 1: endOfDay & daysLeft = 1 -> 0
		item_machine.addTransition(AUCTION_OPEN, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
						new FunctionTree(Equals.instance,
								new PredicateGet(0),
								new Constant("endOfDay")),
						new FunctionTree(Equals.instance,
								new ContextPlaceholder("daysLeft"),
								new Constant(1f))), NO_AUCTION));
		// 1: endOfDay & daysLeft > 1 -> 1 / daysLeft := daysLeft - 1
		item_machine.addTransition(AUCTION_OPEN, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
						new FunctionTree(Equals.instance,
								new PredicateGet(0),
								new Constant("endOfDay")),
								new FunctionTree(IsGreaterThan.instance,
										new ContextPlaceholder("daysLeft"),
										new Constant(1f))), AUCTION_OPEN,
						new ContextAssignment("daysLeft", new FunctionTree(Subtraction.instance,
								new ContextPlaceholder("daysLeft"), new Constant(1)))));
		// 1: bid(_X) & X < minPrice & x > lastPrice -> 1 / lastPrice := X
		item_machine.addTransition(AUCTION_OPEN, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
						new FunctionTree(Equals.instance,
								new PredicateGet(0),
								new Constant("bid")),
						new FunctionTree(BooleanFunction.AND_FUNCTION,
								new FunctionTree(IsGreaterThan.instance,
										new PredicateGetNumber(2),
										new ContextPlaceholder("lastPrice")),
								new FunctionTree(IsLessThan.instance,
												new PredicateGetNumber(2),
												new ContextPlaceholder("minPrice"))
										)), AUCTION_OPEN,
				new ContextAssignment("lastPrice", new PredicateGetNumber(2))));
		// 1: bid(_X) & X >= minPrice & x > lastPrice -> 3
		item_machine.addTransition(AUCTION_OPEN, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
						new FunctionTree(Equals.instance,
								new PredicateGet(0),
								new Constant("bid")),
						new FunctionTree(BooleanFunction.AND_FUNCTION,
								new FunctionTree(IsGreaterThan.instance,
										new PredicateGetNumber(2),
										new ContextPlaceholder("lastPrice")),
								new FunctionTree(IsGreaterOrEqual.instance,
												new PredicateGetNumber(2),
												new ContextPlaceholder("minPrice"))
										)), MIN_PRICE_REACHED,
				new ContextAssignment("lastPrice", new PredicateGetNumber(2))));
		item_machine.addTransition(AUCTION_OPEN, new TransitionOtherwise(ERROR));
		item_machine.addTransition(ERROR, new TransitionOtherwise(ERROR));
		// 3: bid(_X) & X >= minPrice & x > lastPrice -> 3
		item_machine.addTransition(MIN_PRICE_REACHED, new FunctionTransition(
				new FunctionTree(BooleanFunction.AND_FUNCTION,
						new FunctionTree(Equals.instance,
								new PredicateGet(0),
								new Constant("bid")),
						new FunctionTree(BooleanFunction.AND_FUNCTION,
								new FunctionTree(IsGreaterThan.instance,
										new PredicateGetNumber(2),
										new ContextPlaceholder("lastPrice")),
								new FunctionTree(IsGreaterOrEqual.instance,
												new PredicateGetNumber(2),
												new ContextPlaceholder("minPrice"))
										)), MIN_PRICE_REACHED,
				new ContextAssignment("lastPrice", new PredicateGetNumber(2))));

		// 3: endOfDay -> 3
		item_machine.addTransition(MIN_PRICE_REACHED, new FunctionTransition(new FunctionTree(Equals.instance,
				new PredicateGet(0),
				new Constant("endOfDay")), MIN_PRICE_REACHED));
		item_machine.addTransition(MIN_PRICE_REACHED, new TransitionOtherwise(ERROR));
		item_machine.addTransition(FINISHED, new TransitionOtherwise(FINISHED));
		item_machine.addSymbol(NO_AUCTION, Troolean.Value.TRUE);
		item_machine.addSymbol(AUCTION_OPEN, Troolean.Value.TRUE);
		item_machine.addSymbol(ERROR, "false"); // A string is used to indicate cleanup
		item_machine.addSymbol(MIN_PRICE_REACHED, Troolean.Value.FALSE); // If price reached, must be sold
		item_machine.addSymbol(FINISHED, "true"); // A string is used to indicate cleanup
		*/
		//item_machine.addSymbol(ERROR, "false");
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
