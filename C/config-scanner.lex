%option noyywrap
%x str
%{
#include "config-grammer.h"

#define YY_DECL token_t yylex()
%}

DIGIT						[0-9]
U_CASE_LETTER					[A-Z]
LETTER						[a-z]|{U_CASE_LETTER}
PRINTABLE_CHAR					[a-zA-Z0-9]|"_"|"-"|"/"
PATH						"/"{LETTER}+(("_"|"-"|"/"){LETTER}*)*					
%%
%{
#define BO_CFG_TOKEN(X) token_construct((X), NULL, 0)
#define BO_CFG_TOKEN_2(X,Y) token_construct((X), (Y), strlen(Y))
#define BO_CFG_TOKEN_3(X,Y) token_construct((X), (Y), strlen(Y) - 1)

/* Taken from the fles manual: Start Constitons around 74%. */
char string_buf[150];
char *string_buf_ptr;
%}


[\t]+					{ /* Remove white space. */ }

{DIGIT}+				return BO_CFG_TOKEN_2(INT_LITERAL, yytext);
{PATH}					return BO_CFG_TOKEN_2(PATH_LITERAL, yytext);
{U_CASE_LETTER}{2,}(_{U_CASE_LETTER}+)*	return BO_CFG_TOKEN_2(KEYWORD, yytext);


\"					string_buf_ptr = string_buf; BEGIN(str);
<str>{PATH}\"				{
					BEGIN(INITIAL);
					*string_buf_ptr = '\0';
					return BO_CFG_TOKEN_3(PATH_LITERAL, yytext);
					}
<str>{PRINTABLE_CHAR}*\"		{
					BEGIN(INITIAL);
					*string_buf_ptr = '\0';
					return BO_CFG_TOKEN_3(STRING_LITERAL, yytext);
					}
<str>\n					return BO_CFG_TOKEN(ERROR);

"="					return BO_CFG_TOKEN(EQUALS);
" "					return BO_CFG_TOKEN(SPACE);
\n					return BO_CFG_TOKEN(END_OF_LINE);
.					return BO_CFG_TOKEN(ERROR);
<<EOF>>					return BO_CFG_TOKEN(END_OF_FILE);

%%

#undef BO_CFG_TOKEN
#undef BO_CFG_TOKEN_2
#undef BO_CFG_TOKEN_3

int main(){
	token_t token;
	yyin = stdin;

	while((token = yylex()).type != END_OF_FILE){
		printf("Token: %d - %s\n", token.type, token.content);
	}

	return 0;
}
