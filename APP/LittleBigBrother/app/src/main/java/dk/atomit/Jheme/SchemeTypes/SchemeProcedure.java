package dk.atomit.Jheme.SchemeTypes;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;

/**
 * Created by Kristian on 6/7/2016.
 */
public abstract class SchemeProcedure implements SchemeObject {

    public String getStringValue(){
        return "<#procedure>";
    }


    //Unevaluated arguments
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e){
        SchemeObject[] evargs = new SchemeObject[args.length];
        for(int index=0; index < args.length; index++){
            evargs[index] = i.eval(args[index], e).getSchemeObject();
        }

        return execute(evargs, i, e);
    }

    //Should be called from call
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        return new EvaluationResult(e);
    }

}
