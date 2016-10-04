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

        StringBuilder sb = new StringBuilder();
        for(SchemeObject arg : args){
            if (arg instanceof SchemeString){
                sb.append(((SchemeString) arg).getValue());
            } else {
                sb.append(arg.getStringValue());
            }
        }

        JhemeInterpreter interp = (JhemeInterpreter) i;
        Toast.makeText(interp.getActivity(), sb.toString(), Toast.LENGTH_LONG).show();
        return new EvaluationResult(e);
    }
}
