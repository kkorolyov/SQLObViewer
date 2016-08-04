package dev.kkorolyov.sqlobviewer.model;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.statement.StatementCommand;
import dev.kkorolyov.sqlob.construct.statement.UpdateStatement;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;

/**
 * Maintains the main model for the {@code SQLObViewer} application.
 */
public class DatabaseModel {
	private static final Logger log = Logger.getLogger(DatabaseModel.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	private DatabaseConnection dbConn;
	private TableConnection tableConn;
	private SQLObTableModel tableModel;
	
	/** @return tables under current database connection */
	public String[] getTables() {
		return dbConn.getTables();
	}
	
	/** @return columns of current table connection */
	public Column[] getTableColumns() {
		return tableConn != null ? tableConn.getColumns() : new Column[0];
	}
	/** @return data of current table connection */
	public RowEntry[][] getTableData() {
		if (tableConn == null)
			return new RowEntry[0][0];
		
		List<RowEntry[]> data = new LinkedList<>();

		Results allResults = tableConn.select(null);
		
		RowEntry[] currentRow;
		while ((currentRow = allResults.getNextRow()) != null)				
			data.add(currentRow);
			
		return data.toArray(new RowEntry[data.size()][]);
	}
	
	/** @return last-executed {@code UpdateStatement}, or {@code null} if no such statement */
	public UpdateStatement getLastStatement() {
		UpdateStatement lastStatement = null;
		
		if (dbConn != null) {
			for (int i = dbConn.getStatementLog().size() - 1; i >= 0; i--) {
				StatementCommand currentStatement = dbConn.getStatementLog().get(i);
				if (currentStatement instanceof UpdateStatement) {
					lastStatement = (UpdateStatement) currentStatement;
					break;
				}
			}
		}
		return lastStatement;
	}
	
	/** @return current database connection */
	public DatabaseConnection getDatabaseConnection() {
		return dbConn;
	}
	/** @param newDatabaseConnection new database connection */
	public void setDatabaseConnection(DatabaseConnection newDatabaseConnection) {
		if (dbConn != null)
			dbConn.close();
		tableConn = null;
		
		dbConn = newDatabaseConnection;
		log.debug("Set new database connection = " + dbConn);
	}
	
	/** @return current table connection */
	public TableConnection getTableConnection() {
		return tableConn;
	}
	/** @param newTableConnection new table connection */
	public void setTableConnection(TableConnection newTableConnection) {
		tableConn = newTableConnection;
		log.debug("Set new table connection = " + tableConn);
		
		updateTableModel();
	}
	
	/** @return current table model */
	public SQLObTableModel getTableModel() {
		return tableModel;
	}
	/** @param newModel new table model */
	public void setTableModel(SQLObTableModel newModel) {
		if (tableModel != null)
			tableModel.clearListeners();
		
		tableModel = newModel;
		log.debug("Set new table model = " + tableModel);
		
		updateTableModel();
	}
	
	/**
	 * Updates this database model's table model.
	 */
	public void updateTableModel() {
		log.debug("Updating table model...");
		
		if (tableModel != null)
			tableModel.setData(getTableColumns(), getTableData());
		
		log.debug("Done updating table model");
	}
}
