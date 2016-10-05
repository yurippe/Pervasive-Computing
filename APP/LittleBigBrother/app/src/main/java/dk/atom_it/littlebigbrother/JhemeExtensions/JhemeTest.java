package dk.atom_it.littlebigbrother.JhemeExtensions;

import java.io.IOException;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeJavaObject;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.SchemeTypes.SchemeProcedure;
import dk.atomit.Jheme.SchemeTypes.SchemeString;
import okhttp3.Response;

/**
 * Created by Kristian on 10/5/2016.
 */

public class JhemeTest extends SchemeProcedure {

    @Override
    public EvaluationResult execute(SchemeObject[] args, Interpreter i, Environment e) {

        Response x = (Response) ((SchemeJavaObject)args[0]).getValue();

        try {
            return new EvaluationResult(new SchemeString(x.body().string()), e);
        } catch (IOException except){
            return new EvaluationResult(new SchemeString("IOError"), e);
        }

    }
}
