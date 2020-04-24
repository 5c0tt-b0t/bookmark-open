%option noyywrap
%x str
%{
#include "config-scanner.h"

struct token_struct{
	TOKEN_TYPE type;
	char * content;
};

#define YY_DECL struct token_struct yylex()

static int line = 1, column = 1;
#define YY_USER_ACTION column += yyleng;

TOKEN_TYPE get_token_type(struct token_struct * token){
	return token -> type;
}

char * get_token_content(struct token_struct * token){
	return token -> content;
}

static struct token_struct token_construct(TOKEN_TYPE type, char * content, int length);

static void token_destruct(token_t * token);

%}

DIGIT						[0-9]
U_CASE_LETTER					[A-Z]
LETTER						[a-z]|{U_CASE_LETTER}
PRINTABLE_CHAR					[a-zA-Z0-9]|"_"|"-"|"/"
PATH						"/"{LETTER}+(("_"|"-"|"/"){LETTER}*)*					
%%
%{
#define BO_CFG_TOKEN(X) token_construct((X), NULL, 0)
#define BO_CFG_TOKEN_2(X) token_construct((X), yytext, yyleng)
#define BO_CFG_TOKEN_3(X) token_construct((X), yytext, (yyleng) - 1)

/* Taken from the fles manual: Start Constitons around 74%. */
char string_buf[150];
char *string_buf_ptr;
%}


[\t]+					{ /* Remove white space. */ }

{DIGIT}+				return BO_CFG_TOKEN_2(INT_LITERAL);
{PATH}					return BO_CFG_TOKEN_2(PATH_LITERAL);
{U_CASE_LETTER}{2,}(_{U_CASE_LETTER}+)*	return BO_CFG_TOKEN_2(KEYWORD);


\"					string_buf_ptr = string_buf; BEGIN(str);
<str>{PATH}\"				{
					BEGIN(INITIAL);
					*string_buf_ptr = '\0';
					return BO_CFG_TOKEN_3(PATH_LITERAL);
					}
<str>{PRINTABLE_CHAR}*\"		{
					BEGIN(INITIAL);
					*string_buf_ptr = '\0';
					return BO_CFG_TOKEN_3(STRING_LITERAL);
					}
<str>\n					{
					line++; column = 1;
					fprintf(stderr,
						"Scanning error: (%d:%d)"
						" unterminated string.\n",
						line, column
						);
					return BO_CFG_TOKEN(ERROR);
					}

"="					return BO_CFG_TOKEN(EQUALS);
" "					return BO_CFG_TOKEN(SPACE);
\n					line++; column = 1; return BO_CFG_TOKEN(END_OF_LINE);
.					{
					fprintf(stderr,
						"Scanning error: (%d:%d) %s\n",
						line, column, yytext
						);
					return BO_CFG_TOKEN(ERROR);
					}
<<EOF>>					return BO_CFG_TOKEN(END_OF_FILE);

%%

#undef BO_CFG_TOKEN
#undef BO_CFG_TOKEN_2
#undef BO_CFG_TOKEN_3

struct token_struct * token_malloc(){
	struct token_struct *token=(struct token_struct *) malloc(sizeof(struct token_struct));
	return token;
}

void token_free(struct token_struct * token){
	token_destruct(token);
}

static struct token_struct token_construct(TOKEN_TYPE type, char * content, int length){
	token_t token;

	if(type < 0 || type > TOKEN_TYPES || type == INVALID_TOKEN){
		/* Not a valid token. */
		token.type = INVALID_TOKEN;
		return token;
	}

	token.type = type;
	if(content != NULL && length > 0){
		token.content = (char *) malloc(sizeof(char) * length);
		strncpy(token.content, content, length);
	} else {
		token.content = NULL;
	}

	return token;
}

static void token_destruct(struct token_struct * token){
	token -> type = INVALID_TOKEN;
	if(token -> content != NULL){
		free(token -> content);
		token -> content = NULL;
	}
}


struct token_struct * get_next_token(){
	static struct token_struct token = {INITIAL_TOKEN, NULL};
	yyin = stdin;

	if(token.type == END_OF_FILE){
		return &token;
	}

	token_destruct(&token);
	token = yylex();

	return &token;
}
