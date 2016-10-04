package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/11/2016.
 */
public class Or extends SchemeProcedure {

    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e){

        if(args.length == 0){
            return new EvaluationResult(new SchemeBoolean(false), e);
        } else {
            for(SchemeObject arg : args){
                EvaluationResult r = i.eval(arg, e);
                SchemeObject rr = r.getSchemeObject();
                if(rr instanceof SchemeBoolean && ((SchemeBoolean) rr).getValue() == false){
                    //False
                    continue;
                } else {
                    //True
                    return r;
                }
            }

            return new EvaluationResult(new SchemeBoolean(false), e);
        }

    }

}
