package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;

/**
 * Created by Kristian on 6/11/2016.
 */
public class IsPair extends SchemeProcedure {


    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {
        i.assertArgCountEqual(this,args,1);

        if(args[0] instanceof SchemePair){
            SchemePair p = (SchemePair) args[0];
            if(p.isNull()){
                return new EvaluationResult(new SchemeBoolean(false), e);
            }
            return new EvaluationResult(new SchemeBoolean(true), e);
        }

        return new EvaluationResult(new SchemeBoolean(false), e);
    }

}
