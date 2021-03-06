import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This panel displays the contents of a result set.
 */
public class DataPanel extends JPanel {
	private final List<JTextField> fields;

	/**
	 * Constructs the data panel.
	 *
	 * @param rowSet the result set whose contents this panel displays
	 */
	public DataPanel(RowSet rowSet) {
		setLayout(new GridBagLayout());
		fields = new ArrayList<>();
		showLabels(rowSet);
	}
	/**
	 * Shows all the columns labels
	 *
	 * @param rowSet the result set whose contents this panel displays
	 */
	private void showLabels(RowSet rowSet) {
		try {
			var gridBagConstraints = new GridBagConstraints();
			var resultSetMetaData = rowSet.getMetaData();
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				showColumnLabelAt(i, resultSetMetaData, gridBagConstraints);
			}
		} catch (SQLException sqlException) {
			for (Throwable t : sqlException) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Shows the column at index i
	 *
	 * @param i the index of the column
	 * @param resultSetMetaData the meta-data of the table
	 * @param gridBagConstraints the constraints of the label position
	 */
	private void showColumnLabelAt(int i, ResultSetMetaData resultSetMetaData, GridBagConstraints gridBagConstraints) throws SQLException {
		String columnName = resultSetMetaData.getColumnLabel(i);
		int columnWidth = resultSetMetaData.getColumnDisplaySize(i);

		gridBagConstraints.gridy = i - 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		add(new JLabel(columnName), gridBagConstraints);

		var tb = new JTextField(Math.min(50, columnWidth));

		gridBagConstraints.gridx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(tb, gridBagConstraints);

		fields.add(tb);
	}

	/**
	 * Shows a database row by populating all text fields with the column values.
	 */
	public void showRow(ResultSet rowSet) {
		try {
			if (rowSet == null) return;
			for (int i = 1; i <= fields.size(); i++) {
				String field = rowSet.getString(i);
				JTextField tb = fields.get(i - 1);
				tb.setText(field);
			}
		} catch (SQLException ex) {
			for (Throwable t : ex) t.printStackTrace();
		}
	}

	/**
	 * Updates changed data into the current row of the row set.
	 */
	public void setRow(RowSet rowSet) throws SQLException {
		for (int i = 1; i <= fields.size(); i++) {
			String field = rowSet.getString(i);
			JTextField tb = fields.get(i - 1);
			if (!field.equals(tb.getText()))
				rowSet.updateString(i, tb.getText());
		}
		rowSet.updateRow();
	}

	/**
	 * Inserts the values in the data panel to the rowset
	 * @param rowSet the rowset pointing to the new empty row
	 * @throws SQLException sqlException
	 */
	public void insert(RowSet rowSet) throws SQLException {
		for (int i = 1; i <= fields.size(); i++) {
			String value = fields.get(i - 1).getText();
			rowSet.updateString(i, value);
		}
	}
}