package bemtevi.parsers.judiciario.trt10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoSituacao;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;


public class ParserCertidaoTRT10v1 implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão de Distribuição de Ações Trabalhistas - TRT 10º Região";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	// Padrão especial de CPF/CNPJ para o TRT10, pois o site não obriga que o CPF/CNPJ seja informado no formato correto.
	private static final String TRT10_REGEX_CPF_CNPJ = "[\\d/.-]{9,18}";
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("Tribunal Regional do Trabalho 10[ªa] Região");
		pattern.search("CERTIDÃO DE DISTRIBUIÇÃO");
		pattern.search("AÇÕES TRABALHISTAS");
		pattern.search("Nome: (.*?)");
		pattern.search("(?:CPF|CNPJ): (" + TRT10_REGEX_CPF_CNPJ + ")");
		pattern.searchNext(", até a presente data, (.*?)\\.");
		pattern.search("(.*?)");
		pattern.search("A pesquisa foi realizada");
		pattern.searchNext("Certidão emitida em:\\s*(\\S+)\\s*-\\s*(\\d+:\\d+:\\d+)");
		pattern.searchNext("Para verificar a autenticidade da certidão, informe o número de controle: (\\S*) - (\\S*) na");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());		
		
		
		/*String documentText = _documentText;
		//documentText = ParserUtil.normalize(documentText);
		documentText = ParserUtil.joinLines(documentText);
		Pattern pattern = Pattern
				.compile(
						"Poder Judiciário\\s*"
								+ "Tribunal Regional do Trabalho 10[ªa] Região\\s*"
								+ "CERTIDÃO DE DISTRIBUIÇÃO\\s*"
								+ "AÇÕES TRABALHISTAS\\s*"
								+ "Nome: (.*?)\\s*"
								+ "(?:CPF|CNPJ): (" + ParserUtil.REGEX_CPF_OU_CNPJ + ")\\s*"
								+ ".*?, até a presente data, (.*?)\\."
								+ "\\s*(.*?)\\s*A pesquisa foi realizada.*"
								+ "Certidão emitida em:\\s*(\\S+)\\s*-\\s*(\\d+:\\d+:\\d+).*"
								+ "Para verificar a autenticidade da certidão, informe o número de controle: (\\S*) - (\\S*) na",
						Pattern.DOTALL);
		
		Matcher matcher = pattern.matcher(documentText);*/

		if (matcher.find()) {
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String statusTexto = matcher.nextGroup();
			String constas = matcher.nextGroup();
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String codigoAutenticacao1 = matcher.nextGroup();
			String codigoAutenticacao2 = matcher.nextGroup();
			String codigoAutenticacao = codigoAutenticacao1 + " - "
					+ codigoAutenticacao2;
			
			CampoSituacao situacao;
			if (statusTexto.contains("não existem processos em tramitação / NADA CONSTA em desfavor de")) {
				situacao = CampoSituacao.NADA_CONSTA;
			} else if (statusTexto.contains("consta(m) em tramitação(s) o(s) seguinte(s) processo(s) contra")) {
				constas = constas.replaceAll("\\s*([^\\d:()]{10,}:)\\s*", "\n$1\n");
				constas = constas.replaceAll("([\\d.-]{15,}(?:\\(\\S+\\))?),?\\s*", "$1\n");
				if (constas.startsWith("\n")) {
					constas = constas.substring(1);
				}
	        	situacao = new CampoSituacao(CampoSituacao.CONSTA, constas);
			} else {
				situacao = null;
			}

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);			
			nadaConsta.setSituacao(situacao);
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			//nadaConsta.setInfo(CODIGO_1, codigoAutenticacao1);
			//nadaConsta.setInfo(CODIGO_2, codigoAutenticacao2);

			nadaConsta.setLinkValidacao(getLinkValidacao(codigoAutenticacao1, codigoAutenticacao2));
			return (nadaConsta);
		} else {
			return null;
		}

	}

	private String getLinkValidacao(String codaut, String autenticacao) {
		return "https://autenticador.trt10.jus.br/?modulo=&sessao_valida=&codaut="
				+ codaut
				+ "&autenticacao="
				+ autenticacao;
	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		//if (1==1) throw new ValidationException("Em Implementação");
		//if (1==1) return false;
		
		URL url;
		try {
			url = new URL(certidao.getLinkValidacao());
			URLConnection con = url.openConnection();
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			InputStream is = con.getInputStream();			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					is, "ISO-8859-1"));
			StringBuilder sb = new StringBuilder();
			String inputLine;

			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);

			String response = sb.toString();
			response = ParserUtil.simplifyText(response);
			in.close();
			
			//String response = "<title>Autenticador - TRT 10ª Região</title><script language=\"JavaScript\" src='/js/funcoes.js' ><!--// --></script><script language=\"JavaScript\" src='/js/forms.js' ><!--// --></script><link rel='stylesheet' href='/css/autenticador.css' type='text/css'  /><link rel='stylesheet' href='/css/nao_imprimir.css' type='text/css' media=print /><div id=\"div_cabecalho_impressao\" name=\"div_cabecalho_impressao\"  style=\"width: 100%\"  class=\"nao_imprimir\" ><form name=barra_impressao  action=/ method=post id=barra_impressao ><input type=hidden name=PHPSESSID value=\"7eip19mnin2jf43h1a24j1ck31\" ><input type=hidden name=modulo value=\"\" ><input type=hidden name=modulo_anterior id=\"modulo_anterior\" value=\"\"><input type=hidden name=sessao_valida id=\"sessao_valida\" value=\"\"><input type=hidden name=mbref id=\"mbref\" value=\"\"><input type=hidden name=mbref_anterior id=\"mbref_anterior\" value=\"\"><input type=hidden name=sessao_valida id=\"sessao_valida\" value=\"\"><button type=button  Onclick=\"parent.window.print()\" class=botao >Imprimir</button><button type=button  Onclick=\"document.barra_impressao.submit();\" class=botao >Nova Consulta</button></form></div><div id=\"div_conteudo_impressao\" name=\"div_conteudo_impressao\" ><div id=\"documento_autenticado\" name=\"documento_autenticado\"  class=\"hr\" >DOCUMENTO AUTENTICADO em 27/03/2017 11:04:52 [6DRooTLqM1VfAgpqLt]</div> <table width='700' border='0' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>  <tr>    <td colspan='3' align='center'><br><br><strong style='font-family: Arial, Helvetica, sans-serif;            font-size: 18px;'>***CERTIDÃO VÁLIDA*** </strong><br><br></td>  </tr>  <tr>    <td colspan='3'>&nbsp;</td>  </tr>  <tr>    <td colspan='3' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>O TRT da 10ª Região garante apenas a verifica&ccedil;&atilde;o de autenticidade realizada diretamente <strong>pela autoridade recebedora competente</strong>, na p&aacute;gina www.trt10.jus.br, n&atilde;o sendo v&aacute;lidas verifica&ccedil;&otilde;es de autenticidade meramente impressas. </td>  </tr>  <tr>    <td width='102'>&nbsp;</td>    <td width='185'>&nbsp;</td>    <td width='399'>&nbsp;</td>  </tr>  <tr>    <td>&nbsp;</td>    <td>&nbsp;</td>    <td>&nbsp;</td>  </tr>  <tr>    <td>&nbsp;</td>    <td width='185' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>N&uacute;mero de Controle: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>439 - 6DRooTLqM1VfAgpqLt</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Nome:</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>EDANS FLAVIUS DE OLIVEIRA SANDES</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>CPF</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>001.312.061-17</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Data de Emiss&atilde;o: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>26/03/2017 - 19:13:37</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Tipo de Certid&atilde;o: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>Certidão de Distribuição de Ações Trabalhistas</b></td>  </tr></table></div>";
			
			System.out.println(response);
			
			if (response.matches("(?d)<div id=\"div_conteudo_impressao\" name=\"div_conteudo_impressao\" >.*?Certidão não encontrada</div>")) {
				throw new ValidationException("Certidão Inválida"); 
			} else {
				Pattern p1 = Pattern.compile("(?d).*\\*\\*\\*CERTIDÃO VÁLIDA\\*\\*\\* </strong>" + ".*?"
			
							+ "<b>" + certidao.getCodigoAutenticacao() + "</b>.*?"
							+ "<b>" + certidao.getNome() + "</b>.*?"
							+ "<b>(" + TRT10_REGEX_CPF_CNPJ + ")</b>.*?"
							+ "<b>" + certidao.getDataEmissao().getDia() + ".*?</b>.*?"
							+ "Certidão de Distribuição de Ações Trabalhistas.*");
				Matcher m1 = p1.matcher(response);
				if (!m1.find()) {
					throw new ValidationException("Validação Divergente");
				}
				String response_cpfCnpj = m1.group(1).replaceAll("\\D", "");
				String certidao_cpfCnpj = certidao.getCpfCnpj().replaceAll("\\D", "");
				
				if (!response_cpfCnpj.equals(certidao_cpfCnpj)) {
					throw new ValidationException("Validação Divergente (Campo CPF/CNPJ)");
				}
			}

		} catch (IOException e) {
			throw e;
		}
	}

}
