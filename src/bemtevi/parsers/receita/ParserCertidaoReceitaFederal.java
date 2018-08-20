package bemtevi.parsers.receita;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.model.campos.CampoSituacao;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;



public class ParserCertidaoReceitaFederal implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão Relativa aos Tributos Federais e à Dívida Ativa da União - Receita Federal/PGFN";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);

	private enum TipoCertidao {
		NEGATIVA(1,"CERTIDÃO NEGATIVA","Negativa"), 
		POSITIVA_COM_EFEITOS_DE_NEGATIVA(2,"CERTIDÃO (?:CONJUNTA )?POSITIVA COM EFEITOS DE NEGATIVA","Positiva com Efeitos de Negativa"),
		POSITIVA(3,"CERTIDÃO POSITIVA", "Positiva"); 
		int codigo;
		String descricao;
		String descricao2;
		
		TipoCertidao(int codigo, String descricao, String descricao2) {
			this.codigo = codigo;
			this.descricao = descricao;
			this.descricao2 = descricao2;
		}
	}

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("MINISTÉRIO DA FAZENDA");
		pattern.searchFirst("Secretaria da Receita Federal do Brasil");
		pattern.searchFirst("Procuradoria-Geral da Fazenda Nacional");
		pattern.searchFirst("("+TipoCertidao.NEGATIVA.descricao+"|"+TipoCertidao.POSITIVA_COM_EFEITOS_DE_NEGATIVA.descricao+"|"+TipoCertidao.POSITIVA.descricao + ") DE DÉBITOS RELATIVOS AOS TRIBUTOS FEDERAIS E À DÍVIDA ATIVA DA UNIÃO");
		pattern.search("Nome: (.*?)");
		pattern.search("(?:CPF|CNPJ): (" + ParserUtil.REGEX_CPF_OU_CNPJ + ")");
		pattern.searchNext("Emitida às\\s(\\S*)\\sdo dia\\s(\\S*)\\s");
		pattern.searchNext("Válida até\\s*(\\S+?)\\.");
		pattern.searchNext("Código de controle da certidão:\\s*(\\S+?)\\s");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());			

		if (matcher.find()) {
			String resultado = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String dataEmissao = matcher.nextGroup();
			String dataValidade = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			
			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);		
			if (resultado.matches(TipoCertidao.NEGATIVA.descricao)) {
				nadaConsta.setSituacao(CampoSituacao.CERTIDAO_NEGATIVA);
			} else if (resultado.matches(TipoCertidao.POSITIVA_COM_EFEITOS_DE_NEGATIVA.descricao)) {
				nadaConsta.setSituacao(CampoSituacao.CERTIDAO_POSITIVA_COM_EFEITOS_DE_NEGATIVA);
			} else if (resultado.matches(TipoCertidao.POSITIVA.descricao)) {
				nadaConsta.setSituacao(CampoSituacao.CERTIDAO_POSITIVA);
			}			
			
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidade));
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			String link = "http://www.receita.fazenda.gov.br/Aplicacoes/ATSPO/Certidao/certaut/CndConjunta/ConfirmaAutenticCndSolicitacao.asp?ORIGEM=";
			if (nadaConsta.getTipoPessoa() == Certidao.PESSOA_FISICA) {
				link += "PF";
			} else {
				link += "PJ";
			}
			//"http://www.receita.fazenda.gov.br/Aplicacoes/ATSPO/Certidao/CndConjuntaInter/InformaNICertidao.asp?Tipo=1"
			nadaConsta.setLinkValidacao(link);
			


			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		try {
			TipoCertidao tipoCertidao = null;
			if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_NEGATIVA.getSituacao()) {
				tipoCertidao = TipoCertidao.NEGATIVA;
			} else if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_POSITIVA_COM_EFEITOS_DE_NEGATIVA.getSituacao()) {
				tipoCertidao = TipoCertidao.POSITIVA_COM_EFEITOS_DE_NEGATIVA;
			} else if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_POSITIVA.getSituacao()) {
				tipoCertidao = TipoCertidao.POSITIVA;
			}
			
			String params = "txt_CPF_CNPJce=" + certidao.getCpfCnpj().replaceAll("\\/", "%2F") +
					"&DtEmissao=" + certidao.getDataEmissao().getDia().replaceAll("\\/", "%2F") +
					"&HrEmissao=" + certidao.getDataEmissao().getHoraSec().replaceAll(":", "%3A") +
					"&txtCodCertidao=" + certidao.getCodigoAutenticacao() +
					"&cboTipoCertidaoPGFN="+tipoCertidao.codigo+"&" +
					"txtorigem=" + (certidao.getTipoPessoa() == Certidao.PESSOA_FISICA?"PF":"PJ");
			WebResponse response = ParserUtil.downloadFromURL(new URL("http://servicos.receita.fazenda.gov.br/Servicos/certidao/certaut/CndConjunta/ConfirmaAutenticResultado.asp"), params, null);
			String text = response.getText();
			if (text.contains("A Certidão não é autêntica. Verifique os dados informados.")) {
				throw new ValidationException("Certidão não é autêntica");
			}
			Map<String, String> expectedFields = new LinkedHashMap<String, String>();
			expectedFields.put("CPF/CNPJ", "(CPF|CNPJ)\\S{0,30}"+certidao.getCpfCnpj());
			//expectedFields.put("CPF/CNPJ", "33");
			expectedFields.put("Data da Emissão", "Data da Emissão\\S{0,30}"+certidao.getDataEmissao().getDia());
			expectedFields.put("Hora da Emissão", "Hora da Emissão\\S{0,30}"+certidao.getDataEmissao().getHoraSec());
			expectedFields.put("Código de Controle", "Código de Controle da Certidão\\S{0,30}"+certidao.getCodigoAutenticacao());
			expectedFields.put("Tipo da Certidão", "Tipo da Certidão\\S{0,30}"+tipoCertidao.descricao2);
			
			List<String> camposDivergentes = new ArrayList<String>();
			for (Entry<String, String> entry : expectedFields.entrySet()) {
				if (!text.matches(".*<b>" + entry.getValue() + "</font>.*")) {
					camposDivergentes.add(entry.getKey());
				}
			}
			if (camposDivergentes.size() > 0) {
				throw new ValidationException(
						"Certidão Divergente! Campos Incorretos: "
								+ ParserUtil.join(";", camposDivergentes));
			}
			
			
			System.out.println(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
	}

}
