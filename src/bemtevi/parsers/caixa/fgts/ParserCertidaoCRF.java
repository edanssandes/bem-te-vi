package bemtevi.parsers.caixa.fgts;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bemtevi.model.Certidao;
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


public class ParserCertidaoCRF implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certificado de Regularidade do FGTS";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	private static final String HOSTNAME = "https://consulta-crf.caixa.gov.br";

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("Certificado de Regularidade do FGTS", ParserUtilPattern.FILL_SPACES);
		pattern.searchFirst("CRF");
		pattern.search("Inscrição:\\s*([0-9/-]+)");
		pattern.search("Razão Social:\\s*(.*?)");
		pattern.search("(?:Nome\\s*Fantasia:\\s*(.*?)\\s*)?");
		pattern.search("Endereço:\\s*(.*?)");
		pattern.search("A Caixa Econômica Federal, no uso da");
		pattern.searchNext("encontra-se em situação regular perante o Fundo de Garantia");
		pattern.searchNext("Validade:\\s*(\\S*)\\s*a\\s*(\\S*)");
		pattern.search("[Certificação ]* [Número ]*: (\\d{22})");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());			

		if (matcher.find(true)) {
			String cnpj = matcher.nextGroup();
			String razaoSocial = matcher.nextGroup();
			String nomeFantasia = matcher.nextGroup();
			String endereco = matcher.nextGroup();
			String dataValidadeInicio = matcher.nextGroup();
			String dataValidadeFim = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			// String dataEmissao = matcher.group(8);
			// String horaEmissao = matcher.group(9);

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(razaoSocial);
			nadaConsta.setCpfCnpj(cnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidadeFim));
			nadaConsta.setDataEmissao(new CampoData(dataValidadeInicio));
			nadaConsta.setNomeFantasia(nomeFantasia);
			nadaConsta.setEndereco(endereco);
			nadaConsta
					.setLinkValidacao(HOSTNAME + "/Cidadao/Crf/FgeCfSCriteriosPesquisa.asp");
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		try {
			WebResponse response = ParserUtil
					.downloadFromURL(
							new URL(HOSTNAME + "/Cidadao/Crf/FgeCfSCriteriosPesquisa.asp"),
							null, null);
			String texto = response.getText();
			String cookie = response.getCookie();

			System.out.println(texto);
			System.out.println(cookie);

			Pattern p0 = Pattern
					.compile("id=\"resultadopath\\d*\"\\s+value=\"(\\d{4})-(.*?)\"");
			Matcher m0 = p0.matcher(texto);
			String captcha = "";
			while (m0.find()) {
				int i = (Integer.parseInt(m0.group(1)));
				char c = (char) ((i - 5047) + '0');
				System.out.println(c);
				captcha += c;
			}
			System.out.println(captcha);
			String cnpj = certidao.getCpfCnpj().replaceAll("\\.", "");
			// 19161094%2F0001-09;

			WebResponse response2 = ParserUtil
					.downloadFromURL(
							new URL(HOSTNAME + "/Cidadao/Crf/CheckCaptcha.asp?txtCaptchaVerificar="
											+ captcha), null, cookie);
			texto = response2.getText();
			System.out.println(texto);

			String param = "ImportWorkEmpregadorTipoInscricao=1&"
					+ "tipoinscricao=1&"
					+ "ImportWorkEmpregadorCodigoInscricaoAlfanum=" + cnpj
					+ "&" + "ImportEstadoSigla=&" + "txtConsulta=" + captcha;

			WebResponse response3 = ParserUtil
					.downloadFromURL(
							new URL(HOSTNAME + "/Cidadao/Crf/Crf/FgeCfSConsultaRegularidade.asp"),
							param, cookie);
			texto = response3.getText();
			System.out.println(texto);

			Pattern p1 = Pattern
					.compile("<input type=hidden name=\"(\\S+)\" value=\"(.*?)\">");
			Matcher m1 = p1.matcher(texto);
			Map<String, String> fields = new LinkedHashMap<String, String>();
			while (m1.find()) {
				fields.put(m1.group(1), m1.group(2));
			}
			System.out.println(fields);
			String varPessoa = fields.get("VARPessoa");
			String varPessoaMatriz = fields.get("VARPessoaMatriz");

			String auth = certidao.getCodigoAutenticacao();
			String param4 = "VARPessoaMatriz=" + varPessoaMatriz
					+ "&VARPessoa=" + varPessoa;
			WebResponse response4 = ParserUtil
					.downloadFromURL(
							new URL(HOSTNAME + "/Cidadao/Crf/Crf/FgeCfSHistoricoStatusRegul.asp"),
							param4, cookie);
			texto = response4.getText();
			System.out.println(texto);

			Pattern p2 = Pattern
					.compile("<tr[^>]*?><TD[^>]*?>(\\S+)</TD><TD[^>]*?>\\S+\\s+a\\s+(\\S+)</TD>\\s*<TD[^>]*?>&nbsp;"
							+ auth + "</TD>\\s*</tr>");
			Matcher m2 = p2.matcher(texto);

			if (!m2.find()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -24);
				Date date24MesesAtras = cal.getTime();
				String mensagem = "Número de CRF não encontrado: "	+ auth;
				if (certidao.getDataEmissao().compareTo(date24MesesAtras) < 0) {
					mensagem += ". Somente é possível validar CRFs concedidas nos últimos 24 meses."; 
				}
				throw new ValidationException(mensagem);
			} else {
				String dataEmissao = m2.group(1);
				String dataValidade = m2.group(2);
				if (!certidao.getDataEmissao().getDia().equals(dataEmissao)) {
					throw new ValidationException("Data de Emissão divergente");
				} else if (!certidao.getDataValidade().getDia()
						.equals(dataValidade)) {
					throw new ValidationException("Data de Validade divergente");
				}
			}
		} catch (IOException e) {
			throw e;
		}
	}

}
