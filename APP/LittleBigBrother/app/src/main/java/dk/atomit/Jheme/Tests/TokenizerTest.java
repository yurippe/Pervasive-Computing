package dk.atomit.Jheme.Tests;

import dk.atomit.Jheme.Parser.Parser;
import dk.atomit.Jheme.Parser.Tokenizer;

/**
 * Created by Kristian on 6/11/2016.
 */
public class TokenizerTest {

    public static void main(String[] args){
        Tokenizer tok = new Tokenizer();

        for(String t : tok.tokenize("'a(hello werld)")){
            System.out.println(t);
        }

        Parser p = new Parser();
        p.printParsed("'a(hello werld)");
    }
}
