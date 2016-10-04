package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 10/4/2016.
 */
public class Begin extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter interpreter, Environment e) {
        interpreter.assertArgCountMin(this, args, 1);

        return new EvaluationResult(args[args.length - 1], e);
    }
}
