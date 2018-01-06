package bemtevi.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.model.campos.CampoSituacao;
import bemtevi.model.campos.CampoVerificado;
import bemtevi.model.campos.ICampoCertidao;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.ValidationException;


/**
 * Classe que modela as informações de uma certidão.
 * @author edans
 */
public class Certidao implements Comparable<Certidao> {

	public static final String CAMPO_CPF_CNPJ = "CPF/CNPJ";
	public static final String CAMPO_CODIGO_AUTENTICACAO = "Código de Autenticação";
	public static final String CAMPO_DATA_EMISSAO = "Data de Emissão";
	public static final String CAMPO_DATA_VALIDADE = "Data de Validade";
	public static final String CAMPO_ENDERECO = "Endereço";
	public static final String CAMPO_FILIACAO = "Filiação";
	public static final String CAMPO_NOME = "Nome";
	public static final String CAMPO_NOME_FANTASIA = "Nome Fantasia";
	public static final String CAMPO_ORGAO_EMISSOR = "Órgão Emissor";
	public static final String CAMPO_RESULTADO = "Resultado da Certidão";
	public static final String CAMPO_SITUACAO = "Situação";
	public static final String CAMPO_TIPO_CERTIDAO = "Tipo Certidão";
	

	public static final String PESSOA_FISICA = "Pessoa Física";
	public static final String PESSOA_JURIDICA = "Pessoa Jurídica";
	
	public static enum StatusValidacao {
		CERTIDAO_VALIDA, CERTIDAO_INVALIDA, ERRO_DE_VALIDACAO, VALIDACAO_NAO_REALIZADA;
		public String mensagem;
	};

	private Documento documento = null;
	private String linkValidacao = null;
	private String nomeCertidao = null;
	private IParserCertidao parser = null;
	private int parserId = 0;
	private Map<String, Object> extraInfo = new LinkedHashMap<String, Object>();
	private String tipoPessoa;
	private StatusValidacao resultadoValidacao = StatusValidacao.VALIDACAO_NAO_REALIZADA;
	private String mensagemValidacao;

	public Certidao(String nomeCertidao) {
		this.nomeCertidao = nomeCertidao;
	}

	public String getCodigoAutenticacao() {
		return getInfo(CAMPO_CODIGO_AUTENTICACAO);
	}

	public void setCodigoAutenticacao(String codigoAutenticacao) {
		setInfo(CAMPO_CODIGO_AUTENTICACAO, codigoAutenticacao);
	}

	public String getCpfCnpj() {
		return getInfo(CAMPO_CPF_CNPJ);
	}

	public void setCpfCnpj(String cpfCnpj) {
		if (cpfCnpj == null) {
			cpfCnpj = "";
		}
		String cpfCnpjNumeros = cpfCnpj.replaceAll("\\D", "");
		switch (cpfCnpjNumeros.length()) {
		case 11: // CPF possui 11 dígitos
			setInfo(CAMPO_CPF_CNPJ, cpfCnpjNumeros.replaceFirst(
					"(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4"));
			this.tipoPessoa = Certidao.PESSOA_FISICA;
			break;
		case 14: // CNPJ possui 114dígitos
			setInfo(CAMPO_CPF_CNPJ, cpfCnpjNumeros.replaceFirst(
					"(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
					"$1.$2.$3/$4-$5"));
			this.tipoPessoa = Certidao.PESSOA_JURIDICA;
			break;
		default:
			setInfo(CAMPO_CPF_CNPJ, "");
			this.tipoPessoa = null;
		}
	}

	public CampoData getDataEmissao() {
		return (CampoData) getInfoObject(CAMPO_DATA_EMISSAO);
	}

	public void setDataEmissao(CampoData dataEmissao) {
		setInfo(CAMPO_DATA_EMISSAO, dataEmissao);
	}

	public CampoDataValidade getDataValidade() {
		return (CampoDataValidade) getInfoObject(CAMPO_DATA_VALIDADE);
	}

	public void setDataValidade(CampoDataValidade dataValidade) {
		setInfo(CAMPO_DATA_VALIDADE, dataValidade);
	}

	public String getEndereco() {
		return getInfo(CAMPO_ENDERECO);
	}

	public void setEndereco(String endereco) {
		setInfo(CAMPO_ENDERECO, endereco);
	}

	public String getFiliacao() {
		return getInfo(CAMPO_FILIACAO);
	}

	public void setFiliacao(String filiacao) {
		setInfo(CAMPO_FILIACAO, filiacao);
	}

	public String getNome() {
		return getInfo(CAMPO_NOME);
	}

	public void setNome(String nome) {
		setInfo(CAMPO_NOME, nome);
	}

	public String getNomeFantasia() {
		return getInfo(CAMPO_NOME_FANTASIA);
	}

	public void setNomeFantasia(String nomeFantasia) {
		setInfo(CAMPO_NOME_FANTASIA, nomeFantasia);
	}

	public CampoVerificado getOrgaoEmissao() {
		return (CampoVerificado)getInfoObject(CAMPO_ORGAO_EMISSOR);
	}

	public void setOrgaoEmissao(CampoVerificado orgaoEmissor) {
		setInfo(CAMPO_ORGAO_EMISSOR, orgaoEmissor);
	}

