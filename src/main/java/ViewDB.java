import java.awt.*;
import javax.swing.*;

/**
 * This program views arbitrary tables in a database and provide data manipulation options.
 *
 * @author Amr Bumadian
 * @version 1.1 2022-05-24
 * @version 1.0 2022-03-30
 */
public class ViewDB {
	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getId());
		EventQueue.invokeLater(() -> {
			System.out.println(Thread.currentThread().getId());
			var frame = new ViewDBFrame();
			frame.setTitle("ViewDB");
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
