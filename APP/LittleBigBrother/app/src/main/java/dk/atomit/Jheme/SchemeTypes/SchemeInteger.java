package dk.atomit.Jheme.SchemeTypes;

/**
 * Created by Kristian on 6/7/2016.
 */
public class SchemeInteger implements SchemeObject, HasValue<Integer>, SchemeNumber {

    private Integer value;

    public SchemeInteger(String i){
        value = Integer.parseInt(i);
    }

    public SchemeInteger(int i){
        value = i;
    }

    public Integer getValue(){
        return value;
    }

    public String getStringValue(){
        return value.toString();
    }

    @Override
    public Double getNumericValue() {
        return value.doubleValue();
    }
}
