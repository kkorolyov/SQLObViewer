# SQLObViewer
A Java-based graphical database viewer utilizing the SQLOb API.

## Table of Contents
* [Screenshots] (#screenshots)
* [Dependencies] (#dependencies)
* [Installation] (#installation)
* [Usage] (#usage)
  * [Login Screen] (#login-screen)
  * [Options Screen] (#options-screen)
  * [Main Screen] (#main-screen)
    * [Table Actions] (#table-actions)
    * [Row Actions] (#row-actions)
    * [Grid Selector] (#grid-selector)
* [License] (#license)

## Screenshots
![Main table view](screenshots/table.png?raw=true)

![Editing a cell](screenshots/edit.png?raw=true)

## Dependencies
* [SQLOb](https://github.com/kkorolyov/SQLOb) - Database communication
* [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) - Database connection
* [RandomUtils-ResourceExtractor](https://github.com/kkorolyov/RandomUtils) - Bundled asset extracting
* [SimpleProps](https://github.com/kkorolyov/SimpleProps) - Configuration file management
* [SimplePropsEditor](https://github.com/kkorolyov/SimplePropsEditor) - Configuration editing
* [SimpleLogs](https://github.com/kkorolyov/SimpleLogs) - Application logging
* [SwingPlus](https://github.com/kkorolyov/SwingPlus) - Additional Swing components
* [MiGLayout](http://www.miglayout.com/) - GUI layout management

## Installation
* Download the [latest release](https://github.com/kkorolyov/SQLObViewer/releases/latest).
* If using the provided .jar file, move it to your directory of choice.
* If building from source, make sure all [dependencies] (#dependencies) are in your classpath.

## Usage

### Login Screen
![Login Screen] (screenshots/login.png?raw=true)
* Input IP address or hostname of the database server.
* Input database name.
* Input user credentials for database access.
* Press "Log In" to connect.
  * Upon pressing "Log In", input information is saved to the config file and prepopulates the login screen on subsequent application runs.

### Options Screen
![Options Screen] (screenshots/options.png?raw=true)
* All "Value" cells may be edited.
* Edited, unsaved options turn red.
* "Save" writes current changes to file.
* "Discard changes" discards all changes since the last save.
* Changes requiring an application restart to take effect:
  * File or folder locations
  * Logging preferences

### Main Screen
![Main Screen](screenshots/table.png?raw=true)
* Select a table to view from the dropdown at the top.
* Select any cell, change its value, and press the "Enter" key (or click on any other cell) to update the respective cell in the backing database.
  * Pressing "Escape" cancels any current editing.

#### Table Actions
![Table Actions](screenshots/create-table-button.png?raw=true)
![Table Create](screenshots/create-table.png?raw=true)
![Table Drop](screenshots/drop-table.png?raw=true)
* Hover over the "Table" button on the top right and select from one of the possible actions.
* Follow the prompts on the popup.

#### Row Actions
![Row Actions](screenshots/add-row-button.png?raw=true)
![Row Add](screenshots/add-row.png?raw=true)
![Row Delete](screenshots/delete-row.png?raw=true)
* Hover over the "Row" button on the top right and select from one of the possible actions.
* Follow the prompts on the popup.

#### Grid Selector
![Grid Selector](screenshots/grid-selector.png?raw=true)
![Grid Selector 3x3](screenshots/grid-selector-3x3.png?raw=true)
* The grid of buttons below the "Table" and "Row" buttons controls the number of displayed tables.
* Each table provides a view to the same data, but may be interacted with independently of the others.

## License
BSD-new license.  
More detail found [here](LICENSE).
