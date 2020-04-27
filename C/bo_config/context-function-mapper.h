#ifndef BO_FILE_CONTEX_FUNCTION_MAPPER
#define BO_FILE_CONTEX_FUNCTION_MAPPER

#include <context.h>
#include <bo_config/tokens.h>

typedef int (*setter_function)(context_t*, char*, int);
setter_function get_setter_function(TOKEN_TYPE token){
	switch(token){
		case DB:		return set_db;
		case URL_OPEN_CMD:	return set_url_open_cmd;
		default:		return NULL;
	}
}

#endif
