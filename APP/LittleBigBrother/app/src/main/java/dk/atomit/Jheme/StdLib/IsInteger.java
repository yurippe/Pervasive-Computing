package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeInteger;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/7/2016.
 */
public class IsInteger extends SchemeProcedure{


    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {
        assert args.length == 1;
        return new EvaluationResult(new SchemeBoolean(args[0] instanceof SchemeInteger), e);
    }


}
