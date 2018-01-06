package bemtevi.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class WebResponse {
	private InputStream is;
	private HttpURLConnection con;
	private String cookie;
	public WebResponse(InputStream is, String cookie) {
		this.is = is;
		this.cookie = cookie;
	}
	
	public WebResponse(HttpURLConnection con) throws IOException {
		this.con = con;
		this.is = con.getInputStream();			
		
		if (cookie == null) {
			Map<String, List<String>> headerFields = con.getHeaderFields();
			List<String> cookiesHeader = headerFields.get("Set-Cookie");
			if (cookiesHeader != null) {
				cookie = ParserUtil.join(";",  cookiesHeader);
			}
			System.out.println(cookie);
		}
	}
	
	public void disconnect() {
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.disconnect();
	}

	public String getCookie() {
		return cookie;
	}
	
	public String getText() throws IOException {
		return getText("ISO-8859-1");
	}
	
	public String getTextUtf8() throws IOException {
		return getText("UTF-8");
	}
	
	public String getText(String encoding) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				is, encoding));
		StringBuilder sb = new StringBuilder();
		String inputLine;

		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine);

		String response = sb.toString();
		disconnect();
		//String response = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head> <title>Sistema de Emiss&atilde;o de Certid&otilde;s Negativas da 1&ordm; Regi&atilde;o</title> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"> <link href=\"/Objetos/trf1_style.css\" rel=\"stylesheet\" type=\"text/css\"> <script language=\"JavaScript1.2\" src=\"/Objetos/trf1_layout.js\"></script></head><body><!-- DF || EDANS FLAVIUS DE OLIVEIRA SANDES || EDANS FLAVIUS DE OLIVEIRA SANDES || 3400 --> <img src=\"/ImagemIcone/trf1_icone_imprimir.gif\" onClick=\"print();\"> <font class=\"titulo2\"><center>Confirma&ccedil;&atilde;o da Autenticidade de Certid&otilde;es</b></font></center> <script>imprime_barra8();</script><br> <table width=450 align=center> <tr><td style=\"text-align: justify;text-indent: 50pt;\"> Certid&atilde;o n&uacute;mero 70211 emitida via Internet &agrave;s 19:18 horas do dia 26/03/2017.<br> </td></tr> <tr><td style=\"text-align: justify;text-indent: 50pt;\"> Resultado: &quot;<span class=\"fontGeral\">N A D A&nbsp;&nbsp;C O N S T A&nbsp;&nbsp;na Justi&ccedil;a Federal de 1&ordf; Inst&acirc;ncia, Seção Judiciária do Distrito Federal</span>, em nome de EDANS FLAVIUS DE OLIVEIRA SANDES, CPF: 001.312.061-17&quot;.</p> </td></tr> </table><br /> <script>imprime_barra8();</script><br> <table cellpadding=0 cellspacing=0 align=center> <tr><td class=\"font1\"><!--?=carimbo()?--></td></tr> </table></body></html>";
		
		//String response = "<title>Autenticador - TRT 10ª Região</title><script language=\"JavaScript\" src='/js/funcoes.js' ><!--// --></script><script language=\"JavaScript\" src='/js/forms.js' ><!--// --></script><link rel='stylesheet' href='/css/autenticador.css' type='text/css'  /><link rel='stylesheet' href='/css/nao_imprimir.css' type='text/css' media=print /><div id=\"div_cabecalho_impressao\" name=\"div_cabecalho_impressao\"  style=\"width: 100%\"  class=\"nao_imprimir\" ><form name=barra_impressao  action=/ method=post id=barra_impressao ><input type=hidden name=PHPSESSID value=\"7eip19mnin2jf43h1a24j1ck31\" ><input type=hidden name=modulo value=\"\" ><input type=hidden name=modulo_anterior id=\"modulo_anterior\" value=\"\"><input type=hidden name=sessao_valida id=\"sessao_valida\" value=\"\"><input type=hidden name=mbref id=\"mbref\" value=\"\"><input type=hidden name=mbref_anterior id=\"mbref_anterior\" value=\"\"><input type=hidden name=sessao_valida id=\"sessao_valida\" value=\"\"><button type=button  Onclick=\"parent.window.print()\" class=botao >Imprimir</button><button type=button  Onclick=\"document.barra_impressao.submit();\" class=botao >Nova Consulta</button></form></div><div id=\"div_conteudo_impressao\" name=\"div_conteudo_impressao\" ><div id=\"documento_autenticado\" name=\"documento_autenticado\"  class=\"hr\" >DOCUMENTO AUTENTICADO em 27/03/2017 11:04:52 [6DRooTLqM1VfAgpqLt]</div> <table width='700' border='0' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>  <tr>    <td colspan='3' align='center'><br><br><strong style='font-family: Arial, Helvetica, sans-serif;            font-size: 18px;'>***CERTIDÃO VÁLIDA*** </strong><br><br></td>  </tr>  <tr>    <td colspan='3'>&nbsp;</td>  </tr>  <tr>    <td colspan='3' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>O TRT da 10ª Região garante apenas a verifica&ccedil;&atilde;o de autenticidade realizada diretamente <strong>pela autoridade recebedora competente</strong>, na p&aacute;gina www.trt10.jus.br, n&atilde;o sendo v&aacute;lidas verifica&ccedil;&otilde;es de autenticidade meramente impressas. </td>  </tr>  <tr>    <td width='102'>&nbsp;</td>    <td width='185'>&nbsp;</td>    <td width='399'>&nbsp;</td>  </tr>  <tr>    <td>&nbsp;</td>    <td>&nbsp;</td>    <td>&nbsp;</td>  </tr>  <tr>    <td>&nbsp;</td>    <td width='185' style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>N&uacute;mero de Controle: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>439 - 6DRooTLqM1VfAgpqLt</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Nome:</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>EDANS FLAVIUS DE OLIVEIRA SANDES</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>CPF</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>001.312.061-17</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Data de Emiss&atilde;o: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>26/03/2017 - 19:13:37</b></td>  </tr>  <tr>    <td>&nbsp;</td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'>Tipo de Certid&atilde;o: </td>    <td style='font-family: Arial, Helvetica, sans-serif;           font-size: 16px;           text-align: justify;'><b>Certidão de Distribuição de Ações Trabalhistas</b></td>  </tr></table></div>";
		
		System.out.println(response);
		response = ParserUtil.joinLines(response);
		
		return response;
	}
	
	public Image getImage() throws IOException {
	    BufferedImage image = ImageIO.read(is);
	    disconnect();
	    return image;
	}

	public boolean saveFile(File name) throws IOException {

		OutputStream out = new FileOutputStream(name);
        int read = 0;
        byte[] bytes = new byte[1024];
        boolean empty = true;

        while ((read = is.read(bytes)) != -1) {
        	out.write(bytes, 0, read);
        	empty = false;
        }
		out.close();
		disconnect();
		return !empty;
	}

}
