import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

public class DataController {

	private CachedRowSet cachedRowSet;
	private Connection connection;

	DataController() {
		try {
			readDatabaseProperties();
			connection = getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException sqlException) {
			for (Throwable t : sqlException) System.out.println(t.getMessage());
		} catch (IOException ioException) {
			System.out.println(ioException.getMessage());
		}
	}

	/**
	 * Moves to the previous table row.
	 */
	public void showPreviousRow(DataPanel dataPanel) {
		System.out.println(Thread.currentThread().getId());
		try {
			if (cachedRowSet == null || cachedRowSet.isFirst()) return;
			cachedRowSet.previous();
			dataPanel.showRow(cachedRowSet);
		} catch (SQLException sqlException) {
			for (Throwable t : sqlException)
				t.printStackTrace();
		}
	}

	/**
	 * Moves to the next table row.
	 */
	public void showNextRow(DataPanel dataPanel) {
		System.out.println(Thread.currentThread().getId());
		try {
			if (cachedRowSet == null || cachedRowSet.isLast()) return;
			cachedRowSet.next();
			dataPanel.showRow(cachedRowSet);
		} catch (SQLException sqlException) {
			for (Throwable t : sqlException)
				t.printStackTrace();
		}
	}

	/**
	 * Deletes current table row.
	 */
	public void deleteRow(DataPanel dataPanel) {
		if (cachedRowSet == null) return;
		new SwingWorker<Void, Void>() {
			public Void doInBackground() throws SQLException {
				System.out.println(Thread.currentThread().getId());
				cachedRowSet.deleteRow();
				cachedRowSet.acceptChanges(connection);
				if (cachedRowSet.isAfterLast() && !cachedRowSet.last()) cachedRowSet = null;
				return null;
			}

			public void done() {
				dataPanel.showRow(cachedRowSet);
			}
		}.execute();
	}

	/**
	 * Saves all changes, in the background.
	 */
	public void saveChanges(DataPanel dataPanel) {
		if (cachedRowSet == null) return;
		new SwingWorker<Void, Void>() {
			public Void doInBackground() throws SQLException {
				System.out.println(Thread.currentThread().getId());
				dataPanel.setRow(cachedRowSet);
				cachedRowSet.acceptChanges(connection);
				return null;
			}
		}.execute();
	}

	public DatabaseMetaData getConnectionMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/**
	 * Queries the database to get the rows in the selected table
	 * @param tableName the table to be shown
	 * @return a cached copy of the result set
	 */
	public CachedRowSet getCachedRowSetOf(String tableName) throws SQLException {
		try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet result = statement.executeQuery("SELECT * FROM " + tableName)) {

			RowSetFactory factory = RowSetProvider.newFactory();
			cachedRowSet = factory.createCachedRowSet();
			cachedRowSet.setTableName(tableName);
			cachedRowSet.populate(result);

			var temp = factory.createCachedRowSet();
			temp.setTableName(tableName);
			temp.populate(result);
			return temp;
		} catch (SQLException sqlException) {
			for (Throwable t : sqlException) t.printStackTrace();
			return RowSetProvider.newFactory().createCachedRowSet();
		}
	}

	public void closeConnection() throws SQLException {
		if (connection!= null) connection.close();
	}

	private void readDatabaseProperties() throws IOException {
		var properties = new Properties();
		try (InputStream in = Files.newInputStream(Path.of("C:\\Users\\Amr Bumadian\\Desktop\\JavaProjects\\DBUI\\src\\main\\resources\\database.properties"))) {
			properties.load(in);
		}
		String drivers = properties.getProperty("jdbc.drivers");
		String url = properties.getProperty("jdbc.url");
		String username = properties.getProperty("jdbc.username");
		String password = properties.getProperty("jdbc.password");
		if (drivers != null) System.setProperty("jdbc.drivers", drivers);
		if (url != null) System.setProperty("jdbc.url", url);
		if (username != null) System.setProperty("jdbc.username", username);
		if (password != null) System.setProperty("jdbc.password", password);
	}

	/**
	 * Gets a connection from the properties specified in the System properties.
	 *
	 * @return the database connection
	 */
	private Connection getConnection() throws SQLException {
		String url = System.getProperty("jdbc.url");
		String username = System.getProperty("jdbc.username");
		String password = System.getProperty("jdbc.password");
		return DriverManager.getConnection(url, username, password);
	}
}
