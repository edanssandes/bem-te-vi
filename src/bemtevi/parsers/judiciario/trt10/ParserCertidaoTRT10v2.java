package bemtevi.parsers.judiciario.trt10;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

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


public class ParserCertidaoTRT10v2 implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certidão de Distribuição de Ações Trabalhistas - TRT 10º Região";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	// Padrão especial de CPF/CNPJ para o TRT10, pois o site não obriga que o CPF/CNPJ seja informado no formato correto.
	//private static final String TRT10_REGEX_CPF_CNPJ = "[\\d/.-]{9,18}";
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("TRIBUNAL REGIONAL DO TRABALHO 10[ªa] REGIÃO");
		pattern.search("CERTIDÃO DE AÇÕES TRABALHISTAS EM TRAMITAÇÃO - TRT 10[ªa] REGIÃO");
		pattern.search("Dados Pesquisados:");
		pattern.search("NOME: (.*?)");
		pattern.search("CPF/CNPJ: (" + ParserUtil.REGEX_CPF_OU_CNPJ + ")");
		pattern.search("Expedição:\\s*(\\d+/\\d+/\\d+)\\s*-\\s*(\\d+:\\d+:\\d+)");
		pattern.search("Código de Autenticidade:\\s*(\\S+)");
		pattern.search("Válida até\\s*(\\d+/\\d+/\\d+)");
		
		pattern.searchNext(", até a presente data, (.*?)\\.");
		pattern.search("(.*?)");
		pattern.search("OBSERVAÇÕES: 1\\) A pesquisa foi realizada");
		
		ParserUtilMatcher matcher = pattern.matcher(doc.getTextInOrder());		

		if (matcher.find(true)) {
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			String dataValidade = matcher.nextGroup();
			String statusTexto = matcher.nextGroup();
			String constas = matcher.nextGroup();
			
			CampoSituacao situacao;
			if (statusTexto.contains("NÃO CONSTA ação trabalhista em tramitação")) {
				situacao = CampoSituacao.NADA_CONSTA;
			} else if (statusTexto.contains("CONSTA(M) a(s) seguinte(s) ação(ões) trabalhista(s) em tramitação")) {
				constas = constas.replaceAll("([\\d.-]{15,}(?:\\(\\S+\\))?),?\\s*", "$1\n");
				if (constas.startsWith("\n")) {
					constas = constas.substring(1);
				}
				String[] processos = constas.split("\n");
				Arrays.sort(processos, new Comparator<String>(){
					public int compare(String arg0, String arg1) {
						int l0 = arg0.length();
						int l1 = arg1.length();
						if (l0 != l1) {
							return l0 - l1;
						}
						String s0 = arg0.substring(11, 16) + arg0.substring(0, 11) + arg0.substring(16);
						String s1 = arg1.substring(11, 16) + arg1.substring(0, 11) + arg1.substring(16);
						return s0.compareTo(s1);
					}
				});
				StringBuilder sb = new StringBuilder();
				for (String processo : processos) {
					sb.append(processo + "\n");
				}
	        	situacao = new CampoSituacao(CampoSituacao.CONSTA, sb.toString());
			} else {
				situacao = null;
			}

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);			
			nadaConsta.setSituacao(situacao);
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidade));
			//nadaConsta.setInfo(CODIGO_1, codigoAutenticacao1);
			//nadaConsta.setInfo(CODIGO_2, codigoAutenticacao2);

			nadaConsta.setLinkValidacao("https://www.trt10.jus.br/certidao_online/ServletCertidaoOnline?codigo=" + codigoAutenticacao);
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		URL downloadUrl = new URL(certidao.getLinkValidacao());
		WebResponse res = ParserUtil.downloadFromURL(downloadUrl, null, null);
		Certidao certidao2 = ParserUtil.downloadCertidao(downloadUrl, null, res.getCookie(), this);
		certidao2.assertEquals(certidao, null);
	}

}
