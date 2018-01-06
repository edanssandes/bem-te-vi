package bemtevi.parsers.judiciario.tst;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import bemtevi.model.Certidao;
import bemtevi.model.CertidaoFactory;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;


public class ParserCertidaoTST implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão de Débitos Trabalhistas - TST";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(
				ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("CERTIDÃO NEGATIVA DE DÉBITOS TRABALHISTAS");
		pattern.search("Nome: (.*?) (?:CPF|CNPJ): ("
				+ ParserUtil.REGEX_CPF_OU_CNPJ + ")");
		pattern.search("Certidão n[oº]: (\\d+)/(\\d+)");
		pattern.search("Expedição: (\\S+), às (\\d+:\\d+:\\d+)");
		pattern.search("Validade: (\\S+)\\s");
		pattern.searchNext("NÃO CONSTA do Banco Nacional de Devedores Trabalhistas");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());

		if (matcher.find()) {
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String codigoAutenticacao1 = matcher.nextGroup();
			String codigoAutenticacao2 = matcher.nextGroup();
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String dataValidade = matcher.nextGroup();

			String codigoAutenticacao = codigoAutenticacao1 + "/"
					+ codigoAutenticacao2;

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidade));
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			nadaConsta
					.setLinkValidacao("http://aplicacao.jt.jus.br/cndtCertidao/inicio.faces");
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException,
			IOException {
		try {
			// Primeira requisição: obtém o cookie, o código de download (pfnc) e o viewstate
			WebResponse response0 = ParserUtil.downloadFromURL(
							new URL("http://aplicacao.jt.jus.br/cndtCertidao/consultarCertidao.faces"),
							null, null);
			String texto0 = response0.getText("utf-8");
			String savedCookie = response0.getCookie();

			Pattern p0 = Pattern.compile("window.parent.location = 'http://aplicacao.jt.jus.br/cndtCertidao/emissaoCertidao\\?pfnc=(\\d+)'.*"
							+ "id=\"javax.faces.ViewState\" value=\"(.*?)\"", Pattern.MULTILINE);
			Matcher m0 = p0.matcher(texto0);
			if (!m0.find()) {
				throw new IOException("Erro no conteúdo da validação do TST (1).");
			}
			String pfnc = m0.group(1);
			String viewState = m0.group(2);

			// Dividindo o código de autenticação - codigo/ano - ex. "45540059/2014" 
			String[] codigoAutenticacao = certidao.getCodigoAutenticacao().split("/"); 

			// Segunda requisição: usada para informar os dados da certidão
			String params1 = "AJAXREQUEST=j_id_jsp_1421772967_0&"
					+ "validarCertidaoForm=validarCertidaoForm&"
					+ "validarCertidaoForm%3AcpfCnpj=" + certidao.getCpfCnpj() + "&"
					+ "validarCertidaoForm%3AnumCertidao=" + codigoAutenticacao[0] + "&"
					+ "validarCertidaoForm%3AanoCertidao=" + codigoAutenticacao[1] + "&"
					+ "javax.faces.ViewState=" + URLEncoder.encode(viewState, "UTF-8") + "&"
					+ "validarCertidaoForm%3AbtnValidarCertidao=validarCertidaoForm%3AbtnValidarCertidao&";

			WebResponse response = ParserUtil.downloadFromURL(
							new URL("http://aplicacao.jt.jus.br/cndtCertidao/consultarCertidao.faces"),
							params1, savedCookie);
			
			// Identifica se o site emitiu algum erro de validação
			String texto1 = response.getText("utf-8");
			if (!texto1.contains("Aguarde a emissão da certidão...")) {
				Pattern p1 = Pattern.compile("<div id=\"mensagens\"><ul class=\"erros\"><li>(.*)</li></ul></div>");
				Matcher m1 = p1.matcher(texto1);
				if (m1.find()) {
					throw new ValidationException("Erro na Validação da certidão: " + m1.group(1));
				} else {
					throw new ValidationException("Erro na Validação da certidão");
				}
			}
			
			// Terceira Requisição: Download da certidão pelo site oficial
			URL downloadUrl = new URL("http://aplicacao.jt.jus.br/cndtCertidao/emissaoCertidao?pfnc=" + pfnc);
			Certidao certidao2 = ParserUtil.downloadCertidao(downloadUrl, null, savedCookie, this);
			certidao2.assertEquals(certidao);

		} catch (IOException e) {
			throw e;
		}

	}

}
