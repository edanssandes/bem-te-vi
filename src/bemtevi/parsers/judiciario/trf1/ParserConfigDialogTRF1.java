package bemtevi.parsers.judiciario.trf1;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import bemtevi.gui.common.DualList;
import bemtevi.parsers.AbstractParserConfig;
import bemtevi.parsers.AbstractParserDialog;


public class ParserConfigDialogTRF1 extends AbstractParserDialog {
	private DualList<String> dualListOrgaos = new DualList<String>();
	private JCheckBox cbSelectOrgaos = new JCheckBox("Selecionar órgãos válidos");
	private DualList<String> dualListTipos = new DualList<String>();
	private JCheckBox cbSelectTipos = new JCheckBox("Selecionar tipos válidos de certidão");
	
	public ParserConfigDialogTRF1() {
		Set<String> names = ConstantesTRF1.getNomeOrgaos();
		dualListOrgaos.setSourceChoicesTitle("Órgãos Inválidos");
		dualListOrgaos.setDestinationChoicesTitle("Órgãos Válidos");
		dualListOrgaos.setFont(dualListOrgaos.getFont().deriveFont(10.0f));
		dualListOrgaos.setItems(names);
		dualListOrgaos.setDisablePolicy(2);
		dualListOrgaos.clear();
		dualListOrgaos.setPreferredSize(new Dimension(500, 200));
		//setPreferredSize(new Dimension(500, 300));
		
		cbSelectOrgaos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateComponentsState();
			}
		});
		
		List<String> tipos = ConstantesTRF1.getTiposCertidao();
		dualListTipos.setSourceChoicesTitle("Tipos Inválidos");
		dualListTipos.setDestinationChoicesTitle("Tipos Válidos");
		dualListTipos.setFont(dualListOrgaos.getFont().deriveFont(10.0f));
		dualListTipos.setItems(tipos);
		dualListTipos.setDisablePolicy(2);
		dualListTipos.clear();
		dualListTipos.setPreferredSize(new Dimension(500, 100));
		
		cbSelectTipos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateComponentsState();
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		cbSelectOrgaos.setAlignmentX( Component.LEFT_ALIGNMENT );
		dualListOrgaos.setAlignmentX( Component.LEFT_ALIGNMENT );
		cbSelectTipos.setAlignmentX( Component.LEFT_ALIGNMENT );
		dualListTipos.setAlignmentX( Component.LEFT_ALIGNMENT );
		add(cbSelectOrgaos);
		add(dualListOrgaos);
		add(cbSelectTipos);
		add(dualListTipos);
		
		
		
		//setBackground(Color.red);
	}
	
	public AbstractParserConfig getConfig() {
		ParserConfigTRF1 config = new ParserConfigTRF1();
		config.setSelecionarOrgaosValidos(cbSelectOrgaos.isSelected());
		if (cbSelectOrgaos.isSelected()) {
			config.setOrgaosValidos(dualListOrgaos.getSelectedItems());
		}
		config.setSelecionarTiposValidos(cbSelectTipos.isSelected());
		if (cbSelectTipos.isSelected()) {
			config.setTiposValidos(dualListTipos.getSelectedItems());
		}
		return config;
	}

	@Override
	public void setConfig(AbstractParserConfig parserConfig) {
		if (parserConfig == null) {
			cbSelectOrgaos.setSelected(false);
			cbSelectTipos.setSelected(false);
			dualListOrgaos.clear();
			dualListTipos.clear();
			updateComponentsState();
		} else {
			ParserConfigTRF1 config = (ParserConfigTRF1)parserConfig;
			cbSelectOrgaos.setSelected(config.isSelecionarOrgaosValidos());
			if (config.isSelecionarOrgaosValidos()) {
				dualListOrgaos.setSelectedItems(config.getOrgaosValidos());
			} else {
				dualListOrgaos.clear();
			}
			cbSelectTipos.setSelected(config.isSelecionarTiposValidos());
			if (config.isSelecionarTiposValidos()) {
				dualListTipos.setSelectedItems(config.getTiposValidos());
			} else {
				dualListTipos.clear();
			}
			updateComponentsState();
		}
	}
	
	private void updateComponentsState() {
		dualListOrgaos.setEnabled(cbSelectOrgaos.isSelected());
		dualListTipos.setEnabled(cbSelectTipos.isSelected());
	}
}
