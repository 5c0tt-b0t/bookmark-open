# Troshure
Fancy bookmarking system.

## Features
* Usage from the terminal.

## Features to implement.
* POSIX compliant.
* GUI (dmenu and rofi).
* Integration with browsers.
* Multiple groupings or collections.
* Open a collection of sites in one go.
* Open websites in a collection one by one, stepping through.

## Getting started
First you will need to setup a database and configuration file. Execute the following in your cloned repo:
```{BASH}
./setup PATH
```
Where PATH is the path to the database file. If no PATH is specified or a directory name is specified, the database file will be created in the current directory or specified directory with name 'website'.
Now you will need to copy the 'troshure' script to /usr/local/bin/ so that you can use it no matter your working directory.
```{BASH}
sudo cp troshure /usr/local/bin/
```
You should now be set to use troshure on the terminal.

## Structure
The core functionality will be implemented in a terminal application and the integration with the bowsers will be achieved through an extension with native messaging.
All urls will be saved in a file on the user's system.

## To-do
* Use XDG paths with fallback paths.
* Remove -D option and add an option to use a new database.

## License
This project is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; version 2 of the License.
