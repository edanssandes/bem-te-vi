package bemtevi.gui.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import bemtevi.configs.PreferenciaParser;
import bemtevi.configs.PreferenciasPerfil;
import bemtevi.configs.PreferenciasPerfis;
import bemtevi.gui.icons.Icons;
import bemtevi.parsers.AbstractParserDialog;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.parsers.ParserSingletons;


/**
 * Janela de preferências dos analisadores de certidões (parsers).
 * @author edans
 */
public class PainelParsers extends JPanel {
	private static final Border border = BorderFactory.createEtchedBorder(1);

	private List<ParserRow> parserRows = new ArrayList<ParserRow>();
	private JComboBox<String> cbPerfis;
	private PreferenciasPerfis perfis = new PreferenciasPerfis();
	private boolean dirty = false;
	private int selectedIndex = 0;

	private JButton createProfileButton;
	private JButton deleteProfileButton;
	private JButton editProfileButton;

	private JScrollPane parsersPanel;

	public PainelParsers() {
		super(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0.0;

		cs.gridy = 1;

		cs.gridx = 0;
		cs.gridwidth = 5;
		cs.weighty = 0.2;
		// cs.fill = GridBagConstraints.REMAINDER;
		JLabel topLabel = new JLabel(
				"<html><font color=gray>Analisadores de Certidões Ativos:</font> </html>");
		topLabel.setVerticalAlignment(JLabel.TOP);
		topLabel.setVerticalTextPosition(JLabel.TOP);
		this.add(topLabel, cs);
		cs.weighty = 0;

		cs.gridwidth = 1;
		cs.gridy = 0;
		cs.gridx = 0;
		cs.weightx = 0;
		this.add(new JLabel("Perfil:"), cs);
		cs.gridx++;
		cs.weightx = 1.0;
		// String[] perfis = new String[] {"Padrão", "Auto Escola - Anual",
		// "Auto Escola - Trimestral", "Clínica - Anual",
		// "Clínica - Trimestral"};
		cbPerfis = new JComboBox<String>();
		cbPerfis.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					if (cbPerfis.getSelectedIndex() == selectedIndex) {
						// Avoid double operation when canceling selecion
						return;
					}
					if (dirty) {
						int ret = JOptionPane.showConfirmDialog(PainelParsers.this, "Deseja salvar as alterações feitas no perfil atual?");
						if (ret == JOptionPane.CANCEL_OPTION) {
							cbPerfis.setSelectedIndex(selectedIndex); // Return Previous State 
							return; // Cancel Selection
						} else if (ret == JOptionPane.YES_OPTION) {
							updatePreferenciasPerfil();
						} else if (ret == JOptionPane.NO_OPTION) {
							dirty = false;
						}
					}
					reloadPreferenciasPerfil(cbPerfis.getSelectedIndex());
					break;
				}
			}
		});
		this.add(cbPerfis, cs);

		createProfileButton = new JButton(Icons.getIcon(Icons.ICON_PLUS));
		deleteProfileButton = new JButton(Icons.getIcon(Icons.ICON_MINUS));
		editProfileButton = new JButton(Icons.getIcon(Icons.EDIT_MINUS));
		Border border = BorderFactory.createEtchedBorder(1);

		createProfileButton.setToolTipText("Criar Novo Perfil");
		deleteProfileButton.setToolTipText("Excluir Perfil Selecionado");
		editProfileButton.setToolTipText("Editar Perfil Selecionado");
		
		editProfileButton.setBorderPainted(false);
		editProfileButton.setPreferredSize(new Dimension(16, 16));
		editProfileButton.setFocusable(false);
		editProfileButton.setBackground(Color.white);
		editProfileButton.setOpaque(false);
		editProfileButton.setBorder(border);
		editProfileButton.setEnabled(true);
		editProfileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selectedIndex = cbPerfis.getSelectedIndex();
				String nome = JOptionPane.showInputDialog(PainelParsers.this,
						"Informe o novo nome do perfil:",
						perfis.getPerfil(selectedIndex));
				if (nome != null) {
					perfis.getPerfil(selectedIndex).setNomePerfil(nome);
					updateComboBoxPerfis(selectedIndex);
				}
			}
		});

		createProfileButton.setBorderPainted(false);
		// createProfileButton.setContentAreaFilled(false);
		// createProfileButton.setFocusPainted(false);
		createProfileButton.setPreferredSize(new Dimension(16, 16));
		createProfileButton.setFocusable(false);
		createProfileButton.setBackground(Color.WHITE);
		createProfileButton.setOpaque(false);
		createProfileButton.setBorder(border);
		createProfileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				String nome = JOptionPane.showInputDialog(PainelParsers.this,
						"Informe o nome do novo perfil:");
				if (nome != null && !nome.isEmpty()) {
					perfis.createPerfil(nome);
					int id = perfis.getPerfis().size() - 1;
					updateComboBoxPerfis(id);
				}
			}
		});

		deleteProfileButton.setBorderPainted(false);
		deleteProfileButton.setPreferredSize(new Dimension(16, 16));
		deleteProfileButton.setFocusable(false);
		deleteProfileButton.setBackground(Color.white);
		deleteProfileButton.setOpaque(false);
		deleteProfileButton.setBorder(border);
		deleteProfileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				int selectedIndex = cbPerfis.getSelectedIndex();

				int confirm = JOptionPane.showConfirmDialog(PainelParsers.this,
						"Deseja realmente excluir o perfil \""
								+ perfis.getPerfil(selectedIndex)
										.getNomePerfil() + "\"", null,
						JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.OK_OPTION) {
					perfis.deletePerfil(selectedIndex);
					updateComboBoxPerfis(0);
					reloadPreferenciasPerfil(0);
				}
			}
		});

		cs.fill = GridBagConstraints.VERTICAL;
		cs.weightx = 0;

		cs.gridx++;
		this.add(editProfileButton, cs);
		cs.gridx++;
		this.add(createProfileButton, cs);
		cs.gridx++;
		this.add(deleteProfileButton, cs);
		// cs.weightx = 1.0;

		// cs.fill = GridBagConstraints.HORIZONTAL;
		// cs.gridx = 0;

		// cs.gridy++;
		// this.add(new JSeparator(), cs);

		IParserCertidao[] x = ParserSingletons.getParsers();

		JPanel parserTable = new JPanel(new GridLayout(x.length, 1));
		parserTable.setMinimumSize(new Dimension(1, 1));
		for (IParserCertidao a : x) {
			ParserRow row = new ParserRow(a);
			parserRows.add(row);
			parserTable.add(row);
		}

		JPanel table4 = new JPanel(new GridBagLayout());
		GridBagConstraints cs3 = new GridBagConstraints();
		cs3.gridy = 0;
		cs3.fill = GridBagConstraints.HORIZONTAL;
		cs3.gridwidth = 1;
		cs3.weightx = 1;
		cs3.weighty = 1;
		cs3.anchor = GridBagConstraints.NORTH;
		// cs3.anchor = GridBagConstraints.

		table4.add(parserTable, cs3);

		parsersPanel = new JScrollPane(table4);

		parsersPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		parsersPanel
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cs.gridwidth = 4;

		cs.gridy = 3;
		cs.gridx = 0;
		cs.weightx = 1.0;
		cs.weighty = 1.0;
		cs.fill = GridBagConstraints.BOTH;

		this.add(parsersPanel, cs);
	}

	public void setPreferencias(PreferenciasPerfis perfis) {
		this.perfis = perfis;
		updateComboBoxPerfis(selectedIndex);
		// reloadPreferenciasPerfil(perfis.getPerfilSelecionado());
		reloadPreferenciasPerfil(selectedIndex);
	}

	public PreferenciasPerfis getPreferencias() {
		updatePreferenciasPerfil();
		return perfis;
	}

	private void setPreferenciasPerfil(PreferenciasPerfil preferenciasParsers) {
		int i = 0;
		for (PreferenciaParser preferencia : preferenciasParsers
				.getPreferencias()) {
			parserRows.get(i).setPreferencia(preferencia);
			i++;
		}
	}

	private void updatePreferenciasPerfil() {
		if (dirty) {
			dirty = false;
			String nomePerfil = perfis.getPerfil(selectedIndex)
					.getNomePerfil();
			PreferenciasPerfil preferenciasParsers = new PreferenciasPerfil();
			preferenciasParsers.setNomePerfil(nomePerfil);
			for (ParserRow row : parserRows) {
				preferenciasParsers.addPreferencia(row.getPreferencia());
			}

			perfis.getPerfis().set(selectedIndex, preferenciasParsers);
		}
	}

	private void reloadPreferenciasPerfil(int selectedIndex) {
		// perfis.setPerfilSelecionado(selectedIndex);
		this.selectedIndex = selectedIndex;
		setPreferenciasPerfil(perfis.getPerfil(selectedIndex));

		boolean editable = (selectedIndex > 0); // Perfil Padrão não é editável
		editProfileButton.setEnabled(editable);
		deleteProfileButton.setEnabled(editable);
		parsersPanel.setEnabled(editable);
		for (ParserRow row : parserRows) {
			row.setEnabled(editable);
		}			
	}

	private void updateComboBoxPerfis(int selectedIndex) {
		cbPerfis.setModel(new DefaultComboBoxModel<String>(perfis
				.getNomesPerfis()));
		cbPerfis.setSelectedIndex(selectedIndex);
	}

	private class ParserRow extends JPanel {
		private IParserCertidao parser;
		private JCheckBox cbAtivo;
		private JLabel label;
		private AbstractParserDialog configPanel;
		private JButton configButton;

		public ParserRow(IParserCertidao parser) {
			super(new BorderLayout());
			this.parser = parser;

			cbAtivo = new JCheckBox();
			this.add(cbAtivo, BorderLayout.WEST);
			cbAtivo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dirty = true;
					ParserRow.this.setSelected(cbAtivo.isSelected());
				}
			});

			label = new JLabel(parser.getParserInfo().getName());
			label.setFont(label.getFont().deriveFont(10.0f));
			label.setHorizontalAlignment(JLabel.LEFT);
			label.setPreferredSize(new Dimension(100, 10));
			this.add(label, BorderLayout.CENTER);

			if (parser instanceof IParserConfigurable) {
				final IParserConfigurable configurable = (IParserConfigurable) parser;
				configPanel = configurable.getConfigDialog();
				configButton = new JButton(Icons.getIcon(Icons.ICON_GEAR));
				configButton.setBorderPainted(false);
				configButton.setPreferredSize(new Dimension(16, 16));
				configButton.setFocusable(false);
				configButton.setBackground(Color.white);
				configButton.setOpaque(false);
				configButton.setBorder(border);
				this.add(configButton, BorderLayout.EAST);
				configButton.addActionListener(new ActionListener() {


					public void actionPerformed(ActionEvent arg0) {
						JDialog windowAncestor = (JDialog) SwingUtilities
								.getWindowAncestor(ParserRow.this);
						String parserName = ParserRow.this.parser
								.getParserInfo().getName();
						JDialog dialog = new JDialog(windowAncestor,
								parserName, true);
						dialog.setLayout(new BorderLayout());
						dialog.getContentPane().add(configPanel, BorderLayout.CENTER);
						//dialog.pack();
						//dialog.setVisible(true);
						
						
						int ret = JOptionPane.showConfirmDialog(ParserRow.this, configurable.getConfigDialog(), parserName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (ret == JOptionPane.OK_OPTION) {
							configPanel.getConfig();
							dirty = true;
						}
					}

				});
			}
		}

		private void setSelected(boolean selected) {
			cbAtivo.setSelected(selected);
			label.setEnabled(selected);
			if (configButton != null) {
				configButton.setEnabled(selected & cbAtivo.isSelected());
			}
		}

		public void setPreferencia(PreferenciaParser preferencia) {
			setSelected(preferencia.isAtivo());
			if (configPanel != null) {			
				configPanel.setConfig(preferencia.getConfig());
			}
		}

		public PreferenciaParser getPreferencia() {
			PreferenciaParser preferencia = new PreferenciaParser(parser);
			preferencia.setAtivo(cbAtivo.isSelected());
			if (configPanel != null) {
				preferencia.setConfig(configPanel.getConfig());
			}
			return preferencia;
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			cbAtivo.setEnabled(enabled);
			label.setEnabled(enabled & cbAtivo.isSelected());
			if (configButton != null) {
				configButton.setEnabled(enabled & cbAtivo.isSelected());
			}
		}
	}

}
