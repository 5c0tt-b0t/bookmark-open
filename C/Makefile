CC=gcc
CFLAGS=-Wall -Wextra -pedantic
C89=-std=c89

bookmark-open: main.c bo_config/config-scanner.c bo_config/
	#$(CC) -o2 bo_config/config-scanner.c bo_config/config-parser.c
	@$(CC) -o2 -o bookmark-open main.c

dev: main.c
	@clear
	@echo "C89"
	@$(CC) $(CFLAGS) $(C89) main.c -D C89

scanner-generate: bo_config/config-scanner.lex
	@echo "Generating scanners with lex." &&\
	 	lex -o bo_config/config-scanner.c bo_config/config-scanner.lex
