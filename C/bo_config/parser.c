#include <stdio.h>

#include <bo_config/scanner.h>
#include <bo_config/semantics-processor.h>
#include <bo_config/context-loader.h>

/*
Grammar:
	Look at tokens for the terminals.

	PROGRAM		-> STATEMENT_LIST END_OF_FILE
	STATEMENT_LIST	-> STATEMENT {STATEMENT}
	STATEMENT	-> KEYWORD ASSIGN_OP LITERAL END_OF_LINE
	KEYWORD		-> DB
	KEYWORD		-> URL_OPEN_CMD
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

static int token_is(TOKEN_TYPE type){
	return get_token_type(token) == type;
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
static void keyword();
static void assign_op();
static void literal();

int main(){
	context_t* context = context_malloc();

	load_context(context, stdin);

	printf("DB: %s\nURL_OPEN_CMD: %s\n", get_db(context), get_url_open_cmd(context));

	context_free(context);
	return 0;
}

static context_t * context_ptr = NULL;

static stack_t * semantic_stack_ptr = NULL;
#define save(X) push(semantic_stack_ptr, (X))

int load_context(context_t* context, FILE* file){
	semantic_stack_ptr = stack_malloc();

	if(context == NULL || file == NULL){
		return 0;
	}

	context_ptr = context;
	set_scanner_input(file);
	next_token();
	program();

	stack_free(semantic_stack_ptr);
	semantic_stack_ptr = NULL;
	return 1;
}


static void program(){
	statement_list();

	if(!token_is(END_OF_FILE)){
		parse_error();
	}

	next_token();
}

static void statement_list(){
	statement();
	while(token_is(DB) || token_is(URL_OPEN_CMD)){
		statement();
	}
}

static void statement(){
	keyword();
	assign_op();
	literal();

	if(!token_is(END_OF_LINE))
		parse_error();

	if(!valid_assignment(semantic_stack_ptr)){
		fprintf(stderr, "Semantic Error: Invalid assignment.\n");
	}

	perform_assignment(context_ptr, semantic_stack_ptr);

	next_token();	
}

static void keyword(){
	switch(get_token_type(token)){
		case DB	:
		case URL_OPEN_CMD :	save(token); next_token(); return;
		default :		parse_error();
	}
}

static void assign_op(){
	if(token_is(EQUALS) || token_is(SPACE))
		next_token();
	else
		parse_error();
}

static void literal(){
	if(token_is(INT_LITERAL) || token_is(STRING_LITERAL) || token_is(PATH_LITERAL)){
		save(token);
		next_token();
	} else {
		parse_error();
	}
}

#undef save
