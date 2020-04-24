#ifndef CONFIG_GRAMMER
#define CONFIG_GRAMMER

/*
Grammar:
	Look at tokens for the terminals.

	PROGRAM		-> STATEMENT_LIST END_OF_FILE
	STATEMENT_LIST	-> STATEMENT {STATEMENT}
	STATEMENT	-> KEYWORD ASSIGN_OP LITERAL END_OF_LINE
	ASSIGN_OP	-> EQUALS
	ASSIGN_OP	-> SPACE
	LITERAL		-> INT_LITERAL
	LITERAL		-> STRING_LITERAL
	LITERAL		-> PATH_LITERAL
*/

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
	INITIAL_TOKEN,
	TOKEN_TYPES
} TOKEN_TYPE;

struct token_struct;

typedef struct token_struct token_t;

token_t * token_malloc();

void token_free(token_t * token);

token_t * get_next_token();

TOKEN_TYPE get_token_type(token_t * token);

char * get_token_content(token_t * token);

#endif
