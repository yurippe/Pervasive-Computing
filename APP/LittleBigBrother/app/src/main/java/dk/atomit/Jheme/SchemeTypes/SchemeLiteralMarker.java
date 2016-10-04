package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeLiteralMarker implements SchemeObject, HasValue<String>{

    public String getValue(){return "'";}

    public String getStringValue(){return "'";}
}
