CC=gcc
CFLAGS=-Wall -Wextra -pedantic
C89=-std=c89

bookmark-open: main.c config-scanner.c
	@$(CC) -o2 -o bookmark-open main.c

dev: main.c
	@clear
	@echo "C89"
	@$(CC) $(CFLAGS) $(C89) main.c -D C89

dev-scanner: config-scanner.lex
	@echo "Generating scanners with lex." && lex -o config-scanner.c config-scanner.lex
	@$(CC) $(CFLAGS) $(C89) config-scanner.c
