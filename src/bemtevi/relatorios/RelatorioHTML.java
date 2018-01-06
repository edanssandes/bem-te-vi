package bemtevi.relatorios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bemtevi.AppVersion;
import bemtevi.configs.PreferenciasRelatorioHTML;
import bemtevi.model.Certidao;
import bemtevi.model.Certidoes;
import bemtevi.model.CertidoesSecao;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.model.campos.CampoLista;
import bemtevi.model.campos.CampoVerificado;
import bemtevi.parsers.ParserCertidaoInvalida;



/**
 * Classe que cria o relatório HTML
 * @author edans
 */
public class RelatorioHTML {

	private static String template;
	private static final String IMG_HTML_OK = "<span class='icon_ok'/>";
	private static final String IMG_HTML_ERROR = "<span class='icon_error'/>";
	private static final String IMG_HTML_QUESTION = "<span class='icon_info'/>";
	private static final String IMG_HTML_PF = "<span class='icon_pf'/>";
	private static final String IMG_HTML_PJ = "<span class='icon_pj'/>";

	private Certidoes certidoes;
	private PreferenciasRelatorioHTML preferenciasRelatorioHTML;
	private boolean possuiCertidaoExpirada;
	private Date dataDeValidacao;
	
	static {
		InputStream in = RelatorioHTML.class
				.getResourceAsStream("template.html");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			template = sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			template = e.getMessage();
		}
	}

	public RelatorioHTML(Certidoes certidoes,
			PreferenciasRelatorioHTML preferenciasRelatorioHTML,
			Date dataDeValidacao) {
		this.certidoes = certidoes;
		this.preferenciasRelatorioHTML = preferenciasRelatorioHTML;
		this.dataDeValidacao = dataDeValidacao;
	}

	public void save(File file) throws IOException {
		possuiCertidaoExpirada = false;
		Date now = new Date();
		if (this.dataDeValidacao == null) {
			dataDeValidacao = now;
		}

		StringBuilder body = new StringBuilder();

		for (CertidoesSecao secao : certidoes.getSecoes()) {
			body.append("<hr>\n");
			body.append("<h1><em>" + secao.getNomeCertidao() + "</em></h1>\n");
			List<String> header;
			boolean certidaoInvalida = secao.get(0).getParser().getClass() == ParserCertidaoInvalida.class;
			if (certidaoInvalida) {
				header = new ArrayList<String>();
				header.add("Nome do Arquivo");
			} else {
				header = secao.getHeaders();
			}
			int rows = secao.size();
			int cols = header.size();

			String[][] table = new String[rows][cols];
			for (int i = 0; i < rows; i++) {
				Certidao certidao = secao.get(i);
				if (certidaoInvalida) {
					table[i][0] = certidao.getDocumento().getName();
				} else {
					for (int j = 0; j < cols; j++) {
						String campo = header.get(j);
						Object valor = certidao.getInfoObject(campo);
						
						String cell;
						if (valor instanceof CampoLista) {
							cell = null;
							for (Object item : ((CampoLista)valor).getItems()) {
								if (cell == null) {
									cell = "";
								} else if (item != null) {
									cell += "<br>";
								}
								cell += getCampoHTML(certidao, campo, item);
							}
						} else {
							cell = getCampoHTML(certidao, campo, valor);
						}

						table[i][j] = cell;
					}
				}
			}

			Map<String, Integer> defaultSizes = new HashMap<String, Integer>();
			defaultSizes.put(Certidao.CAMPO_NOME, 350);
			defaultSizes.put(Certidao.CAMPO_NOME_FANTASIA, 150);
			defaultSizes.put(Certidao.CAMPO_CPF_CNPJ, 100);
			defaultSizes.put(Certidao.CAMPO_CODIGO_AUTENTICACAO, 240);
			defaultSizes.put(Certidao.CAMPO_DATA_EMISSAO, 100);
			defaultSizes.put(Certidao.CAMPO_DATA_VALIDADE, 100);
			defaultSizes.put(Certidao.CAMPO_ORGAO_EMISSOR, 200);
			defaultSizes.put(Certidao.CAMPO_SITUACAO, 140);
			
			Map<String, String> defaultAlign = new HashMap<String, String>();
			//defaultAlign.put(Certidao.CAMPO_CODIGO_AUTENTICACAO, "center");
			defaultAlign.put(Certidao.CAMPO_DATA_EMISSAO, "center");
			defaultAlign.put(Certidao.CAMPO_DATA_VALIDADE, "center");

			body.append("<table cellspacing=\"0\" border=\"0\">\n");

			List<String> tdAlign = new ArrayList<String>();
			for (int j = 0; j < cols; j++) {
				String param;
				if (defaultSizes.containsKey(header.get(j))) {
					param = " width='" + defaultSizes.get(header.get(j)) + "'";
				} else if (certidaoInvalida) {
					param = " width='300'";
				} else {
					param = " style='white-space:nowrap;'";
				}

				body.append("<colgroup" + param + "\"></colgroup>\n");
				
				if (defaultAlign.containsKey(header.get(j))) {
					tdAlign.add(defaultAlign.get(header.get(j)));
				} else {
					tdAlign.add("left");
				}			
			}
			body.append("<colgroup span=\"8\" width=\"85\"></colgroup>\n");
			body.append("<thead>\n");
			body.append("<tr>\n");
			for (int j = 0; j < cols; j++) {
				body.append("<th align=\"center\"><b>" + header.get(j)
						+ "</b></th>\n");
			}
			body.append("</tr>\n");
			body.append("</thead>\n");
			body.append("<tbody>\n");

			
			for (int i = 0; i < rows; i++) {
				body.append("<tr>\n");
				for (int j = 0; j < cols; j++) {
					body.append("<td align=\""+tdAlign.get(j)+"\">" + table[i][j] + "</td>\n");
				}
				body.append("</tr>\n");
			}
			body.append("</tbody>\n");
			body.append("</table>\n");
		}

		SimpleDateFormat datetimeFormat = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String footer1 = "Relatório gerado em " + datetimeFormat.format(now)
				+ " pelo aplicativo " + AppVersion.APP_NAME + " versão: "
				+ AppVersion.VERSION;
		String footer2 = "";
		if (possuiCertidaoExpirada) {
			footer2 += "As certidões apresentadas como expiradas possuem data de validade anterior a "
					+ dateFormat.format(dataDeValidacao) + ".";
		}

		
		String cabecalho = preferenciasRelatorioHTML.getHeader();
		String rodape = preferenciasRelatorioHTML.getFooter();
		if (rodape.length() > 0) {
			rodape += "<hr>";
		}
		
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
				file), "UTF-8");
		String html = template.replace("$body", body.toString())
				.replace("$footer1", footer1)
				.replace("$footer2", footer2)
				.replace("$cabecalho", cabecalho)
				.replace("$rodape", rodape);
		out.write(html);
		out.close();
	}

	private String getCampoHTML(Certidao certidao,
			String campo, Object valor) {
		String cell;
		if (valor == null) {
			cell = "<div style='text-align: center;'>-</div>";
		} else if (campo == Certidao.CAMPO_CODIGO_AUTENTICACAO) {
			String link = certidao.getLinkValidacao();
			if (link != null) {
				cell = "<a href='"
						+ link
						+ "' target='popup' onclick=\"window.open('"
						+ link
						+ "','name','width=800,height=600')\">"
						+ valor + "</a>";
			} else {
				cell = "" + valor;
			}
			if (certidao.getResultadoValidacao() == Certidao.StatusValidacao.CERTIDAO_VALIDA) {
				cell = cell + IMG_HTML_OK;
			} else if (certidao.getResultadoValidacao() == Certidao.StatusValidacao.CERTIDAO_INVALIDA) {
				cell = cell + "<span title='"
						+ certidao.getMensagemValidacao()
						+ "' class='icon_error'/>";
			} else if (certidao.getResultadoValidacao() == Certidao.StatusValidacao.ERRO_DE_VALIDACAO) {
				cell = cell + "<span title='"
						+ certidao.getMensagemValidacao()
						+ "' class='icon_info'/>";
			}
			cell = "<pre>" + cell + "</pre>";
		} else if (valor instanceof CampoDataValidade) {
			CampoDataValidade date = (CampoDataValidade)valor;
			if (date.compareTo(dataDeValidacao) < 0) {
				cell = "<font color='red'>" + valor + "</font>"
						+ IMG_HTML_ERROR;
				this.possuiCertidaoExpirada = true;
			} else {
				cell = "<font color='green'>" + valor
						+ "</font>";// + IMG_HTML_OK;
			}
		} else if (campo == Certidao.CAMPO_NOME) {
			String tipoPessoa = certidao.getTipoPessoa();
			if (tipoPessoa.equals(Certidao.PESSOA_FISICA)) {
				cell = IMG_HTML_PF + "&nbsp;" + valor;
			} else if (tipoPessoa
					.equals(Certidao.PESSOA_JURIDICA)) {
				cell = IMG_HTML_PJ + "&nbsp;" + valor;
			} else {
				cell = tipoPessoa + valor;
			}
		} else if (valor instanceof CampoVerificado) {
			CampoVerificado campoVerificado = (CampoVerificado)valor;
			String icon = "";
			if (campoVerificado.getStatus() == CampoVerificado.Status.PROBLEMA) {
				cell = "<font color='red'>" + valor + "</font>";
				icon = IMG_HTML_ERROR;
			} else if (campoVerificado.getStatus() == CampoVerificado.Status.INFO) {
				cell = "<font color='#608020'>" + valor
						+ "</font>";
			} else if (campoVerificado.getStatus() == CampoVerificado.Status.OK) {
				cell = "<font color='green'>" + valor
						+ "</font>";
			} else {
				cell = "" + valor;
			}
			String observacoes = campoVerificado.getObservacoes();
			if (observacoes != null && icon.equals("")) {
				icon = IMG_HTML_QUESTION;
			}
			cell = cell + icon;
			if (observacoes != null) {
				cell = "<div title='" + observacoes + "'>"
						+ cell + "</div>";
			}

		} else {
			cell = "" + valor;
		}
		return cell;
	}

}
