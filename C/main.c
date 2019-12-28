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

#include "config.c"

#ifdef C89
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

#endif

int main(int argc, char ** argv){
	FILE * p_cfg_file;
	char ** arg = argv;

	while(*arg != NULL){
		if(strcmp(*arg, "--init") == 0){
			if(*(arg + 1) != NULL && strcmp(*(arg + 1), "--help") == 0){
				/*TODO init help*/
				printf("--init help.\n");
			} else{
				/* TODO init code*/
				printf("--init code.\n");
			}
			exit(EXIT_SUCCESS);
		} else if(strcmp(*arg, "--help") == 0){
			print_usage();
			exit(EXIT_SUCCESS);
		}
		arg++;
	}

	p_cfg_file = get_cfg_file();
	validate_cfg_file(p_cfg_file);

	exit(EXIT_SUCCESS);
}

