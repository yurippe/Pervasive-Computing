package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Define extends SchemeProcedure {

    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e) {
        i.assertArgCountEqual(this, args, 2);
        SchemeSymbol name = (SchemeSymbol) args[0];
        SchemeObject value = i.eval(args[1], e).getSchemeObject();
        e.put(name.getValue(), value);
        return new EvaluationResult(e);
    }


}
