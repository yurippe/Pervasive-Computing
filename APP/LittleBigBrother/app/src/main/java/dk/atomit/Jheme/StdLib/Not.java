package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;

/**
 * Created by Kristian on 6/12/2016.
 */
public class Not extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 1);

        SchemeObject o = args[0];
        if(o instanceof SchemeBoolean){
            if(((SchemeBoolean) o).getValue() == false){
                return new EvaluationResult(new SchemeBoolean(true), e);
            }
        }
        return new EvaluationResult(new SchemeBoolean(false), e);
    }
}