	public CampoSituacao getSituacao() {
		return (CampoSituacao) getInfoObject(CAMPO_SITUACAO);
	}

	public void setSituacao(CampoSituacao situacao) {
		setInfo(CAMPO_SITUACAO, situacao);
	}
	
	public String getResultado() {
		return (String)getInfoObject(CAMPO_RESULTADO);
	}
	
	public void setResultado(String resultado) {
		setInfo(CAMPO_RESULTADO, resultado);
	}


	public CampoVerificado getTipoCertidao() {
		return (CampoVerificado)getInfoObject(CAMPO_TIPO_CERTIDAO);
	}
	
	public void setTipoCertidao(CampoVerificado tipoCertidao) {
		setInfo(CAMPO_TIPO_CERTIDAO, tipoCertidao);
	}
	
	public int compareTo(Certidao other) {
		int i;
		i = parserId - other.parserId;
		if (i != 0) {
			return i;
		}
		i = nomeCertidao.compareToIgnoreCase(other.nomeCertidao);
		if (i != 0) {
			return i;
		}
		i = parser.getClass().getName().compareToIgnoreCase(other.parser.getClass().getName());
		if (i != 0) {
			return i;
		}				
		i = getTipoPessoa().compareToIgnoreCase(other.getTipoPessoa());
		if (i != 0) {
			return -i;
		}
		i = getNome().compareToIgnoreCase(other.getNome());
		return i;
	}

	public String getTipoPessoa() {
		if (this.tipoPessoa != null) {
			return this.tipoPessoa;
		} else {
			return "?";
		}
	}

	public Documento getDocumento() {
		return documento;
	}

	public Set<String> getInfoKeys() {
		Set<String> keys = new LinkedHashSet<String>();
		for (String string : extraInfo.keySet()) {
			if (!string.startsWith("#")) {
				keys.add(string);
			}
		}
		return keys;
	}

	public String getInfo(String key) {
		// return extraInfo.getOrDefault(key, ""); // No such method error
		Object value = getInfoObject(key);
		if (value != null) {
			return value.toString();
		} else {
			return "";
		}
	}

	public Object getInfoObject(String key) {
		if (extraInfo.containsKey(key)) {
			return extraInfo.get(key);
		} else {
			return null;
		}		
	}

	public String getLinkValidacao() {
		return linkValidacao;
	}

	public String getNomeCertidao() {
		return nomeCertidao;
	}

	public IParserCertidao getParser() {
		return parser;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public void setExtraInfo(String key, String val) {
		setInfo(key, val);
	}
	
	public void setExtraInfo(String key, ICampoCertidao val) {
		setInfo(key, val);
	}
	
	private void setInfo(String key, String val) {
		extraInfo.put(key, val);
	}

	private void setInfo(String key, ICampoCertidao val) {
		extraInfo.put(key, val);
	}

	public void setLinkValidacao(String linkValidacao) {
		this.linkValidacao = linkValidacao;
	}

	public void setParser(IParserCertidao parser) {
		this.parser = parser;
	}

	public void setParserId(int parserId) {
		this.parserId = parserId;
	}

	public int getParserId() {
		return this.parserId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Certidao)) {
			return false;
		}
		Certidao other = (Certidao)obj;
		try {
			this.assertEquals(other);
			return true;
		} catch (ValidationException e) {
			return false;
		}
	}
	
	public String getMensagemValidacao() {
		return mensagemValidacao;
	}

	public void setMensagemValidacao(String mensagemValidacao) {
		this.mensagemValidacao = mensagemValidacao;
	}

	public StatusValidacao getResultadoValidacao() {
		return resultadoValidacao;
	}

	public void setResultadoValidacao(StatusValidacao resultadoValidacao) {
		this.resultadoValidacao = resultadoValidacao;
	}

	public void assertEquals(Certidao other) throws ValidationException {
		this.assertEquals(other, null);
	}
	
	public void assertEquals(Certidao other, String[] excludeFields) throws ValidationException {
		if (parser.getClass() != other.parser.getClass()
				|| !nomeCertidao.equals(other.nomeCertidao)) {
			throw new ValidationException("Tipos diferentes de Certidão");
		}
		System.out.println(extraInfo);
		System.out.println(other.extraInfo);
		
		Map<String, Object> extraInfoFiltered = new LinkedHashMap<String, Object>(extraInfo);
		Map<String, Object> otherExtraInfoFiltered = new LinkedHashMap<String, Object>(other.extraInfo);
		if (excludeFields != null) {
			for (String key : excludeFields) {
				extraInfoFiltered.remove(key);
				otherExtraInfoFiltered.remove(key);
			}
		}
		
		if (!extraInfoFiltered.equals(otherExtraInfoFiltered)) {
			Map<String, Object> difference = new LinkedHashMap<String, Object>();
	        difference.putAll(extraInfoFiltered);
	        difference.putAll(otherExtraInfoFiltered);

	        difference.entrySet().removeAll(extraInfoFiltered.size() <= otherExtraInfoFiltered.size() ? extraInfoFiltered.entrySet() : otherExtraInfoFiltered.entrySet());
			
			throw new ValidationException("Certidões Divergentes. (Campos: " + difference.keySet() + ")");
		}
	}

}
