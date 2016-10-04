package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemePair;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/8/2016.
 */
public class List extends SchemeProcedure {

    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {
        return new EvaluationResult(SchemePair.fromArray(args), e);

    }
}
