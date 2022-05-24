import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

/**
 * The frame that holds the data panel and the navigation buttons.
 */
public class ViewDBFrame extends JFrame {
	private Component scrollPane;
	private final JComboBox<String> tableNames;
	private final DataController dataController;


	public ViewDBFrame() {
		dataController = new DataController();

		tableNames = new JComboBox<>();
		setTableNames();
		tableNames.addActionListener(event -> showTable((String) tableNames.getSelectedItem()));
		add(tableNames, BorderLayout.NORTH);

		setWindowListener();
		constructButtonPanel();

		if (tableNames.getItemCount() > 0) showTable(tableNames.getItemAt(0));
	}

	/**
	 * construct the buttons panel
	 */
	private void constructButtonPanel() {
		var buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		JButton previousButton = new JButton("Previous");
		previousButton.addActionListener(event -> dataController.showPreviousRow());
		buttonPanel.add(previousButton);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(event -> dataController.showNextRow());
		buttonPanel.add(nextButton);

		JButton insertButton = new JButton("Insert");
		insertButton.addActionListener(event -> showInsertPanel());
		buttonPanel.add(insertButton);
		JButton saveButton = new JButton("Update");
		saveButton.addActionListener(event -> dataController.saveChanges());
		buttonPanel.add(saveButton);
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(event -> dataController.deleteRow());
		buttonPanel.add(deleteButton);


	}

	/**
	 * shows the popup panel to get user input
	 */
	private void showInsertPanel() {
		var dialog = constructPopUp();
		dialog.setBounds(50, 50 , 600 , 150);
		dataController.prepareInsert();
		pack();
		dialog.setVisible(true);
	}

	/**
	 * Constructs the popup panel components and their action-listeners
	 */
	private JDialog constructPopUp() {
		var dialog = new JDialog(this, "Inserting new row");

		var dataPanel = new DataPanel(dataController.getCachedRowSet());
		scrollPane = new JScrollPane(dataPanel);
		var buttonPanel = new JPanel();

		var addButton = new JButton("Add");
		addButton.addActionListener(event -> {
			dataController.insertNewRow(dataPanel);
			dialog.dispose();
		});
		buttonPanel.add(addButton);
		var cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(event -> {
			dataController.cancelInsert();
			dialog.dispose();
		});

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.add(scrollPane, BorderLayout.CENTER);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return dialog;
	}

	/**
	 * populates the combobox with the tables names
	 */
	private void setTableNames() {
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
	 * Prepares the text fields for showing a new table then shows the first row.
	 *
	 * @param tableName the name of the table to display
	 */
	public void showTable(String tableName) {
		dataController.viewTableOfName(tableName);
		if (scrollPane != null) remove(scrollPane);
		scrollPane = new JScrollPane(dataController.getDataPanel());
		add(scrollPane, BorderLayout.CENTER);
		dataController.showNextRow();
		pack();
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
