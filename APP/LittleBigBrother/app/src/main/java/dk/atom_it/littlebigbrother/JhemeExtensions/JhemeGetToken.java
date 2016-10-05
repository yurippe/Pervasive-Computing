package dk.atom_it.littlebigbrother.JhemeExtensions;

import dk.atom_it.littlebigbrother.data.Globals;
import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeJavaObject;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;

/**
 * Created by Kristian on 10/5/2016.
 */

public class JhemeGetToken extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 0);

        String tok = Globals.getInstance().token;
        if(tok == null){tok = "";}

        return new EvaluationResult(new SchemeString(tok), e);
    }

}