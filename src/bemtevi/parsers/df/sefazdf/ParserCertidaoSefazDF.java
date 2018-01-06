package bemtevi.parsers.df.sefazdf;

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
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


public class ParserCertidaoSefazDF implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão da Secretaria de Estado de Fazenda/DF";
	private static final List<String> MESES = java.util.Arrays
			.asList(new String[] { "", "Janeiro", "Fevereiro", "Março",
					"Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
					"Outubro", "Novembro", "Dezembro" });
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.MULTILINE_SEARCH);
		pattern.searchFirst("DISTRITO FEDERAL");
		pattern.search("SECRETARIA DE ESTADO DE FAZENDA");
		pattern.search("SUBSECRETARIA DA RECEITA");
		pattern.searchFirst("(CERTIDÃO.*?DE DÉBITOS.*?)", ParserUtilPattern.FULL_LINE);
		pattern.searchNext("Válida até (\\d+) de (\\S+) de (\\d+)");
		pattern.searchNext("Brasília\\S*?, (\\d+) de (\\S+) de (\\d+)");
		pattern.searchNext("Certidão emitida (?:via internet|por \\S+) [àa]s (\\d+:\\d+:?\\d*)");
		pattern.searchFirst("");
		pattern.searchNext("(?:CERTIDÃO NR\\s*)?:\\s*([0-9-./]+)", ParserUtilPattern.FULL_LINE);
		pattern.search("(?:NOME\\s*)?:\\s*(.*?)", ParserUtilPattern.FULL_LINE);
		pattern.search("(?:ENDEREÇO\\s*)?:\\s*(.*?)", ParserUtilPattern.FULL_LINE);
		pattern.search("(?:CIDADE\\s*)?:\\s*(.*?)", ParserUtilPattern.FULL_LINE);
		pattern.search("(?:CPF\\s*)?:?\\s*("+ParserUtil.REGEX_CPF+")?", ParserUtilPattern.FULL_LINE);
		pattern.search("(?:CNPJ\\s*)?:?\\s*("+ParserUtil.REGEX_CNPJ+")?", ParserUtilPattern.FULL_LINE);
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());			
		
		if (matcher.find(true)) {
			String resultado = matcher.nextGroup();
			String validade_dia = matcher.nextGroup();
			String validade_mes = "" + MESES.indexOf(matcher.nextGroup());
			String validade_ano = matcher.nextGroup();
			String dataValidade = validade_dia + "/" + validade_mes + "/"
					+ validade_ano;
			String emissao_dia = matcher.nextGroup();
			String emissao_mes = "" + MESES.indexOf(matcher.nextGroup());
			String emissao_ano = matcher.nextGroup();
			String dataEmissao = emissao_dia + "/" + emissao_mes + "/"
					+ emissao_ano;
			String horaEmissao = matcher.nextGroup();
			
			
			String codigoAutenticacao = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup().replace("\n"," ");
			String endereco = matcher.nextGroup().replace("\n"," ");
			String cidade = matcher.nextGroup();
			String cpf = matcher.nextGroup();
			String cnpj = matcher.nextGroup();
			String cpfCnpj = cpf==null || cpf.length()==0? cnpj : cpf;

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			if (resultado.equalsIgnoreCase("CERTIDÃO NEGATIVA DE DÉBITOS")) {
				nadaConsta.setSituacao(CampoSituacao.CERTIDAO_NEGATIVA);
			} else if (resultado.equalsIgnoreCase("CERTIDÃO POSITIVA DE DÉBITOS COM EFEITO DE NEGATIVA")) {
				nadaConsta.setSituacao(new CampoSituacao(CampoSituacao.CERTIDAO_POSITIVA_COM_EFEITOS_DE_NEGATIVA,"..."));
			} else if (resultado.equalsIgnoreCase("CERTIDÃO DE DÉBITOS")) {
				nadaConsta.setSituacao(new CampoSituacao(CampoSituacao.CERTIDAO_POSITIVA, "..."));
			}
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidade));
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			nadaConsta.setEndereco(endereco + " - " + cidade);
			nadaConsta
					.setLinkValidacao("http://www.fazenda.df.gov.br/area.cfm?id_area=84");
			//nadaConsta.setResultado(resultado);
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {

		try {
			String codigo = certidao.getCodigoAutenticacao().replaceAll("\\D",
					"");
			String cpfCnpj = certidao.getCpfCnpj().replaceAll("\\D", "");
			String params = "certidao=" + codigo + "&destinatario=" + cpfCnpj;
			WebResponse response = ParserUtil
					.downloadFromURL(
							new URL(
									"http://www.fazenda.df.gov.br/aplicacoes/certidao/valida_certidao.cfm"),
							params, null);
			String text = response.getText();

			Map<String, String> expectedFields = new HashMap<String, String>();
			expectedFields.put("res_certidao", certidao.getResultado());
			if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_NEGATIVA.getSituacao()) {
				if (certidao.getNome().equals("NAO CADASTRADO")) {
					expectedFields.put("res_certidao", "CERTIDAO NEGATIVA DE DEBITOS - NAO CADASTRADO");
				} else {
					expectedFields.put("res_certidao", "CERTIDÃO NEGATIVA DE DÉBITOS");
				}
			} else if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_POSITIVA_COM_EFEITOS_DE_NEGATIVA.getSituacao()) {
				expectedFields.put("res_certidao", "CERTIDÃO POSITIVA DE DÉBITOS COM EFEITO DE NEGATIVA");
			} else if (certidao.getSituacao().getSituacao() == CampoSituacao.CERTIDAO_POSITIVA.getSituacao()) {
				expectedFields.put("res_certidao", "CERTIDÃO DE DÉBITOS(?:\\s+-\\s+.*)?");
			}			
			expectedFields.put("hro_usu", certidao.getDataEmissao().getHora()
					+ ":\\d+");
			expectedFields
					.put("dt_emissao", certidao.getDataEmissao().getDia());
			expectedFields.put("dt_validade", certidao.getDataValidade()
					.getDia());

			if (text.matches("(?d).*<div id=\"div_conteudo_impressao\" name=\"div_conteudo_impressao\" >.*?Certidão não encontrada</div>.*")) {
				throw new ValidationException("Certidão Inválida");
			} else {
				List<String> camposDivergentes = new ArrayList<String>();
				for (Entry<String, String> entry : expectedFields.entrySet()) {
					String value = Normalizer.normalize(entry.getValue(),
							Form.NFD).replaceAll(
							"\\p{InCombiningDiacriticalMarks}+", "");
					String expectedField = ".*<input name=\"" + entry.getKey()
							+ "\" type=\"hidden\" value=\"\\s*" + value
							+ "\\s*\">.*";
					// String expectedField = ".*"+entry.getKey() + ".*";
					if (!text.matches(expectedField)) {
						camposDivergentes.add(entry.getKey());
					}
				}
				if (camposDivergentes.size() > 0) {
					throw new ValidationException(
							"Certidão Divergente! Campos Incorretos: "
									+ ParserUtil.join(";", camposDivergentes));
				}
			}

		} catch (IOException e) {
			throw e;
		}
	}

}
