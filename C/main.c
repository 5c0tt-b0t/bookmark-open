/* Fancy bookmarking system with terminal and browser interfaces.
 * Copyright (C) 2019  Karmjit Mahil
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

#include <stdlib.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>

#define APP_NAME "bookmark-open"

/* The format should always be a string literal.*/
#define GEN_FORMAT_STR(format) APP_NAME ": " format "\nTry 'bookmark-open --help' for more information.\n"
#define print_error_msg(format) \
	fprintf(stderr, GEN_FORMAT_STR(format));

#ifdef C89
	/* C89 */
	
	/* 509 character limit for literals in C89.*/
	#define print_usage() \
		{char * const usage = (char *) malloc(570);						\
		strcpy(usage, "Usage: " APP_NAME " [-adDlo]\n"						\
			"\t{ --add | --delete | --delete-db | --list-all | --open | --init }\n\n"	\
			"\t    --init       path   Initialise database.\n"				\
			"\t                        For more information try '--init --help'\n"		\
			"\t-a, --add        url    Add a url.\n"					\
			"\t-d, --delete     id     Delete url.\n"					\
			"\t-D, --delete-db         Delete database.\n");				\
		strcat(usage,										\
			"\t-l, --list-all          List all urls.\n"					\
			"\t-o, --open       id     Open url in the browser.\n"				\
			"\t-O, --open-all          Open all urls.\n"					\
			"\t-                       Read from standard input.\n"				\
			"\nIf no arguments are specified the default behaviour is as -O.\n");		\
		printf("%s", usage);}

	/* No __VA_ARGS__. */
	/* TODO: Writ printf_error_msg with valist*/
#else
	#define print_usage() printf(									\
			"Usage: " APP_NAME " [-adDlo]\n" 						\
			"\t{ --add | --delete | --delete-db | --list-all | --open | --init }\n\n" 	\
			"\t    --init       path   Initialise database.\n"				\
			"\t                        For more information try '--init --help'\n"		\
			"\t-a, --add        url    Add a url.\n"					\
			"\t-d, --delete     id     Delete url.\n"					\
			"\t-D, --delete-db         Delete database.\n"					\
			"\t-l, --list-all          List all urls.\n"					\
			"\t-o, --open       id     Open url in the browser.\n"				\
			"\t-O, --open-all          Open all urls.\n"					\
			"\t-                       Read from standard input.\n"				\
			"\nIf no arguments are specified the default behaviour is as -O.\n")

	#define printf_error_msg(format, ...) \
		fprintf(stderr, GEN_FORMAT_STR(format), __VA_ARGS__);
#endif


/* Checks if a file is empty.		*/
/* Return -1 if an error occurred.	*/
int is_empty(FILE * p_file);

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

int main(){
	FILE * p_cfg_file;
	printf_error_msg("testing: %s, %s, %s ", "one", "two", "three"); 
	print_usage();
	p_cfg_file = get_cfg_file();
	validate_cfg_file(p_cfg_file);
	

	exit(EXIT_SUCCESS);
}

/* Return -1 if an error occurred.*/
int is_empty(FILE * p_file){
	if(fseek(p_file, 0, SEEK_END) == -1)
		return -1;

	return (ftell(p_file) == 0)? 1: (rewind(p_file), 0);
}
