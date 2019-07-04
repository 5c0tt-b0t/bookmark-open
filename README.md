# Troshure
Fancy bookmarking system.

## Features
- [ ] Usage from the terminal.
- [ ] GUI (dmenu and rofi).
- [ ] Integration with browsers.
- [ ] Multiple groupings or collections.
- [ ] Open a collection of sites in one go.
- [ ] Open websites in a collection one by one, stepping through.
- [ ] Configuration database file location

## To-do
* Use XDG paths with fallback paths.
* Use full path for database file location.
* Solve problem. When first and only url is removed and a new url is then added causes problem.

## Getting started
Execute:
```{BASH}
sudo cp troshure /usr/local/bin/
```
Now you can run the application.

## Structure
The core functionality will be implemented in a terminal application and the integration with the bowsers will be achieved through an extension with native messaging.
All urls will be saved in a file on the user's system.

## License
This project is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; version 2 of the License.
