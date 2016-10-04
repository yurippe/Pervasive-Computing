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
public class Car extends SchemeProcedure {

    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {
        i.assertArgCountEqual(this, args, 1);
        i.assertIsType(this, args[0], SchemePair.class);

        SchemePair p = (SchemePair) args[0];
        return new EvaluationResult(p.getCar(), e);

    }
}
