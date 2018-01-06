package bemtevi.gui.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bemtevi.configs.PreferenciasRelatorioHTML;


/**
 * Janela de preferências dos Relatórios.
 * @author edans
 */
public class PainelRelatorio extends JPanel {
	private PopupTextArea txtCabecalho;
	private PopupTextArea txtRodape;
	
	private static class PopupTextArea extends JPanel {
		JTextField field = new JTextField();
		JTextArea area = new JTextArea();
		JButton testButton = new JButton("Editar...");
		String text;
		
		public PopupTextArea() {
			super(new GridBagLayout());
	        GridBagConstraints cs = new GridBagConstraints();
	        
	        cs.fill = GridBagConstraints.HORIZONTAL;

	        testButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
	        testButton.setFont(testButton.getFont().deriveFont(11.0f));
			field.setEditable(false);
			//area.setPreferredSize(new Dimension(400,200));

			cs.gridx = 0;
	        cs.weightx = 1.0;
			add(field, cs);
			
			cs.gridx = 1;
			cs.weightx = 0;
	        add(testButton, cs);
	        
	        testButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	area.setText(text);
	            	area.setLineWrap(true);
	            	area.setWrapStyleWord(true);
	            	JScrollPane pane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	            	pane.setPreferredSize(new Dimension(400,200));
	            	area.setCaretPosition(0);
	            	
	            	int ret = JOptionPane.showConfirmDialog(PopupTextArea.this, pane, "Editor de Campo", JOptionPane.OK_CANCEL_OPTION);
	            	if (ret == JOptionPane.OK_OPTION) {
	            		text = area.getText();
	            		field.setText(text);
	            	}
	            }
	        });
			
		}

		public String getText() {
			if (text == null) {
				return "";
			} else {
				return text;
			}
		}

		public void setText(String text) {
			if (text == null) {
				this.text = "";
			} else {
				this.text = text;
			}
			field.setText(text);
		}
	}
	
	public PainelRelatorio() {
		super(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        
        cs.fill = GridBagConstraints.HORIZONTAL;
        //cs.insets = new Insets(3,3,3,3);
        cs.weightx = 1.0;
        
        cs.gridy = 0;
        
        cs.gridx = 0;
        cs.gridwidth = 4;
        cs.weighty = 0.4;
        //cs.fill = GridBagConstraints.REMAINDER;
        JLabel topLabel = new JLabel("<html><font color=gray>Configurações personalizadas do relatório:</font> </html>");
        topLabel.setVerticalAlignment(JLabel.TOP);
        topLabel.setVerticalTextPosition(JLabel.TOP);
        this.add(topLabel, cs);
        cs.weighty = 0;
        
        cs.gridy++;
        this.add(new JSeparator(), cs);

        cs.gridwidth = 1; 
        
        cs.gridy++;
		cs.gridx = 0;
		cs.weightx = 0;
		this.add(new JLabel("Cabeçalho:"), cs);
		cs.gridx = 1;
		cs.weightx = 1.0;
		txtCabecalho = new PopupTextArea();
		this.add(txtCabecalho, cs);

        cs.gridy++;
		cs.gridx = 0;
		cs.weightx = 0;
		this.add(new JLabel("Rodapé:"), cs);
		cs.gridx = 1;
		cs.weightx = 1.0;
		txtRodape = new PopupTextArea();
		this.add(txtRodape, cs);
		
		
        cs.gridx = 0;
        cs.gridy++;
        cs.weightx = 0.0;
        cs.weighty = 1.0;
        this.add(new JPanel(), cs);
        
	}
	
	public void setPreferencias(PreferenciasRelatorioHTML preferenciasRelatorioHTML) {
		txtCabecalho.setText(preferenciasRelatorioHTML.getHeader());
		txtRodape.setText(preferenciasRelatorioHTML.getFooter());
	}

	public PreferenciasRelatorioHTML getPreferencias() {
		PreferenciasRelatorioHTML preferenciasRelatorioHTML = new PreferenciasRelatorioHTML();
		preferenciasRelatorioHTML.setHeader(txtCabecalho.getText());
		preferenciasRelatorioHTML.setFooter(txtRodape.getText());
		return preferenciasRelatorioHTML;
	}
}
