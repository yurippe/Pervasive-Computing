package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeExpression;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemePair;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

import java.util.*;

/**
 * Created by Kristian on 6/11/2016.
 */
public class Quote extends SchemeProcedure {

    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 1);

        SchemeObject o = args[0];

        if(o instanceof SchemeExpression){ //convert expressions into lists, this seem to be correct
            java.util.List<SchemeObject> l = ((SchemeExpression)o).list();
            SchemeObject[] ll = new SchemeObject[l.size()];
            o = SchemePair.fromArray(l.toArray(ll));
        }

        return new EvaluationResult(o, e);
    }
}
