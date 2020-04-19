#ifndef KJ_DATA_STRUCTURES
#define KJ_DATA_STRUCTURES

struct list_t;

struct list_t * list_malloc();

struct list_t * list_construct();

void list_destruct(list_t ** list_2ptr);

const void * list_append(list_t * const list_ptr, const void * const item, const unsigned int length);

const void * list_remove_last(list_t * const list_ptr);

const char * list_get_str_rep(const list_t * const list_ptr);

#endif
