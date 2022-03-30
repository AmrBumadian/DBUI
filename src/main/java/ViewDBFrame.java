import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

/**
 * The frame that holds the data panel and the navigation buttons.
 */
public class ViewDBFrame extends JFrame {
	private DataPanel dataPanel;
	private Component scrollPane;
	private final JComboBox<String> tableNames;
	private final DataController dataController;


	public ViewDBFrame() {
		dataController = new DataController();

		tableNames = new JComboBox<>();
		populateTableNames();
		tableNames.addActionListener(event -> showTable((String) tableNames.getSelectedItem()));
		add(tableNames, BorderLayout.NORTH);

		setWindowListener();
		constructButtonPanel();

		if (tableNames.getItemCount() > 0) showTable(tableNames.getItemAt(0));
	}

	/**
	 * Prepares the text fields for showing a new table, and shows the first row.
	 *
	 * @param tableName  the name of the table to display
	 */
	public void showTable(String tableName) {
		try (CachedRowSet cachedRowSet = dataController.getCachedRowSetOf(tableName)) {
			if (scrollPane != null) remove(scrollPane);
			dataPanel = new DataPanel(cachedRowSet);
			scrollPane = new JScrollPane(dataPanel);
			add(scrollPane, BorderLayout.CENTER);
			dataController.showNextRow(dataPanel);
			pack();
		} catch (SQLException ex) {
			for (Throwable t : ex)
				t.printStackTrace();
		}
	}

	/**
	 * populates the combobox with the tables names
	 */
	private void populateTableNames() {
		try {
			DatabaseMetaData meta = dataController.getConnectionMetaData();
			try (ResultSet metaResultSet = meta.getTables("TEST", null, null, new String[]{"TABLE"})) {
				while (metaResultSet.next()) {
					tableNames.addItem(metaResultSet.getString(3));
				}
			}
		} catch (SQLException ex) {
			for (Throwable t : ex) t.printStackTrace();
		}
	}

	/**
	 * construct the buttons panel
	 */
	private void constructButtonPanel() {
		var buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		JButton previousButton = new JButton("Previous");
		previousButton.addActionListener(event -> dataController.showPreviousRow(dataPanel));
		buttonPanel.add(previousButton);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(event -> dataController.showNextRow(dataPanel));
		buttonPanel.add(nextButton);

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(event -> dataController.deleteRow(dataPanel));
		buttonPanel.add(deleteButton);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(event -> dataController.saveChanges(dataPanel));
		buttonPanel.add(saveButton);
	}

	/**
	 * closes the database connection before exiting the program
	 */
	private void setWindowListener() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.out.println(Thread.currentThread().getId());
				try {
					dataController.closeConnection();
				} catch (SQLException ex) {
					for (Throwable t : ex)
						t.printStackTrace();
				}
			}
		});
	}

}
