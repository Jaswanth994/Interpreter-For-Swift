package com.craftinginterpreters.swift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.swift.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("else if",ELSE_IF);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("let",    LET);
        keywords.put("in",     IN);
        keywords.put("repeat", REPEAT_WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }
    // scanning tokens one by one.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    //function to add token.
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': {
                            if(match('.')){ if(match('.')) addToken(DOTDOT);} 
                            else addToken(DOT); 
                      }break;
            case '-': {
                if(match('=')){
                    addToken(MINUS_EQUAL); 
                }else{
                   addToken(MINUS); break;}
            }break;
            case '+':{
                if(match('=')){
                    addToken(PLUS_EQUAL);
                } else addToken(PLUS); 
            }break;
            case ';': addToken(SEMICOLON); break;
            case '*':{if(match('=')){
                       addToken(EQUAL);
                       addToken(STAR);
            }else addToken(STAR); 
            }break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    //single line comment.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } 
                //multi line comment.
                else if(match('*')){
                    while(peek()!='*' && !isAtEnd()) advance();
                    if(match('*')) {
                        if(match('/')) advance();
                        else Swift.error(line, "Unterminated '/*' comment");}
                }
                else if(match('=')){
                    addToken(SLASH_EQUAL);
                }
                else {
                    addToken(SLASH);
                }
                break;

            case ' ': break;

            case '\r': break;

            case '\t': break;

                // Ignore whitespace.
            //moves to next line    
            case '\n': line++; break;
            
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Swift.error(line, "Unexpected character.");
                }
                break;
        }
    }
      // See if the identifier is a reserved word.
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    // verifies whether it is a number or not
    private void number() {
         
        String str=source.substring(start,current);
       
        while (isDigit(peek())){
            int len=0; 
            int start2=current;
            while(peek()=='_'){
                len++;
                advance();
            }
            str=str+source.substring(start2+len, current+1);
            advance();
        }
        int start3=current;
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
           while (isDigit(peek())) advance();
        }
        str=str+source.substring(start3, current);

        addToken(NUMBER, Double.parseDouble(str));
    }



    //verification for strings
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            Swift.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9' )|| c=='_';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }
    //adds token using the below two polymorfic funcn
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}