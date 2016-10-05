package dk.atom_it.littlebigbrother.JhemeExtensions;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeExpression;
import dk.atomit.Jheme.SchemeTypes.SchemeJavaObject;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;

/**
 * Created by Kristian on 10/5/2016.
 */

public class JhemeGetExtra extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 1);
        i.assertIsType(this, args[0], SchemeString.class);

        JhemeInterpreter interpreter = (JhemeInterpreter) i;
        Object javaObj = interpreter.getExtra(((SchemeString) args[0]).getValue());
        return new EvaluationResult(new SchemeJavaObject(javaObj), e);
    }

}
