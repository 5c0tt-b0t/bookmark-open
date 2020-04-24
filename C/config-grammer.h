#ifndef CONFIG_GRAMMER
#define CONFIG_GRAMMER

#include <string.h>
#include <stdlib.h>

typedef enum {
	END_OF_FILE,
	ERROR,
	KEYWORD,
	INT_LITERAL,
	STRING_LITERAL,
	PATH_LITERAL,
	EQUALS,
	SPACE,
	END_OF_LINE,
	INVALID_TOKEN,
	TOKEN_TYPES
} TOKEN_TYPE;


/*
Grammar:
	Look at tokens for the terminals.

	PROGRAM		-> STATEMENT STATEMENT_TAIL END_OF_FILE
	STATEMENT	-> KEYWORD ASSIGN_OP LITERAL END_OF_LINE
	STATEMENT_TAIL	-> lambda
	STATEMENT_TAIL	-> STATEMENT STATEMENT_TAIL
	ASSIGN_OP	-> EQUALS
	ASSIGN_OP	-> SPACE
	LITERAL		-> INT_LITERAL
	LITERAL		-> STRING_LITERAL
*/


typedef struct{
	TOKEN_TYPE type;
	char * content;
} token_t;

token_t token_construct(TOKEN_TYPE type, char * content, int length);

token_t token_construct(TOKEN_TYPE type, char * content, int length){
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
	}

	return token;
}

void token_free(token_t * token){
	token -> type = TOKEN_TYPES;
	free(token -> content);
	token -> content = NULL;
}

#endif
