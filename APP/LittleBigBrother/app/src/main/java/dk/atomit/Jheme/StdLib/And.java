package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeUninitialized;

/**
 * Created by Kristian on 6/11/2016.
 */
public class And extends SchemeProcedure {

    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e){

        if(args.length == 0){
            return new EvaluationResult(new SchemeBoolean(true), e);
        } else {
            EvaluationResult r = null;
            for(SchemeObject arg : args){
                r = i.eval(arg, e);
                SchemeObject rr = r.getSchemeObject();
                if(rr instanceof SchemeBoolean && ((SchemeBoolean) rr).getValue() == false){
                    //False
                    return new EvaluationResult(new SchemeBoolean(false), e);
                } else {
                    //True
                    continue;
                }
            }

            return r;


        }

    }

}
