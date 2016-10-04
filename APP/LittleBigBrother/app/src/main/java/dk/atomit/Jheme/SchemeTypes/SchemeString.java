package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeString implements SchemeObject, HasValue<String> {

    private String value;

    public SchemeString(String s){
        value = s;
    }

    public String getValue() {
        return value;
    }

    public String getStringValue(){
        return "\"" + value + "\"";
    }
}
