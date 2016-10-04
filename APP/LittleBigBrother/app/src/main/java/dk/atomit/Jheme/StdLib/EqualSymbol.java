package dk.atomit.Jheme.StdLib;

/**
 * Created by Kristian on 6/11/2016.
 */
public class EqualSymbol extends Comparator{

    @Override
    protected boolean compare(double x, double y) {
        return x == y;
    }
}
