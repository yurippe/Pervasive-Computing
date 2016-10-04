package dk.atomit.Jheme.SchemeTypes;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeExpression implements SchemeObject {

    private List<SchemeObject> exprs;

    public SchemeExpression(){
        exprs = new ArrayList<>();
    }

    public EvaluationResult evaluate(Interpreter i, Environment e){
        //exprs[0] should be the function we want to call, and exprs[1:] should be the arguments

        //Get the name so we can look it up in the environment:
        //String fname = ((SchemeSymbol) exprs.get(0)).getValue();
        //Since this is an expression we must assume that exprs[0] is a procedure (after evaluation at least):
        EvaluationResult evalResult = i.eval(exprs.get(0), e);
        SchemeProcedure proc = (SchemeProcedure) evalResult.getSchemeObject();
        Environment procenv = evalResult.getEnvironment();

        SchemeObject[] ARGS = new SchemeObject[exprs.size()-1];

        for(int index=1; index<exprs.size(); index++){
            ARGS[index-1] = exprs.get(index);
        }

        return proc.call(ARGS, i, procenv);
    }

    public void append(SchemeObject obj) {
        exprs.add(obj);
    }

    public List<SchemeObject> list(){
        return exprs;
    }

    public String getStringValue(){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int i = 0;
        while(i < exprs.size()){
            sb.append(exprs.get(i).getStringValue());
            if(!(i == exprs.size() - 1)){
                //not the last element, add a space
                sb.append(" ");
            }
            i++;
        }
        sb.append(")");
        return sb.toString();
    }
}
