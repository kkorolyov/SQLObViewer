# Change Log

## [1.1] - 2016-07-30
### Changes
* Added some custom button icons.
* All assets automatically extract from .jar if required.
	* Full application packaged as a .jar again instead of a .zip file.
* Default logging config changed to INFO level, disabled.

### Fixes
* Fixed more wonkiness related to automatic table row reselection on data refresh.
* A log file will no longer be created when logging is disabled.


## [1.0] - 2016-07-24
### Changes
* Custom application icon added.
	* Application images location may be manually configured.
* Application now logs to file.
	* Log file and logging preferences may be manually configured.
* Last statement popup disabled.

### Fixes
* Fixed main screen not shrinking on window resize.
* Fixed wonky automatic table row reselection on data refresh.


## [0.8] - 2016-07-20
### Changes
* Options added.
* Last executed statement display added.

### Fixes
* When changing between screens, the new screen now renders at the location of the previous screen.


## [0.7.2] - 2016-07-19
### Changes
* Selected rows counter added.

### Fixes
* Attempting incompatible operations on empty tables and databases no longer throws an exception.


## [0.7.1] - 2016-07-14
### Changes
* UI tweaks.
* Column filter markers added.
* Table grid selector added


## [0.7] - 2016-07-09
### Changes
* UI refreshed.
	* Now uses MiGLayout layout manager.
	* Some component changes.
* Table deletion added.
* Confirmation dialogs added.
	* Table deletion confirmation.
	* Row deletion confirmation.


## [0.6.1] - 2016-06-28
### Changes
* Table creation added.
* Table cells support context-clicking.

### Fixes
* Clicking a `boolean` cell no longer results in an exception.


## [0.6] - 2016-06-25
### Changes
* Application properties overhauled.
	* Properties are now stored in an `assets` folder in the same directory as the application .jar file.
* Unique filters per table column added.
	* Filtering is based on the `AND` result of all selected column filters.

### Fixes
* Refresh button no longer removes current table filters
* Filter popup is now scrollable.
* Single-clicking a cell no longer selects it for editing.
	* Editing is now triggered by double-clicking.


## [0.5.1] - 2016-06-12
### Changes
* Selecting a cell automatically selects/highlights all its text.


## [0.5] - 2016-06-11
### Changes
* Filtering added
	* Right-clicking a column spawns a popup of all unique values in that column.
		* Selecting one of these values shows only rows with the same value for the right-clicked column.


## [0.4] - 2016-06-05
### Changes
* Table is now sortable


## [0.3] - 2016-06-04
### Changes
* Added delete row button.
	* All selected rows deleted upon delete button press.

### Fixes
* Squashed exception when attempting to add a row to an empty table.


## [0.2] - 2016-06-02
### Changes
* Refresh button.
* Viewed table will now auto-rebuild for synchronization purposes if an update statement changes more than 1 row.
* Last statement label **PLACEHOLDER**.
* Undo button **PLACEHOLDER**.


## [0.1] - 2016-05-30
### Current Functionality
* View tables
* Edit rows
* Add rows


[1.1]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v1.1
[1.0]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v1.0
[0.8]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.8
[0.7.2]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.7.2
[0.7.1]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.7.1
[0.7]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.7
[0.6.1]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.6.1
[0.6]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.6
[0.5.1]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.5.1
[0.5]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.5
[0.4]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.4
[0.3]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.3
[0.2]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.2
[0.1]: https://github.com/kkorolyov/SQLObViewer/releases/tag/v0.1
