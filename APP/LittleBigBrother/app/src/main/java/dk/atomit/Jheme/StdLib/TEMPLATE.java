package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/11/2016.
 */
public class TEMPLATE extends SchemeProcedure {

    @Override
    public String getStringValue(){
        return "<#procedure TEMPLATE>";
    }


    /*
    This method is called with all args unevaluated, and the standard implementation looks like this:
    It is this methods job to evaluate the arguments (since Scheme is call by value) before passing them to execute
    You may override this to directly return the value and never call execute, if that is desireable, however,
    in most cases one should just override execute and leave 'call' alone.
     */
    @Override
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
