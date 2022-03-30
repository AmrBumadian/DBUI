import javax.sql.RowSet;
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
	public DataPanel(RowSet rowSet) throws SQLException {
		fields = new ArrayList<>();
		setLayout(new GridBagLayout());
		var gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;

		ResultSetMetaData resultSetMetaData = rowSet.getMetaData();
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			gridBagConstraints.gridy = i - 1;
			String columnName = resultSetMetaData.getColumnLabel(i);
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			add(new JLabel(columnName), gridBagConstraints);

			int columnWidth = resultSetMetaData.getColumnDisplaySize(i);
			var tb = new JTextField(columnWidth);
			if (!resultSetMetaData.getColumnClassName(i).equals("java.lang.String"))
				tb.setEditable(false);

			fields.add(tb);

			gridBagConstraints.gridx = 1;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			add(tb, gridBagConstraints);
		}
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
}