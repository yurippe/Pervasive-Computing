package dk.atomit.Jheme.StdLib;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.Interpreter.EvaluationResult;
import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.*;

/**
 * Created by Kristian on 6/11/2016.
 */
public abstract class Comparator extends SchemeProcedure {


        @Override
        public EvaluationResult execute(SchemeObject[] args, Interpreter interpreter, Environment e) {
            interpreter.assertArgCountMin(this, args, 1);

            if(args.length == 1){
                return new EvaluationResult(new SchemeBoolean(true), e);
            }

            Double cmpto;

            if(args[0] instanceof SchemeInteger){
                cmpto = (double) ((SchemeInteger) args[0]).getValue();
            } else if (args[0] instanceof SchemeFloat){
                cmpto = ((SchemeFloat) args[0]).getValue();
            } else {
                throw new RuntimeException("Leq can only operate on numbers, got: " + args[0]);
            }

            for(int i=1; i < args.length; i++){
                SchemeObject arg = args[i];
                if(arg instanceof SchemeInteger){
                    int val = ((SchemeInteger) arg).getValue();
                    if(compare(cmpto, val)){
                        cmpto = (double) val;
                        continue;
                    } else {
                        return new EvaluationResult(new SchemeBoolean(false), e);
                    }
                } else if (arg instanceof SchemeFloat){
                    double val = ((SchemeInteger) arg).getValue();
                    if(compare(cmpto, val)){
                        cmpto = val;
                        continue;
                    } else {
                        return new EvaluationResult(new SchemeBoolean(false), e);
                    }
                } else {
                    throw new RuntimeException("Leq can only operate on numbers, got: " + arg);
                }
            }
            return new EvaluationResult(new SchemeBoolean(true), e);
        }


        protected abstract boolean compare(double x, double y);

        private boolean compare(int x, int y){return compare((double) x, (double) y);}
        private boolean compare(double x, int y){return compare(x, (double) y);}
        private boolean compare(int x, double y){return compare((double) x, y);}

}
