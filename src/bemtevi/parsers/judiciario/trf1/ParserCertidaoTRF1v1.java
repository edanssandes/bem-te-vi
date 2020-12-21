package bemtevi.parsers.judiciario.trf1;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoVerificado;
import bemtevi.model.campos.CampoVerificado.Status;
import bemtevi.parsers.AbstractParserConfig;
import bemtevi.parsers.AbstractParserDialog;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;



public class ParserCertidaoTRF1v1 implements IParserCertidao, IValidadorCertidao, IParserConfigurable<ParserConfigTRF1> {
	private static final String NOME_CERTIDAO = "Certidão Cíveis e Criminais - TRF";
	private static final Map<String,String> orgaoAlias = new HashMap<String,String>();
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	static {
		// Correção de bugs encontrados no site do TRF-1
		orgaoAlias.put("Tribunal Regional Federal da 1ª Região", "Tribunal Regional Federal da 1º Região");
		orgaoAlias.put("Seção Judiciária do Estado do Piauí", "Seção Judiciária do Estado do Piau");		
	}

	private ParserConfigTRF1 config;	

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("PODER JUDICIÁRIO");
		pattern.searchNext("CERTIDÃO DE DISTRIBUIÇÃO PARA FINS GERAIS");
		pattern.searchNext("CERTIFICAMOS, após pesquisa nos registros eletrônicos de distribuição");
		pattern.search("de ações e execuções (.*?) mantidos n[oa] (.*?), que");
		pattern.search("NADA CONSTA", ParserUtilPattern.FILL_SPACES);
		pattern.search("contra (.*?)");
		pattern.search("nem contra o (?:CPF|CNPJ): (" + ParserUtil.REGEX_CPF_OU_CNPJ + ")\\.");
		pattern.searchFirst("(?:N[\"°ºo]|.*N[ºo])\\s*(\\d+)");
		pattern.searchFirst("(?:Certidão\\s?Emitida\\s?em:|Emitida\\s?gratuitamente\\s?pela\\s?internet\\s?em:) (\\S+)[, ]*às\\s?(\\d+[:h]\\d+).*");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());
		
		if (matcher.find()) {
			String tipoCertidao = matcher.nextGroup();
			String orgaoEmissor = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup().replace('h', ':');

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			if (config == null || !config.isSelecionarOrgaosValidos()) {
				nadaConsta.setOrgaoEmissao(new CampoVerificado(orgaoEmissor));
			} else {
				Status orgaoEmissonstatus;
				if (config.getOrgaosValidos().contains(orgaoEmissor)) {
					orgaoEmissonstatus = Status.OK;
				} else {
					orgaoEmissonstatus = Status.PROBLEMA;
				}
				nadaConsta.setOrgaoEmissao(new CampoVerificado(orgaoEmissor, orgaoEmissonstatus));
			}
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			if (config == null || !config.isSelecionarTiposValidos()) {
				nadaConsta.setTipoCertidao(new CampoVerificado(tipoCertidao));
			} else {
				Status tipoCertidaoStaus;
				if (config.getTiposValidos().contains(tipoCertidao)) {
					tipoCertidaoStaus = Status.OK;
				} else {
					tipoCertidaoStaus = Status.PROBLEMA;
				}
				nadaConsta.setTipoCertidao(new CampoVerificado(tipoCertidao, tipoCertidaoStaus));
			}			
			
			try {
				//"https://portal.trf1.jus.br/Servicos/Certidao/trf1_autenticacertidao.php?orgao=MA&nomeAut=ADELAIDE FERREIRA DA SILVA&cpf=24675504353&nr=60664"
				String orgaoId = ConstantesTRF1.getIdFromOrgao(orgaoEmissor);
				nadaConsta.setLinkValidacao(new URI("https", "portal.trf1.jus.br",
								"/Servicos/Certidao/trf1_autenticacertidao.php",
								"orgao="+orgaoId+"&nomeAut=" + nomeCompleto
								+ "&cpf=" + cpfCnpj
								+ "&nr=" + codigoAutenticacao,
								null).toASCIIString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				// Ignora
			}
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		try {
			URL url = new URL(certidao.getLinkValidacao());
			WebResponse response = ParserUtil.downloadFromURL(url, null, null);
			String text = ParserUtil.joinLines(response.getTextUtf8());
			
			if (text.contains("os dados informados n&atilde;o conferem com os da certid&atilde;o emitida")) {
				throw new ValidationException("Certidão Inválida: Dados não conferem"); 
			}
			String[] expectedText = new String[] {
					"Certid&atilde;o n&uacute;mero "+certidao.getCodigoAutenticacao()+" emitida via ",
					"&agrave;s " +certidao.getDataEmissao().getHora() + " horas do dia "+certidao.getDataEmissao().getDia(),
					"N A D A&nbsp;&nbsp;C O N S T A&nbsp;&nbsp;",
					"em nome de " + certidao.getNome() + ",",
					": " + certidao.getCpfCnpj(),
					//certidao.getOrgaoEmissao() + "</span>",
			};
			List<String> divergencias = new ArrayList<String>();
			for (String string : expectedText) {
				if (!text.toLowerCase().contains(string.toLowerCase())) {
					divergencias.add(string);
				}
			}
			String orgao = certidao.getOrgaoEmissao().toString();
			
			if (!text.toLowerCase().contains(orgao.toLowerCase() + "</span>")) {
				String orgao2 = orgaoAlias.getOrDefault(orgao, orgao);
				if (!text.toLowerCase().contains(orgao2.toLowerCase())) {
					divergencias.add(orgao);
				}
			}
			if (divergencias.size() > 0) {
				throw new ValidationException("Validação Divergente: Valores esperados: " + ParserUtil.join(";", divergencias));
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public ParserConfigDialogTRF1 getConfigDialog() {
		return null;
	}

	public void setConfig(ParserConfigTRF1 config) {
		this.config = config;
	}

	public ParserConfigTRF1 createConfig() {
		return null;
	}
	
}
