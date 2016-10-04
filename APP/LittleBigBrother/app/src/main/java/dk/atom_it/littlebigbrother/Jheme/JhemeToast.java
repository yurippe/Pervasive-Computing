package dk.atom_it.littlebigbrother.Jheme;

import android.widget.Toast;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;

/**
 * Created by Kristian on 10/4/2016.
 */

public class JhemeToast extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e){
        i.assertArgCountEqual(this, args, 1);
        JhemeInterpreter interp = (JhemeInterpreter) i;
        Toast.makeText(interp.getActivity(), ((SchemeString) args[0]).getValue(), Toast.LENGTH_LONG).show();
        return new EvaluationResult(e);
    }
}
