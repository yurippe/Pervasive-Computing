package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Sub extends SchemeProcedure{

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter interpreter, Environment e) {
        boolean allInts = (args[0] instanceof SchemeInteger);
        double sum = ((SchemeNumber) args[0]).getNumericValue();

        for(int i=1; i < args.length; i++){
            SchemeObject arg = args[i];
            if(arg instanceof SchemeInteger){
                sum -= ((SchemeInteger) arg).getValue();
            } else if (arg instanceof SchemeFloat){
                sum -= ((SchemeFloat) arg).getValue();
                allInts = false;
            } else {
                throw new RuntimeException("Sub can only operate on numbers, got: " + arg);
            }
        }
        return new EvaluationResult((allInts) ? new SchemeInteger((int) sum) : new SchemeFloat(sum), e);
    }
}
