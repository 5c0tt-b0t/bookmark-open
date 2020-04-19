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

const char * list_push(list_t * const list_ptr, const char * const element, const int length){
	char *new_element_ptr;
	list_block *block_ptr, *new_block_ptr;
	unsigned int *next_free_index_ptr;

	assert((list_ptr != NULL) && (element != NULL) && (length > 0));

	new_element_ptr = (char *) malloc(sizeof(char) * (length + 1));
	memset(new_element_ptr, '\0', sizeof(char) * (length + 1));
	strncpy(new_element_ptr, element, length);

	block_ptr = list_ptr -> ending_block;
	if(block_ptr == NULL){
		/* Array list empty. */
		new_block_ptr = list_block_construct();
		list_ptr -> beginning_block = new_block_ptr;
		list_ptr -> ending_block = new_block_ptr;
	} else if(block_ptr -> next_free_index == ARRAY_BLOCK_SIZE){
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

const char * list_pop(list_t * const list_ptr, char ** const dest){
	list_block * block_ptr = NULL;
	list_block * last_block = list_ptr -> ending_block;
	const unsigned int next_free_index = last_block -> next_free_index;

	assert(list_ptr != NULL);

	assert(last_block != NULL && "Error: Trying to remove element from empty list.");
	if(last_block == NULL){
		return NULL;
	}


	if(next_free_index == 1u || next_free_index == 0u){
		/* 0 - SHOULD NOT OCCUR	- Empty block. Remove last item in 2nd last block. */
		/* 1 - CAN OCCUR	- 1 item in block. Remove first item in last block.*/
		/* 0 | 1		- In both cases delete last block.		   */

		/* Traverse until second last block. */
		block_ptr = list_ptr -> beginning_block;
		while(block_ptr -> next_block != last_block){
			block_ptr = block_ptr -> next_block;
		}

		/* Unlink */
		block_ptr -> next_block = NULL;
		list_ptr -> ending_block = block_ptr;

		if(next_free_index == 0u){
			*(dest) = (block_ptr -> strings)[ARRAY_BLOCK_SIZE - 1];
			(block_ptr -> next_free_index)--;
		} else {
			/* next_free_index == 1 */
			*dest = (last_block -> strings)[next_free_index - 1];
			(last_block -> strings)[0u] = NULL;
		}

		free(last_block);
		last_block = NULL;
	} else {
		*dest = (last_block -> strings)[next_free_index - 1];
		(last_block -> strings)[next_free_index - 1] = NULL;
		(last_block -> next_free_index)--;
	}

	return *(dest);
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
	char * txt0 = "1";
	char * txt1 = "2";
	char * txt2 = "3";
	char * txt3 = "4";
	char * txt4 = "5";

	char * txt5 = "6";
	char * txt6 = "7";
	char * txt7 = "8";
	char * txt8 = "9";
	char * txt9 = "10";

	char * breaker = "Break.";

	char * dest0 = "X";
	char * dest1 = "X";

	list_t * list = list_construct();
	list_push(list, txt0, strlen(txt0));
	list_push(list, txt1, strlen(txt1));
	list_push(list, txt2, strlen(txt2));
	list_push(list, txt3, strlen(txt3));
	list_push(list, txt4, strlen(txt4));
	list_push(list, txt5, strlen(txt5));
	list_push(list, txt6, strlen(txt6));
	list_push(list, txt7, strlen(txt7));
	list_push(list, txt8, strlen(txt8));
	list_push(list, txt9, strlen(txt9));

	list_push(list, breaker, strlen(breaker));

	list_print(list);

	list_pop(list, &dest0);

	printf("\nPopped item : \"%s\"\n", dest0);

	list_print(list);

	list_pop(list, &dest1);

	printf("\nPopped item : \"%s\"\n", dest1);

	list_print(list);

	list_destruct(&list);
	
	return 0;
}
