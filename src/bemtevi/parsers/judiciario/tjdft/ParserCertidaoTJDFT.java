package bemtevi.parsers.judiciario.tjdft;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

			nadaConsta.setLinkValidacao("https://cnc.tjdft.jus.br/consulta-externa");
			//nadaConsta.setLinkValidacao("http://procart.tjdft.jus.br/sistjinternet/sistj?visaoId=tjdf.sistj.internet.certidao.apresentacao.VisaoValidacaoCertidaoInternet");
			nadaConsta.setFiliacao(filiacao);
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		String auth = certidao.getCodigoAutenticacao();
		auth = auth.replace(".", "");
		WebResponse response = ParserUtil.downloadFromURL(new URL("https://cnc-api.tjdft.jus.br/certidoes/valida_externo?codigo=" + auth), null, null);
		String json = response.getText();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(json);
		if (jsonNode.size() == 0) {
			throw new ValidationException("Código de Autenticação não foi encontrado: " + auth);
		}
		System.out.println(jsonNode.size());
		String cpf_cnpj = jsonNode.get(0).get("cpf_cnpj").asText();
		String nome = jsonNode.get(0).get("nome").asText();
		String codigo = jsonNode.get(0).get("codigo").asText();
		String data_emissao = jsonNode.get(0).get("data_emissao").asText();
		String data_validade = jsonNode.get(0).get("data_validade").asText();
		String url_certidao = jsonNode.get(0).get("url_certidao").asText();
		String situacao = jsonNode.get(0).get("situacao").asText();
		
		if (!cpf_cnpj.equals(certidao.getCpfCnpj().replaceAll("[/\\.-]", ""))) {
			throw new ValidationException("CPF/CNPJ não confere: " + cpf_cnpj);
		}
		if (!nome.trim().equalsIgnoreCase(certidao.getNome().trim())) {
			throw new ValidationException("CPF/CNPJ não confere: " + cpf_cnpj);
		}
		if (!codigo.equals(auth)) {
			throw new ValidationException("Código de autenticação não confere: " + codigo);
		}
		if (!data_emissao.substring(0, 10).equals(certidao.getDataEmissao().getDiaISO8601().substring(0, 10))) {
			throw new ValidationException("Data de Emissao não confere: " + data_emissao);
		}
		if (!data_validade.substring(0, 10).equals(certidao.getDataValidade().getDiaISO8601().substring(0, 10))) {
			throw new ValidationException("Data de Emissao não confere: " + data_emissao);
		}
		if (!situacao.equals("finalizada")) {
			throw new ValidationException("Situação da certidão ainda não foi finalizada");
		}
			
		URL downloadUrl = new URL(url_certidao);
		Certidao certidao2 = ParserUtil.downloadCertidao(downloadUrl, null, null, this);
		certidao2.assertEquals(certidao);
	}
}
