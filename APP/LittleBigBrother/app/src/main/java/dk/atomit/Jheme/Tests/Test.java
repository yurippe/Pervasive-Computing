package dk.atomit.Jheme.Tests;

import dk.atomit.Jheme.Interpreter.Interpreter;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Test {

    public static void main(String[] args) {

        try {
            assert false;
            System.out.println("Assertions not enabled, unit tests not verified");
        } catch (AssertionError e){
            LambdaTests.test();
            ListTests.test();
            System.out.println("PASSED ALL TESTS");
        }


        Interpreter i = new Interpreter();
        System.out.println("Jheme <version " + Interpreter.VERSION + ">\n");
        i.repl(">> ");
    }

}
