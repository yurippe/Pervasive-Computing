package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;


/**
 * Created by Kristian on 6/11/2016.
 */
public class LetStar extends SchemeProcedure {


    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e) {
        i.assertIsType(this, args[0], SchemeExpression.class);
        i.assertArgCountEqual(this, args, 2);
        //(let ([x 1] [y 2]) exp)

        Environment penv = e;
        Environment nenv = new Environment(e);

        for(SchemeObject a : ((SchemeExpression) args[0]).list()){
            i.assertIsType(this, a, SchemeExpression.class);
            SchemeExpression ar = (SchemeExpression) a;
            i.assertListSize(this, ar.list(), 2);

            SchemeSymbol aname = (SchemeSymbol) ar.list().get(0);
            SchemeObject aval = i.eval(ar.list().get(1),penv).getSchemeObject();

            nenv.put(aname.getValue(), aval);

            penv = nenv;
            nenv = new Environment(penv);

        }

        return i.eval(args[1], penv);

    }

}
