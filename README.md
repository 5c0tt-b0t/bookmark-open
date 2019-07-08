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
troshure --init path
```
Where PATH is the path to the database file.
Try 'troshure --init --help' for more information.

If you want to be able to run the program from any directory, you will need to copy the 'troshure' and 'troshure\_setup' scripts to '/usr/local/bin/' or other directory in your PATH variable.
```{BASH}
sudo cp troshure /usr/local/bin/
sudo cp troshure_setup /usr/locale/bin/
```
You should now be set to use troshure on the terminal.

## Structure
The core functionality will be implemented in a terminal application and the integration with the bowsers will be achieved through an extension with native messaging.
All urls will be saved in a file on the user's system.

## To-do
* Use XDG paths with fallback paths.

## License
This project is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; version 2 of the License.
