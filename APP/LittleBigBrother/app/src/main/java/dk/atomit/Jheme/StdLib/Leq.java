package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;

/**
 * Created by Kristian on 6/11/2016.
 */
public class Leq extends Comparator{

    @Override
    protected boolean compare(double x, double y) {
        return x <= y;
    }
}
