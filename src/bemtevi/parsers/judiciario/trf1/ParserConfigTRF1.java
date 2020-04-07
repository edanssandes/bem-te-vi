package bemtevi.parsers.judiciario.trf1;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bemtevi.parsers.AbstractParserConfig;


public class ParserConfigTRF1 extends AbstractParserConfig {
	private static final String KEY_ORGAOS_VALIDOS = "orgaosValidos";
	private static final String KEY_TIPOS_VALIDOS = "tiposValidos";
	private boolean selecionarOrgaosValidos;
	private List<String> orgaosValidos = new ArrayList<String>();
	private boolean selecionarTiposValidos;
	private List<String> tiposValidos = new ArrayList<String>();

	@Override
	public Properties getProperties() {
		Properties properties = new Properties();
		if (selecionarOrgaosValidos) {
			StringBuilder sb = new StringBuilder();
			for (String orgao : orgaosValidos) {
				sb.append(";" + ParserCertidaoTRF1.getIdFromOrgao(orgao));
			}
			if (sb.length() > 0) {
				properties.put(KEY_ORGAOS_VALIDOS, sb.substring(1));
			}
		}
		if (selecionarTiposValidos) {
			StringBuilder sb = new StringBuilder();
			for (String tipo : tiposValidos) {
				sb.append(";" + tipo);
			}
			if (sb.length() > 0) {
				properties.put(KEY_TIPOS_VALIDOS, sb.substring(1));
			}
		}
		return properties;
	}

	@Override
	public void setProperties(Properties properties) {
		orgaosValidos.clear();
		selecionarOrgaosValidos = properties.containsKey(KEY_ORGAOS_VALIDOS);
		if (selecionarOrgaosValidos) {
			String sb = properties.getProperty(KEY_ORGAOS_VALIDOS);
			String[] orgaos = sb.split(";");
			for (String orgaoId : orgaos) {
				orgaosValidos.add(ParserCertidaoTRF1.getOrgaoFromId(orgaoId));
			}
		}
		selecionarTiposValidos = properties.containsKey(KEY_TIPOS_VALIDOS);
		if (selecionarTiposValidos) {
			String sb = properties.getProperty(KEY_TIPOS_VALIDOS);
			String[] tipos = sb.split(";");
			for (String tipo : tipos) {
				tiposValidos.add(tipo);
			}
		}
	}

	public boolean isSelecionarOrgaosValidos() {
		return selecionarOrgaosValidos;
	}

	public void setSelecionarOrgaosValidos(boolean selecionarOrgaosValidos) {
		this.selecionarOrgaosValidos = selecionarOrgaosValidos;
	}

	public List<String> getOrgaosValidos() {
		return orgaosValidos;
	}

	public void setOrgaosValidos(List<String> orgaosValidos) {
		this.orgaosValidos = orgaosValidos;
	}

	public boolean isSelecionarTiposValidos() {
		return selecionarTiposValidos;
	}

	public void setSelecionarTiposValidos(boolean selecionarTiposValidos) {
		this.selecionarTiposValidos = selecionarTiposValidos;
	}

	public List<String> getTiposValidos() {
		return tiposValidos;
	}

	public void setTiposValidos(List<String> tiposValidos) {
		this.tiposValidos = tiposValidos;
	}

}
