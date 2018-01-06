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



public class ParserCertidaoTRF1 implements IParserCertidao, IValidadorCertidao, IParserConfigurable<ParserConfigTRF1> {
	private static final String NOME_CERTIDAO = "Certidão Cíveis e Criminais - TRF";
	private static final Map<String,String> orgao = new HashMap<String,String>();
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	private static final List<String> tipos = new ArrayList<String>();
	
	static {
		orgao.put("Tribunal Regional Federal da 1ª Região","TRF1");
		orgao.put("Seção Judiciária do Estado do Acre","AC");
		orgao.put("Subseção Judiciária de Cruzeiro do Sul","CZU");
		orgao.put("Seção Judiciária do Estado do Amapá","AP");
		orgao.put("Subseção Judiciária de Laranjal do Jari ","LJI"); //(AP)
		orgao.put("Subseção Judiciária de Oiapoque ","OPQ"); //(AP)
		orgao.put("Seção Judiciária do Estado do Amazonas","AM");
		orgao.put("Subseção Judiciária de Tabatinga ","TB"); //(AM)
		orgao.put("Subseção Judiciária de Tefé ","TFE"); //(AM)
		orgao.put("Seção Judiciária do Estado da Bahia","BA");
		orgao.put("Subseção Judiciária de Alagoinhas ","ALH"); //(BA)
		orgao.put("Subseção Judiciária de Barreiras ","BES"); //(BA)
		orgao.put("Subseção Judiciária de Bom Jesus da Lapa ","BMP"); //(BA)
		orgao.put("Subseção Judiciária de Campo Formoso ","CFS"); //(BA)
		orgao.put("Subseção Judiciária de Eunápolis ","EUS"); //(BA)
		orgao.put("Subseção Judiciária de Feira de Santana ","FSA"); //(BA)
		orgao.put("Subseção Judiciária de Guanambi ","GNB"); //(BA)
		orgao.put("Subseção Judiciária de Ilhéus ","IL"); //(BA)
		orgao.put("Subseção Judiciária de Irecê ","IEE"); //(BA)
		orgao.put("Subseção Judiciária de Itabuna ","ITB"); //(BA)
		orgao.put("Subseção Judiciária de Jequié ","JEE"); //(BA)
		orgao.put("Subseção Judiciária de Juazeiro ","JZR"); //(BA)
		orgao.put("Subseção Judiciária de Paulo Afonso ","PAF"); //(BA)
		orgao.put("Subseção Judiciária de Teixeira de Freitas ","TAF"); //(BA)
		orgao.put("Subseção Judiciária de Vitória da Conquista ","VCA"); //(BA)
		orgao.put("Seção Judiciária do Distrito Federal","DF");
		orgao.put("Seção Judiciária do Estado de Goiás","GO");
		orgao.put("Subseção Judiciária de Anápolis","ANS"); //(GO)
		orgao.put("Subseção Judiciária de Aparecida de Goiânia","ACG"); //(GO)
		orgao.put("Subseção Judiciária de Formosa","FRM"); //(GO)
		orgao.put("Subseção Judiciária de Itumbiara","IUB"); //(GO)
		orgao.put("Subseção Judiciária de Jataí","JTI"); //(GO)
		orgao.put("Subseção Judiciária de Luziania","LZA"); //(GO)
		orgao.put("Subseção Judiciária de Rio Verde","RVD"); //(GO)
		orgao.put("Subseção Judiciária de Uruaçu","URC"); //(GO)
		orgao.put("Seção Judiciária do Estado de Mato Grosso","MT");
		orgao.put("Subseção Judiciária de Barra do Garças ","BAG"); //(MT)
		orgao.put("Subseção Judiciária de Cáceres ","CCS"); //(MT)
		orgao.put("Subseção Judiciária de Diamantino ","DIO"); //(MT)
		orgao.put("Subseção Judiciária de Juína ","JNA"); //(MT)
		orgao.put("Subseção Judiciária de Rondonópolis ","ROI"); //(MT)
		orgao.put("Subseção Judiciária de Sinop ","SNO"); //(MT)
		orgao.put("Seção Judiciária do Estado do Maranhão","MA");
		orgao.put("Subseção Judiciária de Bacabal ","BBL"); //(MA)
		orgao.put("Subseção Judiciária de Balsas ","BLA"); //(MA)
		orgao.put("Subseção Judiciária de Caxias ","CXS"); //(MA)
		orgao.put("Subseção Judiciária de Imperatriz ","IM"); //(MA)
		orgao.put("Seção Judiciária do Estado de Minas Gerais","MG");
		orgao.put("Subseção Judiciária de Contagem","CEM"); //(MG)
		orgao.put("Subseção Judiciária de Divinópolis","DVL"); //(MG)
		orgao.put("Subseção Judiciária de Governador Valadares","GVS"); //(MG)
		orgao.put("Subseção Judiciária de Ipatinga","IIG"); //(MG)
		orgao.put("Subseção Judiciária de Ituiutaba","IUA"); //(MG)
		orgao.put("Subseção Judiciária de Janaúba","JUA"); //(MG)
		orgao.put("Subseção Judiciária de Juiz de Fora ","JF"); //(MG)
		orgao.put("Subseção Judiciária de Lavras","LAV"); //(MG)
		orgao.put("Subseção Judiciária de Manhuaçu","MNC"); //(MG)
		orgao.put("Subseção Judiciária de Montes Claros ","MCL"); //(MG)
		orgao.put("Subseção Judiciária de Muriaé","MRE"); //(MG)
		orgao.put("Subseção Judiciária de Paracatu","PTU"); //(MG)
		orgao.put("Subseção Judiciária de Passos ","PSS"); //(MG)
		orgao.put("Subseção Judiciária de Patos de Minas ","PMS"); //(MG)
		orgao.put("Subseção Judiciária de Poços de Caldas","PCS"); //(MG)
		orgao.put("Subseção Judiciária de Ponte Nova","PNV"); //(MG)
		orgao.put("Subseção Judiciária de Pouso Alegre ","PSA"); //(MG)
		orgao.put("Subseção Judiciária de São João Del Rei","SOE"); //(MG)
		orgao.put("Subseção Judiciária de São Sebastião do Paraíso ","SSP"); //(MG)
		orgao.put("Subseção Judiciária de Sete Lagoas","SLA"); //(MG)
		orgao.put("Subseção Judiciária de Teófilo Otoni","TOT"); //(MG)
		orgao.put("Subseção Judiciária de Uberaba ","UB"); //(MG)
		orgao.put("Subseção Judiciária de Uberlândia ","UD"); //(MG)
		orgao.put("Subseção Judiciária de Unaí","UNI"); //(MG)
		orgao.put("Subseção Judiciária de Varginha","VGA"); //(MG)
		orgao.put("Subseção Judiciária de Viçosa","VCS"); //(MG)
		orgao.put("Seção Judiciária do Estado do Pará","PA");
		orgao.put("Subseção Judiciária de Altamira","ATM"); //(PA)
		orgao.put("Subseção Judiciária de Castanhal","CAH"); //(PA)
		orgao.put("Subseção Judiciária de Itaituba","IAB"); //(PA)
		orgao.put("Subseção Judiciária de Marabá ","MB"); //(PA)
		orgao.put("Subseção Judiciária de Paragominas","PGN"); //(PA)
		orgao.put("Subseção Judiciária de Redenção","RDO"); //(PA)
		orgao.put("Subseção Judiciária de Santarém ","ST"); //(PA)
		orgao.put("Subseção Judiciária de Tucuruí","TUU"); //(PA)
		orgao.put("Seção Judiciária do Estado do Piauí","PI");
		orgao.put("Subseção Judiciária de Corrente ","CNT"); //(PI)
		orgao.put("Subseção Judiciária de Floriano ","FLO"); //(PI)
		orgao.put("Subseção Judiciária de Parnaíba ","PNA"); //(PI)
		orgao.put("Subseção Judiciária de Picos ","PCZ"); //(PI)
		orgao.put("Subseção Judiciária de São Raimundo Nonato ","SRN"); //(PI)
		orgao.put("Seção Judiciária do Estado de Rondônia","RO");
		orgao.put("Subseção Judiciária de Guajará-Mirim","GUM"); //(RO)
		orgao.put("Subseção Judiciária de Ji-paraná","JIP"); //(RO)
		orgao.put("Subseção Judiciária de Vilhena","VHA"); //(RO)
		orgao.put("Seção Judiciária do Estado de Roraima","RR");
		orgao.put("Seção Judiciária do Estado de Tocantins","TO");
		orgao.put("Subseção Judiciária de Araguaína","ARN"); //(TO)
		orgao.put("Subseção Judiciária de Gurupi","GUR"); //(TO)
		
		tipos.add("Cíveis");
		tipos.add("Criminais");
		tipos.add("Cíveis e Criminais");
		tipos.add("Cíveis, Criminais e JEF");
	}

