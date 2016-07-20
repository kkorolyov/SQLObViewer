package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Fires SQL requests.
 */
public interface SqlRequestSubject extends Subject {
	/** @param listener SQL request listener to add */
	void addSqlRequestListener(SqlRequestListener listener);
	/** @param listener SQL request listener to remove */
	void removeSqlRequestListener(SqlRequestListener listener);
}
