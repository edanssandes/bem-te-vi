package bemtevi.parsers.judiciario.tjdft;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.model.campos.CampoSituacao;
import bemtevi.model.campos.CampoVerificado;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;


public class ParserCertidaoTJDFT implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão de Distribuição - TJDFT";
	private String savedCaptcha = null;
	private String savedCookie = null;
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}

	public Certidao parse(Documento doc) {
		
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("CERTIFICAMOS que, após consulta aos registros eletrônicos de distribuição de");
		pattern.search("ações (?:de )?(.*?) disponíveis até (\\d+/\\d+/\\d+), (.*?CONSTA) contra o nome por extenso e");
		pattern.search("CPF/CNPJ de:\\s*(.*)\\s*");
		pattern.search("(" + ParserUtil.REGEX_CPF_OU_CNPJ + ")");
		pattern.search("(?:\\(\\s*(.*?)\\s*\\))?");
		pattern.search("(.*)");
		pattern.searchFirst("Emitida gratuitamente pela internet em: (\\S+)\\s*");
		pattern.searchFirst("V[AÁ]LIDA POR (\\d+)\\s*\\S*\\s*?DIAS");
		pattern.searchFirst(".*?Selo digital de seguran[çc]a: (\\S+)\\s*");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());		
		
		if (matcher.find()) {
			String acoes = matcher.nextGroup();
			String dataAtualizacao = matcher.nextGroup();
			String statusTexto = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String filiacao = matcher.nextGroup();
			String corpo = matcher.nextGroup();
			CampoData dataEmissao = new CampoData(matcher.nextGroup());
			int validadeDias = Integer.parseInt(matcher.nextGroup());
			String codigoAutenticacao = matcher.nextGroup();
			
			CampoDataValidade dataValidade = new CampoDataValidade(dataEmissao.addDate(validadeDias));


			CampoSituacao situacao = null;
			if (statusTexto.contains("NADA CONSTA")) {
				situacao = CampoSituacao.NADA_CONSTA;
			} else if (statusTexto.contains("CONSTA")) {
				Matcher matcher_constas = Pattern.compile(".*?(- .*\\.\\s*)(?:OBSERVA[CÇ][OÕ]ES:|Selo digital)").matcher(corpo);
				if (matcher_constas.find()) {
					String constas = matcher_constas.group(1);
					constas = constas.replaceAll("\\. -", ".\n-");
		        	situacao = new CampoSituacao(CampoSituacao.CONSTA, constas);
				}
			} else {
				situacao = null;
			}			
			
			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			nadaConsta.setSituacao(situacao);
			nadaConsta.setDataValidade(dataValidade);
			nadaConsta.setDataEmissao(dataEmissao);
			nadaConsta.setTipoCertidao(new CampoVerificado(acoes));

			nadaConsta.setLinkValidacao("http://procart.tjdft.jus.br/sistjinternet/sistj?visaoId=tjdf.sistj.internet.certidao.apresentacao.VisaoValidacaoCertidaoInternet");
			nadaConsta.setFiliacao(filiacao);
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		try {
			String captcha = null;
			String auth = certidao.getCodigoAutenticacao();
			String texto = "";
			int tentativa = 0;
			while (captcha == null && tentativa < 5) {
				tentativa++;
				Image image = null;
				if (savedCaptcha == null || savedCookie == null) {
					WebResponse response3 = ParserUtil.downloadFromURL(new URL("https://procart.tjdft.jus.br/sistjinternet/jcaptcha.jpg"), null, savedCookie);
					savedCookie = response3.getCookie();
					savedCaptcha = null;
					image = response3.getImage();
				}
				if (savedCaptcha != null) {
					captcha = savedCaptcha;
				} else {
					captcha = ParserUtil.askCaptcha(image);
				}
			
				String params0 = "visaoId=tjdf.sistj.internet.certidao.apresentacao.VisaoValidacaoCertidaoInternet&" +
						"comando=abrirValidacao&" +
						"numeroDaCertidao="+auth+"&" +
						"codigoDeSeguranca=" + captcha;
				
				WebResponse response2 = ParserUtil.downloadFromURL(new URL("https://procart.tjdft.jus.br/sistjinternet/sistj"), params0, savedCookie);
				texto = response2.getText();
				System.out.println(texto);
				
				if (texto.contains("title=\"Código de segurança inválido\"")) {
					captcha = null;
					savedCaptcha = null;
					JOptionPane.showMessageDialog(null, "Erro de validação do Captcha. Favor digitar novamente.");
				} else if (texto.contains("<p><b><i>ATENÇÃO:</i></b> O número informado é inválido.</p>")) {
					throw new ValidationException("O código de segurança é inválido");
				} else {
					savedCaptcha = captcha;
				}
			}
			if (tentativa >= 5) {
				throw new RuntimeException("Não foi possível validar o captcha");
			}

			Pattern p0 = Pattern.compile("if \\(campoDestino!=null\\) campoDestino\\.value = '(\\d+)';");
			Matcher m0 = p0.matcher(texto);
			String idDaCertidaoParaBaixar = null;
			if (m0.find()) {
				idDaCertidaoParaBaixar = m0.group(1);
			} else {
				JOptionPane.showMessageDialog(null, "Sem idDaCertidaoParaBaixar");
			}
			
			String params1 = "visaoId=tjdf.sistj.internet.certidao.apresentacao.VisaoValidacaoCertidaoInternet&" +
					"comando=downloadDaCertidao&" +
					"numeroDaCertidao="+auth+"&" +
					"codigoDeSeguranca=" + captcha + "&" +
					"idDaCertidaoParaBaixar="+idDaCertidaoParaBaixar;	
			Thread.sleep(100);
			WebResponse response2 = ParserUtil.downloadFromURL(new URL("http://procart.tjdft.jus.br/sistjinternet/sistj"), params1, savedCookie);
			texto = response2.getText();
			
			Pattern p = Pattern.compile("\\(\"src\",\"infra/Download.jsp\\?idd=(\\S+)\"\\)");
			Matcher m = p.matcher(texto);
			if (m.find()) {
				URL downloadUrl = new URL("http://procart.tjdft.jus.br/sistjinternet/infra/Download.jsp?idd=" + m.group(1));
				Certidao certidao2 = ParserUtil.downloadCertidao(downloadUrl, null, savedCookie, this);
				certidao2.assertEquals(certidao);
			}
			
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

}
