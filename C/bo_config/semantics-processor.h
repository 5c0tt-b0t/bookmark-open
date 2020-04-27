#ifndef BO_CONFIG_SEMANTIC_ANALYSER
#define BO_CONFIG_SEMANTIC_ANALYSER

#include <context.h>
#include <bo_config/semantic-stack.h>

int valid_assignment();

void perform_assignment(context_t* context, stack_t* semantic_stack);

#endif
