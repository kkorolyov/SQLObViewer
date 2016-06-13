# SQLObViewer
A Java-based graphical database viewer utilizing the SQLOb API.

## Screenshots
![Main table view](screenshots/table.png?raw=true)

![Editing a cell](screenshots/editing.png?raw=true)

![Adding a row](screenshots/adding.png?raw=true)

## Dependencies
* [SQLOb](https://github.com/kkorolyov/SQLOb) - Database communication
* [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - Database connection
* [SimpleProps](https://github.com/kkorolyov/SimpleProps) - Configuration file management
* [SimpleLogs](https://github.com/kkorolyov/SimpleLogs) - Application logging

## Installation
* Download the [latest release](https://github.com/kkorolyov/SQLObViewer/releases/latest).
* If using the provided .jar file, move it to your directory of choice.
* If building from source, make sure all [dependencies] (#dependencies) are in your classpath.

## Usage
* At the initial login prompt, input appropriate database connection information, then press "Log In" to connect to the database.
 * Input information is saved to the created .ini file found in the application directory, and prepopulates the login screen on subsequent application runs.
* Select a table to view from the dropdown at the top.
* Select any cell, change its value, and press the "Enter" key to update the respective cell in the backing database.
* Select the "Add Row" button on the right, input new values into the popup, then press "OK" to add the row to the backing database.
* Select a row (Use "Shift" and "Ctrl" modifiers to select multiple rows), then press the "Delete Row" button on the right to delete the selected row(s) from the backing database.

## License
BSD-new license.  
More detail found [here](LICENSE).
