package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/11/2016.
 */
public class SchemeUninitialized implements SchemeObject {
    @Override
    public String getStringValue() {
        throw new RuntimeException("Attempt to reference undefined variable");
    }
}
