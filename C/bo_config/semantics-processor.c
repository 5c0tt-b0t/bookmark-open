#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <bo_config/semantics-processor.h>
#include <bo_config/context-function-mapper.h>

#define SIZE 2

struct stack_struct{
	unsigned int top;
	token_t * data[SIZE];
};

stack_t * stack_malloc(){
	stack_t * stack = (stack_t *) malloc(sizeof(stack_t));
	stack -> top = 0u;
	return stack;
}

void stack_free(stack_t * stack){
	if(stack == NULL)
		return;
	if(stack -> data != NULL){
		token_free(stack->data[0]);
		stack -> data[0] = NULL;
		token_free(stack->data[1]);
		stack -> data[1] = NULL;
	}
	stack -> top = 0;
	free(stack);
}

int push(stack_t * stack, const token_t * const token){
	if(token == NULL || stack == NULL)
		return 0;
	else if(stack -> top >= SIZE){
		fprintf(stderr, "Semantic analyser error: Stack full.\n");
		return 0;
	}

	stack -> data[stack -> top] = token_duplicate(token);
	(stack -> top)++;
	return 1;

}

/* Call token_free() to free memory for the popped token. */
token_t * pop(stack_t * stack){
	token_t * token = NULL;
	if(stack -> top > 0){
		token = stack -> data[stack -> top];
		(stack -> top)--;
	}
	return token;
}

void empty(stack_t * stack){
	pop(stack);
	pop(stack);
}

#define FIRST_TOKEN_TYPE(X) get_token_type((X) -> data[0])
#define SECOND_TOKEN_TYPE(X) get_token_type((X) -> data[1])
int valid_assignment(stack_t * stack){
	/* Valid sequences:					*/
	/*	FIRST_TOKEN_TYPE:	SECOND_TOKEN_TYPE:	*/
	/*	DB			PATH_LITERAL		*/
	/*	URL_OPEN_CMD		STRING_LITERAL		*/

	switch(FIRST_TOKEN_TYPE(stack)){
		case DB:		return SECOND_TOKEN_TYPE(stack) == PATH_LITERAL;
		case URL_OPEN_CMD:	return SECOND_TOKEN_TYPE(stack) == STRING_LITERAL;
		default:		return 0;
	}

}

void perform_assignment(context_t* context, stack_t* stack){
	char * value = get_token_content(stack -> data[1]);
	get_setter_function(FIRST_TOKEN_TYPE(stack))(context, value, strlen(value));
	empty(stack);
}

#undef FIRST_TOKEN_TYPE
#undef SECOND_TOKEN_TYPE

#undef SIZE
