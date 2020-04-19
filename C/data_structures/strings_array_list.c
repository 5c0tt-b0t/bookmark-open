#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>

#define ARRAY_BLOCK_SIZE 10

#define ARRAY_LIST_BLOCK_IS_FULL(BLOCK) ((BLOCK) -> next_free_index == ARRAY_BLOCK_SIZE - 1)

typedef struct block {
	char ** strings;
	unsigned int next_free_index;
	struct block * next_block;
} list_block;

typedef struct{
	list_block * beginning_block;
	list_block * ending_block;
} list_t;

list_t * list_construct(){
	list_t * const list = (list_t * const) malloc(sizeof(list_t));
	list -> beginning_block = NULL;
	list -> ending_block = NULL;
	return list;
}

list_block * list_block_construct(){
	list_block * const block_ptr = (list_block * const) malloc(sizeof(list_block));
	block_ptr -> strings = malloc(sizeof(char *) * ARRAY_BLOCK_SIZE);
	block_ptr -> next_free_index = 0u;
	block_ptr -> next_block = NULL;
	return block_ptr;
}


void list_chain_destruct(list_block * block_ptr){
	unsigned int string_n;
	list_block * next_block_ptr;

	/* Base case for the recursion. */
	if(block_ptr == NULL){
		return;
	}

	next_block_ptr = block_ptr -> next_block;
	block_ptr -> next_block = NULL;
	block_ptr -> next_free_index = 0u;

	/* Free memory for each string inside the block */
	for(string_n = 0; string_n < ARRAY_BLOCK_SIZE; string_n++){
		free((block_ptr -> strings)[string_n]);
		(block_ptr -> strings)[string_n] = NULL;
	}

	free(block_ptr);
	list_chain_destruct(next_block_ptr);
}

void list_destruct(list_t ** list){
	list_block * beginning_block_ptr;

	assert(list != NULL);

	if((beginning_block_ptr = (*list) -> beginning_block) != NULL){
		list_chain_destruct(beginning_block_ptr);
		(*list) -> beginning_block = NULL;
		free(*list);
		(*list) = NULL;
	}

}

const char * list_append(list_t * const list_ptr, const char * const element, const int length){
	char *new_element_ptr;
	list_block *block_ptr, *new_block_ptr;
	unsigned int *next_free_index_ptr;

	assert((list_ptr != NULL) && (element != NULL) && (length > 0));

	new_element_ptr = (char *) malloc(sizeof(char) * (length + 1));
	memset(new_element_ptr, '\0', sizeof(char) * (length + 1));
	strncpy(new_element_ptr, element, length);

	block_ptr = list_ptr -> ending_block;
	if(block_ptr == NULL){
		printf("CREATED BLOCK\n");
		/* Array list empty. */
		new_block_ptr = list_block_construct();
		list_ptr -> beginning_block = new_block_ptr;
		list_ptr -> ending_block = new_block_ptr;
	} else if(block_ptr -> next_free_index == ARRAY_BLOCK_SIZE){
		printf("HERE\n");
		new_block_ptr = list_block_construct();
		list_ptr -> ending_block = new_block_ptr;
		block_ptr -> next_block = new_block_ptr ;
	}

	block_ptr = new_block_ptr;

	next_free_index_ptr = &(block_ptr -> next_free_index);
	block_ptr -> strings[*next_free_index_ptr] = new_element_ptr;
	(*next_free_index_ptr)++;

	return element;
}



void list_print(const list_t * const list){
	unsigned int block_number, index;
	const list_block * block_ptr = list -> beginning_block;

	assert(list != NULL);

	if(block_ptr == NULL){
		printf("Array list empty.");
	} else {
		for(block_number = 1u; block_ptr != NULL;
		block_number++, block_ptr = block_ptr -> next_block){
			printf("%u : [ ", block_number);

			for(index = 0u; index < block_ptr -> next_free_index; index++){
				printf("\"%s\" ", block_ptr -> strings[index]);
			}

			printf("]\n");
		}
	}
}

int main(){
	char * txt0 = "Hello World 0 --------";
	char * txt1 = "Hello World 1 ------";
	char * txt2 = "Hello World 2 ----";
	char * txt3 = "Hello World 3 --";
	char * txt4 = "Hello World 4 ";

	char * txt5 = "Hello World 0 --------";
	char * txt6 = "Hello World 1 ------";
	char * txt7 = "Hello World 2 ----";
	char * txt8 = "Hello World 3 --";
	char * txt9 = "Hello World 4 ";

	char * breaker = "HELLOOOOOO WORLD !! From the depths of Hell.";

	list_t * list = list_construct();
	list_append(list, txt0, strlen(txt0));
	list_append(list, txt1, strlen(txt1));
	list_append(list, txt2, strlen(txt2));
	list_append(list, txt3, strlen(txt3));
	list_append(list, txt4, strlen(txt4));
	list_append(list, txt5, strlen(txt5));
	list_append(list, txt6, strlen(txt6));
	list_append(list, txt7, strlen(txt7));
	list_append(list, txt8, strlen(txt8));
	list_append(list, txt9, strlen(txt9));

	list_append(list, breaker, strlen(breaker));
	list_print(list);

	list_destruct(&list);
	
	return 0;
}
