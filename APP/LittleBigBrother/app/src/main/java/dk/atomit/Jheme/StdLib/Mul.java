package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeFloat;
import dk.atomit.Jheme.SchemeTypes.SchemeInteger;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Mul extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {
        boolean allInts = true;
        double sum = 1;

        for(SchemeObject arg : args){
            if(arg instanceof SchemeInteger){
                sum *= ((SchemeInteger) arg).getValue();
            } else if (arg instanceof SchemeFloat){
                sum *= ((SchemeFloat) arg).getValue();
                allInts = false;
            } else {
                throw new RuntimeException("Add can only operate on numbers, got: " + arg);
            }
        }
        return new EvaluationResult((allInts) ? new SchemeInteger((int) sum) : new SchemeFloat(sum), e);
    }
}
