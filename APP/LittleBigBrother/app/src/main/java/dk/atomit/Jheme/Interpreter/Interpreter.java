package dk.atomit.Jheme.Interpreter;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.SchemeTypes.*;
import dk.atomit.Jheme.Parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Interpreter {

    public static String VERSION = "0.03";

    protected Parser parser = new Parser();
    protected Environment environment = Environment.getStdEnvironment();

    //private boolean nextLiteral = false;

    public EvaluationResult eval(SchemeObject o, Environment e){
        /* Shitty implementation of ', current solution is to fix it in the parser by replacing it with (quote <expr>)
        if(nextLiteral){
            nextLiteral = false;

            if(o instanceof SchemeExpression){
                int size = ((SchemeExpression)o).list().size();
                o = SchemePair.fromArray(((SchemeExpression)o).list().toArray(new SchemeObject[size]));
            }

            return new EvaluationResult(o, e);}
        if(o instanceof SchemeLiteralMarker){nextLiteral = true; return new EvaluationResult(e);}
        */
        if(o instanceof SchemeSymbol) {
            return new EvaluationResult(e.find(((SchemeSymbol) o).getValue()), e);

        }else if(o instanceof SchemeExpression){
            return ((SchemeExpression) o).evaluate(this, e);
        } else{
            return new EvaluationResult(o, e);
        }
    }

    public EvaluationResult eval(String expressions){
        EvaluationResult result = null;
        EvaluationResult finalResult = null;
        for(SchemeObject exp : parser.parse(expressions)){
            result = eval(exp, environment);
            if(finalResult == null || !(result.getSchemeObject() instanceof SchemeNoreturn)){
                finalResult = result;
            }
        }
        if(finalResult != null && finalResult.getSchemeObject() instanceof SchemeUninitialized){
            throw new RuntimeException("Attempt to reference undefined variable");
        }
        return finalResult;
    }

    public void repl(){
        repl("");
    }
    public void repl(String prompt){
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            try {
                System.out.print(prompt);
                String line = buffer.readLine();
                try {
                    EvaluationResult r = eval(line);
                    if(!(r.getSchemeObject() instanceof SchemeNoreturn)) {
                        System.out.println(r.getSchemeObject().getStringValue());
                    }
                } catch (RuntimeException e) {
                    System.out.println("--Scheme Exception Thrown--");
                    if(e.getMessage() == null){
                        System.out.println("Syntax error");
                    } else {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("---------------------------");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void assertArgCountEqual(SchemeObject assertedin, SchemeObject[] args, int assertcount){
        if(!(args.length == assertcount)){
            throw new RuntimeException("Illegal argument count");
        }
    }

    public void assertArgCountMin(SchemeObject assertedin, SchemeObject[] args, int assertcount){
        if(!(args.length >= assertcount)){
            throw new RuntimeException("Illegal argument count");
        }
    }

    public void assertIsType(SchemeObject assertedin, SchemeObject arg, Class type){
        if(!(arg.getClass() == type)){
            throw new RuntimeException("Expected argument to be of type: " + type.getClass().getName());
        }
    }

    public void assertListSize(SchemeObject assertedin, List<?> list, int assertcount){
        if(!(list.size() == assertcount)){
            throw new RuntimeException("Expected length to be " + assertcount);
        }
    }


}
