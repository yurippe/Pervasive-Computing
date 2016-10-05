package dk.atomit.Jheme.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kristian on 6/10/2016.
 */
public class Tokenizer {

    private enum State {INITIAL, STANDARD, STRING}

    private State state;

    private static final Pattern QUOTES_PATTERN_1 = Pattern.compile("('\\s*\\([^\\)]*\\))");
    private static final Pattern QUOTES_PATTERN_2 = Pattern.compile("('\\s*[^\\(\\)]\\s)");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public Tokenizer(){state = State.INITIAL;}

    private String fix(String in){

        in = in.replace('\r', ' ');
        in = in.replace('\n', ' ');

        Matcher m = WHITESPACE.matcher(in);
        m.replaceAll(" ");

        m = QUOTES_PATTERN_1.matcher(in);
        while (m.find()){
            String s = m.group();
            in = m.replaceFirst("(quote " + s.substring(1).trim() + ")");
            m.reset(in);
        }

        m = QUOTES_PATTERN_2.matcher(in);
        while (m.find()){
            String s = m.group();
            in = m.replaceFirst("(quote " + s.substring(1).trim() + ")");
            m.reset(in);
        }

        /*
        System.out.println("TOKENS");
        System.out.println(in);
        */
        return in;
    }

    public String[] tokenize(String string_in){

        string_in = fix(string_in);

        char[] input = string_in.toCharArray();

        List<String> output = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        char prev = ' ';


        for(char c : input){

            if(state == state.INITIAL){

                if(c == ')' || c == ']'){
                    output.add(")");
                } else if (c == '(' || c == '[') {
                    output.add("(");
                } else if (c == '\''){
                    output.add("'");
                    state = State.INITIAL;
                }

                else if(!(c == ' ')){
                    currentToken = new StringBuilder();
                    currentToken.append(c);
                    if(StartNewString(c)){
                        state = State.STRING;
                    } else{
                        state = State.STANDARD;
                    }

                }

                prev = c;
                continue;
            }

            else if(state == State.STANDARD){

                if(c == ')' || c == ']'){
                    output.add(currentToken.toString());
                    output.add(")");
                    state = State.INITIAL;
                } else if (c == '(' || c == '[') {
                    output.add(currentToken.toString());
                    output.add("(");
                    state = State.INITIAL;
                } else if (c == '\''){
                    output.add(currentToken.toString());
                    output.add("'");
                    state = State.INITIAL;
                }

                else if(!(c == ' ')){
                    currentToken.append(c);
                } else {
                    //end of this token
                    output.add(currentToken.toString());
                    state = State.INITIAL;
                }

                prev = c;
                continue;
            }

            else if(state == State.STRING){

                currentToken.append(c);

                if(EndString(prev, c)){
                    output.add(currentToken.toString());
                    state = State.INITIAL;
                }

                prev = c;
                continue;
            }



        }

        if(!(state == State.INITIAL)){
            output.add(currentToken.toString());
        }

        return output.toArray(new String[output.size()]);

    }

    private boolean StartNewString(char c){
        return c == '"';
    }

    private boolean EndString(char prev, char c){
        return (!(prev == '\\')) && (c == '"');
    }
}
