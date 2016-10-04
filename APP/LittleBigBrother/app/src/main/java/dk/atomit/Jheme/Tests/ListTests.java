package dk.atomit.Jheme.Tests;

import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeInteger;
import dk.atomit.Jheme.SchemeTypes.SchemePair;

/**
 * Created by Kristian on 6/8/2016.
 */
public class ListTests {

    public static void test(){
        Interpreter interpreter = new Interpreter();

        interpreter.eval("(define t1 (list 1 2 3 4 (list 5 6 7) 8 9))");
        SchemeInteger value;
        value = (SchemeInteger) interpreter.eval("(car t1)").getSchemeObject();
        assert value.getValue() == 1;
        value = (SchemeInteger) interpreter.eval("(car (cdr t1))").getSchemeObject();
        assert value.getValue() == 2;
        value = (SchemeInteger) interpreter.eval("(car (cdr (cdr t1)))").getSchemeObject();
        assert value.getValue() == 3;
        value = (SchemeInteger) interpreter.eval("(car (car (cdr (cdr (cdr (cdr t1))))))").getSchemeObject();
        assert value.getValue() == 5;
        value = (SchemeInteger) interpreter.eval("(car (cdr (car (cdr (cdr (cdr (cdr t1)))))))").getSchemeObject();
        assert value.getValue() == 6;
        SchemePair pair = (SchemePair) interpreter.eval("(cdr (cdr (cdr (car (cdr (cdr (cdr (cdr t1))))))))").getSchemeObject();
        assert pair.isNull();
    }

}
