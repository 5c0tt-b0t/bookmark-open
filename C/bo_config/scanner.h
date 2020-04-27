#ifndef BO_CONFIG_SCANNER
#define BO_CONFIG_SCANNER

#include <bo_config/tokens.h>

void set_scanner_input(FILE* file);

token_t * get_next_token();

void scanner_restart(FILE* file);

#endif
