#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "util.c"

/* Gets a pointer to the configuration file.			*/
/* Terminates the program is the file could not be opened.	*/
FILE * get_cfg_file(void){
	FILE * p_cfg_file;
	const char * const home = getenv("HOME");

	/* e.g. HOME = /home/user, APP_NAME = app	*/
	/* FILE=/home/user/.config/app/config		*/
	/* /.config/ = 9, /config = 7, \0 = 1		*/
	char * const cfg_file_path = (char *) malloc(strlen(home)  +   strlen(APP_NAME) + 16);
	sprintf(cfg_file_path, "%s/.config/%s/config", home, APP_NAME);
	
	p_cfg_file = fopen(cfg_file_path, "r");
	if(p_cfg_file == NULL){
		printf_error_msg("could not open: %s\nApplication not setup, use '--init' option.\n", cfg_file_path);
		free((void *) cfg_file_path);
		exit(EXIT_FAILURE);
	} else {
		free((void *) cfg_file_path);
		return p_cfg_file;
	}
}

/* Validates the configuration file.			*/
/* Terminates the program with EXIT_FAILURE if invalid.	*/
void validate_cfg_file(FILE * p_cfg_file){
	if(is_empty(p_cfg_file)){
		print_error_msg("configuration file empty\nApplication not setup, use `init` option.\n");
		exit(EXIT_FAILURE);
	}
}


