package qea;

import java.util.ArrayList;
import java.util.Set;

import ca.uqac.lif.cep.Context;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;

public class FunctionBenchmark1 extends Function {
	
	//private final Object m_value;
	private String result;
	private ArrayList <Object> listMsg = new ArrayList<Object>();
		
	public FunctionBenchmark1() {
		super();
		result="";
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs, Context context) {
		// TODO Auto-generated method stub
		outputs[0] = result;
	}

	@Override
	public void evaluate(Object[] inputs, Object[] outputs) {
		// TODO Auto-generated method stub
		evaluate(inputs, outputs, null);
	}

	@Override
	public int getInputArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOutputArity() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Function clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getInputTypesFor(Set<Class<?>> classes, int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getOutputTypeFor(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Function benchmark1Logic(Object msgType,Object msgContent){
		if (msgType.equals("send")){
			if (listMsg.contains(msgContent)){
				result="NOK";
			}
			else {
				listMsg.add(msgContent);
				result="KO";
			}
		}
		if (msgType.equals("ack")){
			if (listMsg.remove(msgContent)){
				result="OK";
			}
			else {
				result="KO";
			}
		}
		return new Constant(result);
	}

}
