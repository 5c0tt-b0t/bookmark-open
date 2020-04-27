#ifndef BO_CONFIG_SEMANTIC_STACK
#define BO_CONFIG_SEMANTIC_STACK

#include <bo_config/tokens.h>

struct stack_struct;

typedef struct stack_struct stack_t;

stack_t * stack_malloc();

void stack_free(stack_t * stack);

int push(stack_t * stack, const token_t * const token);

token_t * pop(stack_t * stack);

void empty(stack_t * stack);

#endif
