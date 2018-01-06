package bemtevi.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Tela de login/senha para autenticação do proxy
 * @author edans
 */
public class UserPasswordDialog {

	
	private JPasswordField password;
	private JTextField username;
	private JPanel panel;
	private static final UserPasswordDialog singleton = new UserPasswordDialog();
	
	public static class UserPasswordResponse {
		private String username;
		private String password;
		public String getUsername() {
			return username;
		}
		public String getPassword() {
			return password;
		}
	}
	
	private UserPasswordDialog() {
	    panel = new JPanel(new BorderLayout(5, 5));

	    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
	    label.add(new JLabel("Usuário", SwingConstants.RIGHT));
	    label.add(new JLabel("Senha", SwingConstants.RIGHT));
	    panel.add(label, BorderLayout.WEST);

	    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
	    username = new JTextField();
	    controls.add(username);
	    password = new JPasswordField();
	    controls.add(password);
	    panel.add(controls, BorderLayout.CENTER);
	}
	
	public static UserPasswordResponse showLoginDialog() {
	    JOptionPane.showConfirmDialog(null, singleton.panel, "login", JOptionPane.OK_CANCEL_OPTION);

	    UserPasswordResponse response = new UserPasswordResponse();
	    response.username = singleton.username.getText();
	    response.password = new String(singleton.password.getPassword());
	    
	    return response;
	}	

}
