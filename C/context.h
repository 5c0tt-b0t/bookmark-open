#ifndef BO_CONTEXT
#define BO_CONTEXT

struct context_struct;

typedef struct context_struct context_t;

context_t*  context_malloc();

char * get_db(context_t* context);

char * get_url_open_cmd(context_t* context);

int set_db(context_t* context, char * value, int length);

int set_url_open_cmd(context_t* context, char * value, int length);

void context_free(context_t* context);

#endif
