package dk.atomit.Jheme.Tests;

import dk.atomit.Jheme.Interpreter.Interpreter;
import dk.atomit.Jheme.SchemeTypes.SchemeInteger;

/**
 * Created by Kristian on 6/8/2016.
 */
public class LambdaTests {

    public static void test(){
        Interpreter interpreter = new Interpreter();

        SchemeInteger a1 = (SchemeInteger) interpreter.eval("(((lambda (x) (lambda (x y z) (+ x y z))) 10) 1 2 3)").getSchemeObject();
        assert a1.getValue() == 6;
        SchemeInteger a2 = (SchemeInteger) interpreter.eval("(define x (lambda (x) (lambda (x) (+ x x)))) ((x 10) 20)").getSchemeObject();
        assert a2.getValue() == 40;
        SchemeInteger a3 = (SchemeInteger) interpreter.eval("(define y (lambda (x) x))(y 10)").getSchemeObject();
        assert a3.getValue() == 10;
        SchemeInteger a4 = (SchemeInteger) interpreter.eval("(define x (lambda x (+ (car x) (car (cdr x)))))(x 1 2)").getSchemeObject();
        assert a4.getValue() == 3;
        SchemeInteger a5 = (SchemeInteger) interpreter.eval("(x 9 1 #f 3)").getSchemeObject();
        assert a5.getValue() == 10;

        interpreter.eval("(define x (lambda (x) (+ 10 x)))");
        SchemeInteger a6 = (SchemeInteger) interpreter.eval("(let ([y x] [c (lambda (y) (+ y 100))]) (y (c 1)))").getSchemeObject();
        assert a6.getValue() == 111;

        SchemeInteger a7 = (SchemeInteger) interpreter.eval("(letrec ([x 10] [y x]) y)").getSchemeObject();
        assert a7.getValue() == 10;

        try{
            interpreter.eval("(letrec ([y x] [x 10]) y)");
            assert false;
        } catch (RuntimeException e){
            assert true;
        }

        SchemeInteger a8 = (SchemeInteger) interpreter.eval("(let* ([x 10] [y x] [x 12]) (+ x y))").getSchemeObject();
        assert a8.getValue() == 22;
    }

}
