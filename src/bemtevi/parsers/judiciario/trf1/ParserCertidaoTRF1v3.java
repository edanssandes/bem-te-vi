package bemtevi.parsers.judiciario.trf1;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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



public class ParserCertidaoTRF1v3 implements IParserCertidao, IParserConfigurable<ParserConfigTRF1> {
	private static final String NOME_CERTIDAO = "Certidão Cíveis e Criminais - TRF";
	private static final Map<String,String> orgao = new HashMap<String,String>();
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	private static final Map<String,String> tipos = new HashMap<String,String>();
	
	static {

		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO ACRE", "AC");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO AMAZONAS", "AM");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO AMAPÁ", "AP");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DA BAHIA", "BA");
		orgao.put("SEÇÃO JUDICIÁRIA DO DISTRITO FEDERAL", "DF");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DE GOIÁS", "GO");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO MARANHÃO", "MA");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DE MINAS GERAIS", "MG");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DE MATO GROSSO", "MT");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO PARÁ", "PA");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO PIAUÍ", "PI");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DE RONDÔNIA", "RO");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DE RORAIMA", "RR");
		orgao.put("SEÇÃO JUDICIÁRIA DO ESTADO DO TOCANTINS", "TO");
		orgao.put("TRIBUNAL REGIONAL FEDERAL DA 1ª REGIÃO", "TRF1");
		
		tipos.put("CÍVEL", "Cíveis");
		tipos.put("CRIMINAL", "Criminais");
	}

	private ParserConfigTRF1 config;	

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("CERTIDÃO JUDICIAL (.{1,30}?) \\d+\\s*/\\s*\\d+");
		pattern.search("CERTIFICAMOS, na forma da lei, que, consultando os sistemas processuais");
		pattern.searchNext("N[AÃ]O CONSTAM");
		pattern.searchNext("PROCESSOS .{20,40}? contra:\\s+(.*?)");
		pattern.search("(?:CPF|CNPJ):\\s*(" + ParserUtil.REGEX_CPF_OU_CNPJ + ")");
		
		pattern.searchNext("Certid[aã]o:\\s*(\\d+)\\s");
		pattern.searchNext("C[oó]digo de Validação:\\s*([0-9A-F ]+)\\s");
		pattern.searchNext("Data da Atualiza[cç][aã]o: (\\S+),\\s*às\\s*(\\S+)\\s*");
		pattern.searchNext("Certidão válida para o\\(s\\) seguinte\\(s\\) órgão\\(s\\):\\s*(.*?)\\.");
		
		ParserUtilMatcher matcher = pattern.matcher(doc.getTextInOrder());
		
		if (matcher.find()) {
			String tipoCertidao = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup();
			String cpfCnpj = matcher.nextGroup();
			String numeroCertidao = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup().replaceAll("\\s+","");
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String orgaoEmissor = matcher.nextGroup();

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(nomeCompleto);
			nadaConsta.setCpfCnpj(cpfCnpj);
			nadaConsta.setCodigoAutenticacao(numeroCertidao + "/" + codigoAutenticacao);
			if (config == null || !config.isSelecionarOrgaosValidos()) {
				nadaConsta.setOrgaoEmissao(new CampoVerificado(orgaoEmissor));
			} else {
				Status orgaoEmissonstatus;
				if (config.getOrgaosValidos().contains(ConstantesTRF1.getOrgaoFromId(orgao.get(orgaoEmissor)))) {
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
				if (config.getTiposValidos().contains(tipos.get(tipoCertidao))) {
					tipoCertidaoStaus = Status.OK;
				} else {
					tipoCertidaoStaus = Status.PROBLEMA;
				}
				nadaConsta.setTipoCertidao(new CampoVerificado(tipoCertidao, tipoCertidaoStaus));
			}			
			nadaConsta.setLinkValidacao("https://sistemas.trf1.jus.br/certidao//#/certidao"
					+ "?id=" + numeroCertidao + "&codigo="+codigoAutenticacao);
			return (nadaConsta);
		} else {
			return null;
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
