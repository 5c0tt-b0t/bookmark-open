#include <stdio.h>

#ifndef BO_UTIL
#define BO_UTIL
/* The format should always be a string literal.*/
#define GEN_ERROR_FORMAT_STR(_FORMAT) APP_NAME ": " _FORMAT "\nTry 'bookmark-open --help' for more information.\n"
/* Characters added to the _FORMAT when generating the new format.			*/
/* Remember to change ERROR_FORMAT_EXTRA_LENGTH if GEN_ERROR_FORMAT_STR is modified.	*/
#define ERROR_FORMAT_EXTRA_LENGTH 65

#define print_error_msg(_FORMAT) fprintf(stderr, GEN_ERROR_FORMAT_STR(_FORMAT));

#ifdef C89
	/* No __VA_ARGS__. in C89.*/
	#include <stdarg.h>
	#include <stdlib.h>
	void printf_error_msg(const char * const old_format, ...){
		va_list args;
		char * const format = (char *) malloc(strlen(old_format) + ERROR_FORMAT_EXTRA_LENGTH + 1);
		if(format == NULL){
			fprintf(stderr, "Could not allocate memory for error message.\n");
			exit(EXIT_FAILURE);
		}

		sprintf(format, APP_NAME ": %s\nTry 'bookmark-open --help' for more information.\n", old_format);
		
		va_start(args, old_format);
		vprintf(format, args);
		va_end(args);
	}
#else
	#define printf_error_msg(format, ...) \
		fprintf(stderr, GEN_ERROR_FORMAT_STR(format), __VA_ARGS__);
#endif


/* Checks if a file is empty.		*/
/* Return -1 if an error occurred.*/
int is_empty(FILE * p_file){
	if(fseek(p_file, 0, SEEK_END) == -1)
		return -1;

	return (ftell(p_file) == 0)? 1: (rewind(p_file), 0);
}

#endif
