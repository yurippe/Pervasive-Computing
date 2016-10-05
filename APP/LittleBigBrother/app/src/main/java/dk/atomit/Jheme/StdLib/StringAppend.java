package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeNoreturn;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;

/**
 * Created by Kristian on 10/5/2016.
 */
public class StringAppend extends SchemeProcedure{

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter interpreter, Environment e) {

        StringBuilder sb = new StringBuilder();
        for(SchemeObject arg : args){
            if (arg instanceof SchemeString){
                sb.append(((SchemeString) arg).getValue());
            } else {
                sb.append(arg.getStringValue());
            }
        }

        return new EvaluationResult(new SchemeString(sb.toString()), e);
    }

}
