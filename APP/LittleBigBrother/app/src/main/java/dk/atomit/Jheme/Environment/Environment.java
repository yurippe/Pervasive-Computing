package dk.atomit.Jheme.Environment;

import dk.atomit.Jheme.SchemeTypes.SchemeBoolean;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;
import dk.atomit.Jheme.StdLib.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Environment {

    private Map<String, SchemeObject> env;
    private Environment outer;

    public Environment(List<String> params, List<SchemeObject> args, Environment outer){
        this(outer);
        if(!(params.size() == args.size())){
            throw new RuntimeException("Can't zip lists of different sizes");
        }



        int i = 0;
        while(i < params.size()){
            env.put(params.get(i), args.get(i));
            i++;
        }

    }

    public Environment(Environment outer){
        this();
        this.outer = outer;
    }

    public Environment(){
        env = new HashMap<>();
        this.outer = null;
    }

    public SchemeObject find(String key){
        if(env.containsKey(key)){
            return env.get(key);
        } else if (outer != null){
            return outer.find(key);
        } else {
            throw new RuntimeException("Variable not defined: " + key);
        }
    }

    public void put(String key, SchemeObject value){
        env.put(key, value);
    }

    public static Environment getStdEnvironment(){

        Environment e = new Environment();
        e.put("#t", new SchemeBoolean(true));
        e.put("#f", new SchemeBoolean(false));
        e.put("if", new If());
        e.put("and", new And());
        e.put("or", new Or());
        e.put("not", new Not());
        e.put("define", new Define());
        e.put("lambda", new Lambda());
        e.put("let", new Let());
        e.put("let*", new LetStar());
        e.put("letrec", new LetRec());
        e.put("quote", new Quote());
        e.put(">=", new Geq());
        e.put("<=", new Leq());
        e.put(">", new Gt());
        e.put("<", new Lt());
        e.put("=", new EqualSymbol());
        e.put("'", new Quote());
        e.put("+", new Add());
        e.put("-", new Sub());
        e.put("*", new Mul());
        e.put("list", new dk.atomit.Jheme.StdLib.List());
        e.put("car", new Car());
        e.put("cdr", new Cdr());
        e.put("cons", new Cons());
        e.put("integer?", new IsInteger());
        e.put("pair?", new IsPair());
        e.put("printf", new PrintF());
        e.put("begin", new Begin());
        return e;

    }

    @Override
    public String toString(){
        return env.toString() + "\n" + ((outer == null) ? "" : outer.toString(1));
    }

    public String toString(int depth){
        String tabs = "";
        for(int i=0; i<depth; i++){
            tabs += "\t";
        }
        return tabs + env.toString() + "\n" +  ((outer == null) ? "" : outer.toString(depth + 1));
    }
}
