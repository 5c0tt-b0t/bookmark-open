#include <stdio.h>
#include "config-scanner.h"

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

static token_t * token = NULL;

static void next_token(){
	token = get_next_token();
}

static int peek(TOKEN_TYPE type){
	return get_token_type(token) == type;
}

static int match(TOKEN_TYPE type){
	const int is_a_match =  get_token_type(token) == type;
	if(is_a_match)
		next_token();

	return is_a_match;
}

static void parse_error(){
	fprintf(stderr, "Parsing Error: Could not match token:{%d, %s}\n",
		get_token_type(token),
		get_token_content(token)
	);
}

/* Defining prototypes purely for readability of the code.*/
static void program();
static void statement_list();
static void statement();
static void assign_op();
static void literal();

int main(){
	next_token();
	program();
}

static void program(){
	statement_list();

	if(!match(END_OF_FILE)){
		parse_error();
	}
}

static void statement_list(){
	statement();
	while(peek(KEYWORD)){
		statement();
	}
}

static void statement(){
	if(!match(KEYWORD))
		parse_error();

	assign_op();

	literal();

	if(!match(END_OF_LINE))
		parse_error();
}

static void assign_op(){
	if(peek(EQUALS) || peek(SPACE))
		next_token();
	else
		parse_error();
}

static void literal(){
	if(peek(INT_LITERAL) || peek(STRING_LITERAL) || peek(PATH_LITERAL))
		next_token();
	else
		parse_error();
}
