package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 10/5/2016.
 */
public class SchemeJavaObject implements SchemeObject, HasValue<Object> {

    private Object obj;

    public SchemeJavaObject(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object getValue() {
        return this.obj;
    }

    @Override
    public String getStringValue() {
        return this.obj.toString();
    }
    
}
