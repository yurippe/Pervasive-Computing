package dk.atomit.Jheme.Interpreter;

import dk.atomit.Jheme.Environment.Environment;
import dk.atomit.Jheme.SchemeTypes.SchemeNoreturn;
import dk.atomit.Jheme.SchemeTypes.SchemeObject;

/**
 * Created by Kristian on 6/8/2016.
 */
public class EvaluationResult {

    private SchemeObject schemeObject;
    private Environment environment;

    public EvaluationResult(SchemeObject obj, Environment env){
        this.schemeObject = obj;
        this.environment = env;
    }

    public EvaluationResult(Environment env){
        this.schemeObject = new SchemeNoreturn();
        this.environment = env;
    }

    public Environment getEnvironment(){
        return environment;
    }

    public SchemeObject getSchemeObject(){
        return schemeObject;
    }
}
