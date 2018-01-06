package bemtevi.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import bemtevi.configs.Preferencias;
import bemtevi.configs.PreferenciasPerfil;
import bemtevi.configs.PreferenciasPerfis;
import bemtevi.configs.PreferenciasProxy;
import bemtevi.configs.PreferenciasRelatorioHTML;
import bemtevi.controller.MainController;
import bemtevi.gui.icons.Icons;
import bemtevi.model.CertidaoFactory;
import bemtevi.parsers.IParserCertidao;


/**
 * Janela de preferências
 * @author edans
 */
public class PreferencesDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainController controller;

	private static final String TITLE = "Preferências de Configuração";

	
    private JButton btnOk;
    private JButton btnCancel;
	
	private PainelRelatorio painelRelatorio;
	private PainelRede painelRede;
	private PainelParsers painelParsers;
    
	public PreferencesDialog(MainController controller, Frame owner) {
		super(owner, TITLE, true);
		this.controller = controller;
		JTabbedPane tabPane = new JTabbedPane();
		
		painelRelatorio = new PainelRelatorio();
		painelRede = new PainelRede();
		painelRede.setController(controller);
		painelParsers = new PainelParsers();
		
		tabPane.add("Relatórios", painelRelatorio);
		tabPane.add("Rede", painelRede);
		tabPane.add("Analisadores", painelParsers);

        btnOk = new JButton("Ok");
        
        btnOk.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	Preferencias preferencias = getPreferencias();
            	PreferencesDialog.this.controller.savePreferencias(preferencias);
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(btnOk);
        bp.add(btnCancel);
 
        getContentPane().add(tabPane, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        getContentPane().setPreferredSize(new Dimension(400, 300));
        pack();
        //setResizable(false);
        //setLocationRelativeTo(parent);	    
		
	}

	public Preferencias getPreferencias() {
		PreferenciasProxy preferenciasProxy = painelRede.getPreferencias();
		PreferenciasRelatorioHTML preferenciasrelatorioHTML = painelRelatorio.getPreferencias();
		PreferenciasPerfis preferenciasParsers = painelParsers.getPreferencias();
		
		return new Preferencias(preferenciasProxy, preferenciasrelatorioHTML, preferenciasParsers);
	}
	
	public void setPreferencias(Preferencias preferencias) {
		PreferenciasProxy preferenciasProxy = preferencias.getPreferenciasProxy();
		PreferenciasRelatorioHTML preferenciasRelatorioHTML = preferencias.getPreferenciasRelatorioHTML();
		PreferenciasPerfis preferenciasParsersPerfis = preferencias.getPreferenciasParsersPerfis();
		
		painelRede.setPreferencias(preferenciasProxy);
		painelRelatorio.setPreferencias(preferenciasRelatorioHTML);
		painelParsers.setPreferencias(preferenciasParsersPerfis);
	}

    public static void main(String[] args) {
		PreferencesDialog dialog = new PreferencesDialog(null, null);
		dialog.setPreferencias(new Preferencias());
		dialog.setVisible(true);
		System.exit(0);
	}
	
}

