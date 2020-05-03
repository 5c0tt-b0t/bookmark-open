CC=gcc
DEV_CFLAGS=-I./ -g -ggdb -Wall -Wextra -Wshadow -Wstrict-overflow -pedantic -std=c89

bookmark-open: main.c bo_config/config-scanner.c bo_config/
	#$(CC) -o2 bo_config/config-scanner.c bo_config/config-parser.c
	@$(CC) -o2 -o bookmark-open main.c

dev: bo_config/scanner.c bo_config/semantics-processor.c bo_config/parser.c context.c
	@$(CC) $(DEV_CFLAGS) main.c bo_config/scanner.c bo_config/semantics-processor.c bo_config/parser.c context.c -D C89

scanner: bo_config/scanner.lex
	@echo "Generating scanners with lex." &&\
	 	lex -o bo_config/scanner.c bo_config/scanner.lex

dev-old: main.c
	@clear
	@echo "C89"
	@$(CC) $(CFLAGS) $(C89) main.c -D C89