	private ParserConfigDialogTRF1 configDialog = new ParserConfigDialogTRF1();
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
				String orgaoId = getIdFromOrgao(orgaoEmissor);
				nadaConsta.setLinkValidacao(new URI("http", "www.trf1.jus.br",
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
			String text = ParserUtil.joinLines(response.getText());
			
			if (text.contains("os dados informados n&atilde;o conferem com os da certid&atilde;o emitida")) {
				throw new ValidationException("Certidão Inválida: Dados não conferem"); 
			}
			String[] expectedText = new String[] {
					"Certid&atilde;o n&uacute;mero "+certidao.getCodigoAutenticacao()+" emitida via Internet &agrave;s "
							+certidao.getDataEmissao().getHora() +" horas do dia "+certidao.getDataEmissao().getDia(),
							"N A D A&nbsp;&nbsp;C O N S T A&nbsp;&nbsp;",
							"em nome de " + certidao.getNome() + ",",
							": " + certidao.getCpfCnpj(),
							certidao.getOrgaoEmissao() + "</span>",
			};
			List<String> divergencias = new ArrayList<String>();
			for (String string : expectedText) {
				if (!text.contains(string)) {
					divergencias.add(string);
				}
			}
			if (divergencias.size() > 0) {
				throw new ValidationException("Validação Divergente: Valores esperados: " + ParserUtil.join(";", divergencias));
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public static List<String> getTiposCertidao() {
		return tipos;
	}
	
	public static Set<String> getNomeOrgaos() {
		return orgao.keySet();
	}

	public ParserConfigDialogTRF1 getConfigDialog() {
		return configDialog;
	}

	public void setConfig(ParserConfigTRF1 config) {
		this.config = config;
	}

	public ParserConfigTRF1 createConfig() {
		return new ParserConfigTRF1();
	}
	
	public static String getIdFromOrgao(String nome) {
		return orgao.get(nome);
	}
	
	public static String getOrgaoFromId(String id) {
		for (Entry<String, String> entry : orgao.entrySet()) {
			if (entry.getValue().equals(id)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
