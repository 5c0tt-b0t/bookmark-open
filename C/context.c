#include <stdlib.h>
#include <string.h>

#include <context.h>

#define N_ATTRIBUTES 2
typedef enum {
	DB,
	URL_OPEN_CMD
} ATTRIBUTE;

struct context_struct{
	char ** values;
};

context_t * context_malloc(){
	context_t* context = malloc(sizeof(context_t));
	context -> values = (char **) calloc(N_ATTRIBUTES, sizeof(char *));
	return context;
}

#define get_attribute(C,A) ((C) -> values == NULL)? NULL : ((C) -> values[A])
static int set_attribute(context_t* context, ATTRIBUTE attribute, char * value, int length);

char * get_db(context_t* context){
	return get_attribute(context, DB);
}

char * get_url_open_cmd(context_t* context){
	return get_attribute(context, URL_OPEN_CMD);
}
#undef get_attribute

int set_db(context_t* context, char * value, int length){
	return set_attribute(context, DB, value, length);
}

int set_url_open_cmd(context_t* context, char * value, int length){
	return set_attribute(context, URL_OPEN_CMD, value, length);
}


void context_free(context_t* context){
	int k;
	for(k = 0; k < N_ATTRIBUTES; k++){
		if(context -> values[k] != NULL){
			free(context -> values[k]);
			context -> values[k] = NULL;
		}
	}
}

static int set_attribute(context_t* context, ATTRIBUTE attribute, char * value, int length){
	char * internal_value;

	if(context == NULL || context -> values == NULL || length <= 0){
		return 0;
	}

	internal_value = (char *) malloc(sizeof(char) * length);
	strncpy(internal_value, value, length);

	context -> values[attribute] = internal_value;

	return 1;
}

#undef N_ATTRIBUTES
