package dk.atomit.Jheme.Parser;

import dk.atomit.Jheme.SchemeTypes.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Created by Kristian on 6/7/2016.
 */
public class Parser {

    private boolean DEBUG;

    public Parser(){
        this(false);
    }
    public Parser(boolean debug){
        DEBUG = debug;
    }

    private SchemeObject readTokens(Deque<String> tokens){
        if(tokens.isEmpty()){
            throw new RuntimeException("Unexpected EOF while parsing");
        }

        String token = tokens.pop();

        if(token.equals(")")){
            throw new RuntimeException("Unexpected ')' while parsing");
        } else if (token.equals("(")){
            SchemeExpression exp = new SchemeExpression();
            while(!tokens.peek().equals(")")){
                exp.append(readTokens(tokens));
            }
            tokens.pop(); //remove the parenthesis
            return exp;
        } else {
            //it is not an expression, so it is an atom
            return atomize(token);
        }


    }

    public SchemeObject atomize(String s){
        if(s.equals("'")){
            //is the literal marker
            return new SchemeLiteralMarker();
        } else if(s.matches("-?\\d+")){
            //is integer:
            return new SchemeInteger(s);
        } else if(s.matches("-?\\d+(\\.\\d+)")){
            //is float / double:
            return new SchemeFloat(s);
        } else if(s.matches("\\\".*\\\"")){
           //is a string:
            return new SchemeString(s.substring(1,s.length()-1));
        }
        else {
            return new SchemeSymbol(s);
        }
    }

    private Deque<String> dequeifyTokens(String[] sarr){
        Deque<String> d = new ArrayDeque<>();
        for(String s:sarr){
            d.add(s);
        }
        return d;
    }

    public String[] tokenize(String in){
        Tokenizer t = new Tokenizer();
        return t.tokenize(in);
    }

    @Deprecated
    public String[] deprecated_tokenize(String in){

        in = in.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        in = in.replace((CharSequence) "(", (CharSequence) " ( ");
        in = in.replace((CharSequence) ")", (CharSequence) " ) ");
        in = in.replace((CharSequence) "[", (CharSequence) " ( ");
        in = in.replace((CharSequence) "]", (CharSequence) " ] ");
        in = in.replace((CharSequence) "(", (CharSequence) " ( ");
        in = in.replace((CharSequence) "'", (CharSequence) " ' ");
        in = in.trim();

        String[] ret = in.split("\\s+");
        if(DEBUG){System.out.println(ret);}
        return ret;
    }

    private static String stripComments(String input){
        char[] inp = input.toCharArray();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int c = 0;
        boolean w = false;

        while(i < inp.length){
            if(w){
                if(inp[i] == '\n'){
                    w = false;
                    c = i;
                    continue;
                }
            }

            if(inp[i] == ';'){
                sb.append(input.substring(c,i));
                i++;
                w = true;
                continue;
            }

            i++;
        }

        if(c < i && !w){
            sb.append(input.substring(c,i));
        }

        return sb.toString();
    }

    public List<SchemeObject> parse(String input){
        List<SchemeObject> r = new ArrayList<>();
        Deque<String> tokens = dequeifyTokens(tokenize(stripComments(input)));
        while(!tokens.isEmpty()){
            r.add(readTokens(tokens));
        }
        return r;
    }

    public void printParsed(String input){
        System.out.println(parse(input));
        /*
        for(SchemeObject o : parse(input)){
            if(o instanceof SchemeExpression){
                printExpression((SchemeExpression) o, false);
            } else {
                System.out.println(o);
            }
        }*/
    }

    private void printExpression(SchemeExpression e, boolean pretty){
        if(!pretty){
            System.out.println(e);
            return;
        }
        for(SchemeObject o: e.list()){
            if(o instanceof SchemeExpression){
                System.out.println("(");
                printExpression((SchemeExpression) o, pretty);
                System.out.println(")");


            } else if(o instanceof HasValue) {
                System.out.println(((HasValue<?>) o).getValue());
            } else {
                System.out.println(o);
            }
        }
    }

    public void prettyprintParsed(String input){
        for(SchemeObject o : parse(input)){
            if(o instanceof SchemeExpression){
                System.out.println("(");
                printExpression((SchemeExpression) o, true);
                System.out.println(")");
            } else if(o instanceof HasValue) {
                System.out.println(((HasValue<?>) o).getValue());
            } else {
                System.out.println(o);
            }
        }
    }
}
