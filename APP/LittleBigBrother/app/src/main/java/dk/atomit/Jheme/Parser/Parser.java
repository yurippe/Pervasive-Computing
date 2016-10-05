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
            String subs = s.substring(1,s.length()-1);
            return new SchemeString(unescapeJavaString(subs));
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


    /**
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     *
     * @param st
     *            A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    public String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
