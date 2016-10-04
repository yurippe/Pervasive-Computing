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
public class If extends SchemeProcedure {


    @Override
    public EvaluationResult call(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 3);

        EvaluationResult r = i.eval(args[0], e);
        SchemeObject rr = r.getSchemeObject();
        if(rr instanceof SchemeBoolean && ((SchemeBoolean) rr).getValue() == false){
            //False
            return i.eval(args[2], e);
        } else {
            //True
            return i.eval(args[1], e);
        }
    }

}
