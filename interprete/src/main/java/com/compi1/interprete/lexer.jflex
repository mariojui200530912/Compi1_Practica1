package com.compi1.interprete;
import java_cup.runtime.*;

%%

%class Lexer
%public
%line
%column
%cup
%caseless

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

// ----- Expresiones Regulares
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Digit          = [0-9]
Integer        = {Digit}+
Decimal        = {Digit}+ "." {Digit}+
Identifier     = [a-zA-Z][a-zA-Z0-9_]*
HexColor       = H[0-9a-fA-F]{6}
Comment        = "#" [^\r\n]*

%%

// ------- Seccion 1 --- Palabras Reservadas Pseudocodigo

"INICIO"            { return symbol(sym.INICIO); }
"FIN"               { return symbol(sym.FIN); }
"VAR"               { return symbol(sym.VAR); }
"SI"                { return symbol(sym.SI); }
"ENTONCES"          { return symbol(sym.ENTONCES); }
"FIN SI"            { return symbol(sym.FIN_SI); }
"MIENTRAS"          { return symbol(sym.MIENTRAS); }
"HACER"             { return symbol(sym.HACER); }
"FIN MIENTRAS"      { return symbol(sym.FIN_MIENTRAS); }
"MOSTRAR"           { return symbol(sym.MOSTRAR); }
"LEER"              { return symbol(sym.LEER); }

// Separador de secciones
"%%%%"              { return symbol(sym.SEPARADOR); }

// Operadores Aritmeticos
"+"                 { return symbol(sym.SUMA, yytext()); }
"-"                 { return symbol(sym.RESTA, yytext()); }
"*"                 { return symbol(sym.MULT, yytext()); }
"/"                 { return symbol(sym.DIV, yytext()); }
"("                 { return symbol(sym.LPAREN); }
")"                 { return symbol(sym.RPAREN); }

// Operadores Relacionales y Logicos
"=="                { return symbol(sym.IGUALDAD); }
"!="                { return symbol(sym.DIFERENTE); }
">="                { return symbol(sym.MAYOR_IGUAL); }
"<="                { return symbol(sym.MENOR_IGUAL); }
">"                 { return symbol(sym.MAYOR); }
"<"                 { return symbol(sym.MENOR); }
"&&"                { return symbol(sym.AND); }
"||"                { return symbol(sym.OR); }
"!"                 { return symbol(sym.NOT); }
"="                 { return symbol(sym.ASIGNAR); }

// --- Seccion 2 -- Configuracion

"%DEFAULT"          { return symbol(sym.OPC_DEFAULT); }
"%COLOR_TEXTO_SI"   { return symbol(sym.OPC_COLOR_TXT_SI); }
"%COLOR_SI"         { return symbol(sym.OPC_COLOR_SI); }
"%FIGURA_SI"        { return symbol(sym.OPC_FIG_SI); }
"%LETRA_SI"         { return symbol(sym.OPC_LET_SI); }
"%LETRA_SIZE_SI"    { return symbol(sym.OPC_LET_SIZE_SI); }

"%COLOR_TEXTO_MIENTRAS" { return symbol(sym.OPC_COLOR_TXT_MIE); }
"%COLOR_MIENTRAS"       { return symbol(sym.OPC_COLOR_MIE); }
"%FIGURA_MIENTRAS"      { return symbol(sym.OPC_FIG_MIE); }
"%LETRA_MIENTRAS"       { return symbol(sym.OPC_LET_MIE); }
"%LETRA_SIZE_MIENTRAS"  { return symbol(sym.OPC_LET_SIZE_MIE); }

"%COLOR_TEXTO_BLOQUE"   { return symbol(sym.OPC_COLOR_TXT_BLOQ); }
"%COLOR_BLOQUE"         { return symbol(sym.OPC_COLOR_BLOQ); }
"%FIGURA_BLOQUE"        { return symbol(sym.OPC_FIG_BLOQ); }
"%LETRA_BLOQUE"         { return symbol(sym.OPC_LET_BLOQ); }
"%LETRA_SIZE_BLOQUE"    { return symbol(sym.OPC_LET_SIZE_BLOQ); }

// Figuras
"ELIPSE"                { return symbol(sym.FIG_ELIPSE); }
"CIRCULO"               { return symbol(sym.FIG_CIRCULO); }
"PARALELOGRAMO"         { return symbol(sym.FIG_PARALELO); }
"RECTANGULO"            { return symbol(sym.FIG_RECT); }
"ROMBO"                 { return symbol(sym.FIG_ROMBO); }
"RECTANGULO_REDONDEADO" { return symbol(sym.FIG_RECT_RED); }

"ARIAL"                 { return symbol(sym.LET_ARIAL); }
"TIMES_NEW_ROMAN"       { return symbol(sym.LET_TIMES); }
"COMIC_SANS"            { return symbol(sym.LET_COMIC); }
"VERDANA"               { return symbol(sym.LET_VERDANA); }

// Literales generales
{Integer}           { return symbol(sym.ENTERO, yytext()); }
{Decimal}           { return symbol(sym.DECIMAL, yytext()); }
{HexColor}          { return symbol(sym.HEX, yytext()); }
{Identifier}        { return symbol(sym.ID, yytext()); }
\"[^\"]*\"          { return symbol(sym.CADENA, yytext()); }
"|"                 { return symbol(sym.PIPE); }
","                 { return symbol(sym.COMA); }

{Comment}           { /* Ignorar comentarios  */ }
{WhiteSpace}        { /* Ignorar blancos */ }

// Error Lexico
[^] {
    return symbol(sym.ERROR_LEXICO, yytext());
}