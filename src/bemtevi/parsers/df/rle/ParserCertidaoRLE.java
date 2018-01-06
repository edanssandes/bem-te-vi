package bemtevi.parsers.df.rle;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.model.campos.CampoLista;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;


public class ParserCertidaoRLE implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Registro de Licenciamento de Empresas - RLE/DF";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.MULTILINE_SEARCH);
		pattern.searchFirst("CERTIFICADO DE LICENCIAMENTO");
		pattern.searchNext("Emissão do Documento\\s*(\\d+/\\d+/\\d+)\\s*(\\d+:\\d+:\\d+)");
		pattern.searchNext("DADOS DA EMPRESA");
		pattern.searchNext("Nome da Empresa\\:\\s*(.*?)");
		pattern.search("Endereço do Empreendimento\\:\\s*(.*)");
		//pattern.search("Número do Registro:"); // 3 colunas
		pattern.search("Número de Registro:\\s*CNPJ:\\s*Inscrição Estadual:"); // 3 colunas
		pattern.search("(\\d+?)"); // Registro
		pattern.search("(" + ParserUtil.REGEX_CNPJ + ")"); // CNPJ
		pattern.search("(\\S*?)"); // IE: Nunca testei
		pattern.searchNext("Área do estabelecimento ([\\d.,]+)");
		pattern.searchFirst("LICENCIAMENTO DAS ATIVIDADES(.*)");
		pattern.searchFirst("Valide o certificado no site \\S+ informando o CNPJ e o código (\\S+?)\\s");
		ParserUtilMatcher matcher = pattern.matcher(doc.getTextInOrder());			
		
		if (matcher.find(true)) {
			String dataEmissao = matcher.nextGroup();
			String horaEmissao = matcher.nextGroup();
			String nomeCompleto = matcher.nextGroup();
			String endereco = matcher.nextGroup();
			String numeroRegistro = matcher.nextGroup();
			String cnpj = matcher.nextGroup();
			String inscricaoEstadual = matcher.nextGroup();
			String area = matcher.nextGroup();
			String licenciamento = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			
			// CORPO DE BOMBEIROS MILITAR DO DISTRITO FEDERAL - CBMDF\nAtividades Licenciadas\nCNAE Descrição Validade\n8599-6/01 Formacao de condutores 19/12/2019\n8599-6/02 Cursos de pilotagem 19/12/2019\n
			Pattern p0 = Pattern.compile("\n(.*?)\n(Atividades .*)\nCNAE Descrição(?: Validade)?((?:\n[\\d-/]{7,}.*?(?: \\d+/\\d+/\\d+)?(?=\n))+)", Pattern.MULTILINE);
			Pattern p1 = Pattern.compile("\n([\\d-/]{7,})\\s*(.*?)\\s*((?:\\d+/\\d+/\\d+)?)(?=\n)");
			
			Map<String, List<CampoDataValidade>> licencas = new LinkedHashMap<String, List<CampoDataValidade>>();
			
			CampoData dataValidade = null; // Menor das validades apresentada na certidão
			Matcher m0 = p0.matcher(licenciamento);
			List<String> orgaos = new ArrayList<String>();
			while (m0.find()) {
				String orgao = m0.group(1);
				String sigla = orgao.substring(orgao.lastIndexOf(" - ") + 3);
				String licenca = m0.group(2);
				String validades = m0.group(3);
				
				orgaos.add(sigla);
				boolean licencaDispensada = licenca.contains("Dispensadas");
				
				
				Matcher m1 = p1.matcher(validades + "\n");
				while (m1.find()) {
					System.out.println(m1.group());
					String atividade = m1.group(1) + ": " + m1.group(2);
					CampoDataValidade validade = null;
					if (!licencaDispensada) {
						validade = new CampoDataValidade(m1.group(3));
						if (dataValidade == null) {
							dataValidade = validade;
						} else {
							if (dataValidade.compareTo(validade) > 0) {
								dataValidade = validade;
							}
						}
					}
					if (!licencas.containsKey(atividade)) {
						licencas.put(atividade, new ArrayList<CampoDataValidade>());
					}
					licencas.get(atividade).add(validade);
					
				}
			}
			System.out.println(orgaos);
			System.out.println(licencas);
			

			Certidao certidao = new Certidao(NOME_CERTIDAO);
			certidao.setNome(nomeCompleto);
			certidao.setCpfCnpj(cnpj);
			certidao.setCodigoAutenticacao(codigoAutenticacao);
			certidao.setLinkValidacao("http://portalservicos.jcdf.mdic.gov.br/licenciamento-web");
			certidao.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			certidao.setEndereco(endereco);
			certidao.setExtraInfo("Área", area + "m2");
			//certidao.setDataValidade(dataValidade);
			
			CampoLista atividades = new CampoLista(licencas.keySet());
			certidao.setExtraInfo("Atividade", atividades);
			for (int i=0; i<orgaos.size(); i++) {
				CampoLista validadesOrgao = new CampoLista();
				for (String atividade : licencas.keySet()) {
					validadesOrgao.addItem(licencas.get(atividade).get(i));
				}
				certidao.setExtraInfo(orgaos.get(i), validadesOrgao);
			}

			/*String resultado = matcher.nextGroup();
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
			nadaConsta.setDataValidade(new CampoData(dataValidade));
			nadaConsta.setDataEmissao(new CampoData(dataEmissao, horaEmissao));
			nadaConsta.setEndereco(endereco + " - " + cidade);
			nadaConsta
					.setLinkValidacao("http://www.fazenda.df.gov.br/area.cfm?id_area=84");
			//nadaConsta.setResultado(resultado);
			return (nadaConsta);*/
			return certidao;
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {

		try {
			String codigo = certidao.getCodigoAutenticacao();
			String cpfCnpj = certidao.getCpfCnpj();//.replaceAll("\\D", "");
			WebResponse response1 = ParserUtil
					.downloadFromURL(
							new URL("http://portalservicos.jcdf.mdic.gov.br/licenciamento-web/pages/licenciamento/consultarLicenciamentoEmpresa.jsf"),
							null, null);
			String savedCookie = response1.getCookie();
			String text1 = response1.getText();

			Pattern p1 = Pattern.compile("<input.*?name=\"javax.faces.ViewState\".*?value=\"(\\S*?)\"");
			Matcher m1 = p1.matcher(text1);
			String viewState = null;
			if (m1.find()) {
				viewState = m1.group(1);
			} else {
				throw new IOException("Não foi encontrado o view state");
			}
			
			
			String params = "formLicenciar=formLicenciar&" +
					"inputIdentificador=&" +
					"inputCnpjValidacao=" + URLEncoder.encode(cpfCnpj, "UTF-8") + "&" +
					"inputCodigoValidacao=" + codigo + "&" +
					"javax.faces.ViewState=" + URLEncoder.encode(viewState, "UTF-8") + "&" +
					"validarCodigoCert=validarCodigoCert";
			
			WebResponse response2 = ParserUtil
					.downloadFromURL(
							new URL("http://portalservicos.jcdf.mdic.gov.br/licenciamento-web/pages/licenciamento/consultarLicenciamentoEmpresa.jsf"),
							params, savedCookie);
		
			System.out.println(response2);
			
			String text2 = response2.getTextUtf8();
			System.out.println(text2);
			
			if (!text2.contains("O código do certificado informado é válido.")) {
				throw new ValidationException("Certidão Inválida");
			}
			
			URL downloadUrl = new URL("http://portalservicos.jcdf.mdic.gov.br/" +
					"licenciamento-web/pages/licenciamento/" +
					"visualizacaoCertificadoLicenciamento.jsf" +
					"?faces-redirect=true&refCnpj=" + cpfCnpj.replaceAll("\\D", ""));
			Certidao certidao2 = ParserUtil.downloadCertidao(downloadUrl, null, savedCookie, this);
			certidao2.assertEquals(certidao, new String[] {Certidao.CAMPO_DATA_EMISSAO});
		} catch (IOException e) {
			throw e;
		}
	}

}
