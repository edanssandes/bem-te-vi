package bemtevi.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import bemtevi.configs.PreferenciasProxy;
import bemtevi.controller.MainController;


/**
 * Janela de Preferência de rede.
 * @author edans
 */
public class PainelRede extends JPanel {
	private JTextField tfProxyHost;
	private JTextField tfProxyPort;
	private ButtonGroup bg;
	private JRadioButton rbProxyNone;
	private JRadioButton rbProxySystem;
	private JRadioButton rbProxyManual;
	private MainController controller;

	public PainelRede() {
		super(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;
		// cs.insets = new Insets(3,3,3,3);
		cs.weightx = 1.0;

		cs.gridy = 0;

		cs.gridx = 0;
		cs.gridwidth = 4;
		cs.weighty = 0.4;
		// cs.fill = GridBagConstraints.REMAINDER;
		JLabel topLabel = new JLabel(
				"<html><font color=gray>Caso seja necessário, informe as opções de proxy da sua coorporação:</font> </html>");
		topLabel.setVerticalAlignment(JLabel.TOP);
		topLabel.setVerticalTextPosition(JLabel.TOP);
		this.add(topLabel, cs);
		cs.weighty = 0;

		cs.gridy++;
		this.add(new JSeparator(), cs);

		bg = new ButtonGroup();
		rbProxyNone = new JRadioButton("Sem Proxy");
		rbProxySystem = new JRadioButton("Utilizar Proxy do Sistema");
		rbProxyManual = new JRadioButton("Configuração Manual do Proxy");
		bg.add(rbProxyNone);
		bg.add(rbProxySystem);
		bg.add(rbProxyManual);

		rbProxyManual.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				tfProxyHost.setEditable(rbProxyManual.isSelected());
				tfProxyPort.setEditable(rbProxyManual.isSelected());
			}
		});

		cs.gridy++;
		this.add(rbProxyNone, cs);
		cs.gridy++;
		this.add(rbProxySystem, cs);
		cs.gridy++;
		this.add(rbProxyManual, cs);

		cs.gridwidth = 1;

		cs.gridx = 0;
		cs.gridy++;
		cs.weightx = 0.0;
		this.add(new JLabel("Endereço: "), cs);

		tfProxyHost = new JTextField(20);
		cs.gridx = 1;
		cs.weightx = 2.0;
		this.add(tfProxyHost, cs);

		cs.gridx = 2;
		cs.weightx = 0.0;
		this.add(new JLabel(":"), cs);

		tfProxyPort = new JTextField(20);
		cs.gridx = 3;
		cs.weightx = 0.5;
		this.add(tfProxyPort, cs);

		cs.gridx = 1;
		cs.gridy++;
		cs.weightx = 0.0;
		cs.gridwidth = 3;
		this.add(
				new JLabel(
						"<html><font color=gray><i>Ex.: proxy.intranet.com.br:3128</i></font>"),
				cs);

		cs.weightx = 0;
		cs.gridx = 0;
		cs.gridwidth = 4;
		cs.gridy++;
		cs.fill = GridBagConstraints.NONE;
		cs.anchor = GridBagConstraints.EAST;
		JButton testButton = new JButton("Teste de Conexão...");
		testButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		testButton.setFont(testButton.getFont().deriveFont(11.0f));
		this.add(testButton, cs);
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PainelRede.this.controller.testConnection(getPreferencias());
			}
		});

		cs.gridx = 0;
		cs.gridy++;
		cs.weightx = 0.0;
		cs.weighty = 1.0;
		this.add(new JPanel(), cs);

		rbProxyNone.setSelected(true);
		tfProxyHost.setEditable(false);
		tfProxyPort.setEditable(false);
	}

	public void setController(MainController controller) {
		this.controller = controller;
	}

	public String getProxyHost() {
		return tfProxyHost.getText();
	}

	public String getProxyPort() {
		return tfProxyPort.getText();
	}

	public void setPreferencias(PreferenciasProxy preferenciasProxy) {
		switch (preferenciasProxy.getProxyTipo()) {
		case SISTEMA:
			rbProxySystem.setSelected(true);
			tfProxyHost.setText("");
			tfProxyPort.setText("");
			break;
		case MANUAL:
			rbProxyManual.setSelected(true);
			tfProxyHost.setText(preferenciasProxy.getProxyHost());
			tfProxyPort.setText(preferenciasProxy.getProxyPort());
			break;
		case NENHUM:
			rbProxyNone.setSelected(true);
			tfProxyHost.setText("");
			tfProxyPort.setText("");
			break;
		}
		tfProxyHost.setEditable(rbProxyManual.isSelected());
		tfProxyPort.setEditable(rbProxyManual.isSelected());
	}

	public PreferenciasProxy getPreferencias() {
		PreferenciasProxy preferenciasProxy = new PreferenciasProxy();
		if (rbProxySystem.isSelected()) {
			preferenciasProxy.setProxyTipo(PreferenciasProxy.ProxyType.SISTEMA);
		} else if (rbProxyManual.isSelected()) {
			preferenciasProxy.setProxyTipo(PreferenciasProxy.ProxyType.MANUAL);
			preferenciasProxy.setProxyHost(tfProxyHost.getText());
			preferenciasProxy.setProxyPort(tfProxyPort.getText());
		} else {
			preferenciasProxy.setProxyTipo(PreferenciasProxy.ProxyType.NENHUM);
		}
		return preferenciasProxy;
	}
}
