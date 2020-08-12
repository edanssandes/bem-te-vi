package bemtevi.parsers.caixa.fgts;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

//import sun.misc.BASE64Decoder;
//import java.util.Base64; 

import bemtevi.model.Certidao;
import bemtevi.model.CertidaoFactory;
import bemtevi.model.Documento;
import bemtevi.model.campos.CampoData;
import bemtevi.model.campos.CampoDataValidade;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ParserInfo;
import bemtevi.parsers.ValidationException;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.ParserUtilMatcher;
import bemtevi.utils.ParserUtilPattern;
import bemtevi.utils.WebResponse;


public class ParserCertidaoCRF implements IParserCertidao, IValidadorCertidao {
	private static final String NOME_CERTIDAO = "Certificado de Regularidade do FGTS";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	private static final String HOSTNAME = "https://consulta-crf.caixa.gov.br";

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		
		ParserUtilPattern pattern = new ParserUtilPattern(ParserUtilPattern.SIMPLE_SEARCH);
		pattern.searchFirst("Certificado de Regularidade do FGTS", ParserUtilPattern.FILL_SPACES);
		pattern.searchFirst("CRF");
		pattern.search("Inscrição:\\s*([0-9./-]+)");
		pattern.search("Razão Social:\\s*(.*?)");
		pattern.search("(?:Nome\\s*Fantasia:\\s*(.*?)\\s*)?");
		pattern.search("Endereço:\\s*(.*?)");
		pattern.search("A Caixa Econômica Federal, no uso da");
		pattern.searchNext("encontra-se em situação regular perante o Fundo de Garantia");
		pattern.searchNext("Validade:\\s*(\\S*)\\s*a\\s*(\\S*)");
		pattern.search("[Certificação ]* [Número ]*: (\\d{22})");
		ParserUtilMatcher matcher = pattern.matcher(doc.getText());			

		if (matcher.find(true)) {
			String cnpj = matcher.nextGroup();
			String razaoSocial = matcher.nextGroup();
			String nomeFantasia = matcher.nextGroup();
			String endereco = matcher.nextGroup();
			String dataValidadeInicio = matcher.nextGroup();
			String dataValidadeFim = matcher.nextGroup();
			String codigoAutenticacao = matcher.nextGroup();
			// String dataEmissao = matcher.group(8);
			// String horaEmissao = matcher.group(9);

			Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
			nadaConsta.setNome(razaoSocial);
			nadaConsta.setCpfCnpj(cnpj);
			nadaConsta.setCodigoAutenticacao(codigoAutenticacao);
			nadaConsta.setDataValidade(new CampoDataValidade(dataValidadeFim));
			nadaConsta.setDataEmissao(new CampoData(dataValidadeInicio));
			nadaConsta.setNomeFantasia(nomeFantasia);
			nadaConsta.setEndereco(endereco);
			nadaConsta
					.setLinkValidacao(HOSTNAME + "/Cidadao/Crf/FgeCfSCriteriosPesquisa.asp");
			return (nadaConsta);
		} else {
			return null;
		}

	}

	public void validate(Certidao certidao) throws ValidationException, IOException {
		try {
			String cpfCnpj = certidao.getCpfCnpj().replaceAll("\\D", "");
			String auth = certidao.getCodigoAutenticacao();

			URL url = new URL(HOSTNAME + "/consultacrf/pages/consultaEmpregador.jsf");
			
			boolean regular = false;
			String cookie = null;
			String texto = null;
			
			int tentativa = 0;
			String viewState = null;
			
			while (!regular && tentativa < 5) {
				tentativa++;
				WebResponse response = ParserUtil
						.downloadFromURL(url, null, null);
				texto = response.getText();
				cookie = response.getCookie();
				viewState = ParserCertidaoCRF.getViewState(texto);
	
				Pattern p0 = Pattern.compile("src=\"data:image/png;base64,(.*?)\"");
				Matcher m0 = p0.matcher(texto);
				String imageString = null;
				if (m0.find()) {
					imageString = m0.group(1);
				} else {
					throw new IOException("Não foi encontrada a imagem do captcha");
				}
	
				Image image = decodeToImage(imageString);
				String captcha = ParserUtil.askCaptcha(image, "Caixa/CRF");
	
				//String captcha = "12345";
				
				String params = "AJAXREQUEST=_viewRoot&" +
				"mainForm%3AtipoEstabelecimento=1&" +
				"mainForm%3AtxtInscricao1=" + cpfCnpj + "&" +
				"mainForm%3Auf=&" +
				"mainForm%3AtxtCaptcha="+ captcha +"&" +
				"mainForm=mainForm&" +
				"autoScroll=&" +
				"javax.faces.ViewState=" + URLEncoder.encode(viewState, "UTF-8") + "&" +
				"mainForm%3AbtnConsultar=mainForm%3AbtnConsultar&";
				
				System.out.println(texto);
				System.out.println(cookie);
	
				WebResponse response2 = ParserUtil.downloadFromURL(url, params, cookie);
				texto = response2.getText();
				System.out.println(texto);
				
				
				Pattern p1 = Pattern.compile("<span class=\"feedback-text\">(.*?)</span>", Pattern.DOTALL);
				Matcher m1 = p1.matcher(texto);
				String feedback = null;
				if (m1.find()) {
					feedback = m1.group(1);
				} else {
					throw new IOException("Erro de parser. Não foi possível ler a mensagem de feedback.");
				}
				
				viewState = getViewState(texto);
				
				System.out.println(feedback);
				if (feedback.contains("Captcha Inválido")) {
					viewState = null;
					JOptionPane.showMessageDialog(null, "Erro de validação do Captcha. Favor digitar novamente.");
				} else if (feedback.contains("A EMPRESA abaixo identificada está REGULAR perante o FGTS")) {
					regular = true;
				} else {
					throw new RuntimeException("Resposta não reconhecida: " + feedback);
				}
			}
			if (tentativa >= 5) {
				throw new RuntimeException("Não foi possível validar o captcha");
			}
			
			URL url2 = new URL(HOSTNAME + "/consultacrf/pages/consultaRegularidade.jsf");
			String params2 = "AJAXREQUEST=_viewRoot&" +
					"mainForm%3AcodAtivo=&" +
					"mainForm%3AlistEmpFpas=true&" +
					"mainForm%3AhidCodPessoa=0&" +
					"mainForm%3AhidCodigo=0&" +
					"mainForm%3AhidDescricao=&" +
					"mainForm=mainForm&" +
					"autoScroll=&" +
					"javax.faces.ViewState=" + URLEncoder.encode(viewState, "UTF-8") + "&" +
					"mainForm%3Aj_id54=mainForm%3Aj_id54&";		
			WebResponse response3 = ParserUtil.downloadFromURL(url2, params2, cookie);
			
			texto = response3.getText();
			System.out.println(texto);
			viewState = getViewState(texto);

			Pattern p2 = Pattern
					.compile("<tr[^>]*?>" +
							"<td[^>]*?><span[^>]*?>(\\S+?)</span></td>" +
							"<td[^>]*?><span[^>]*?>\\S+?</span><span[^>]*?> a </span><span[^>]*?>(\\S+?)</span><br\\s*/></td>" +
							"<td[^>]*?><span[^>]*?>"+auth+"</span></td>" +
							"</tr>", Pattern.CASE_INSENSITIVE);
			Matcher m2 = p2.matcher(texto);

			if (!m2.find()) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -24);
				Date date24MesesAtras = cal.getTime();
				String mensagem = "Número de CRF não encontrado: "	+ auth;
				if (certidao.getDataEmissao().compareTo(date24MesesAtras) < 0) {
					mensagem += ". Somente é possível validar CRFs concedidas nos últimos 24 meses."; 
				}
				throw new ValidationException(mensagem);
			} else {
				String dataEmissao = m2.group(1);
				String dataValidade = m2.group(2);
				if (!certidao.getDataEmissao().getDia().equals(dataEmissao)) {
					throw new ValidationException("Data de Emissão divergente");
				} else if (!certidao.getDataValidade().getDia()
						.equals(dataValidade)) {
					throw new ValidationException("Data de Validade divergente");
				}
			}
		} catch (IOException e) {
			throw e;
		}
	}
	
	
	private static String getViewState(String html) throws IOException {
		Pattern p0 = Pattern.compile("<input.*?name=\"javax.faces.ViewState\".*?value=\"(\\S*?)\"");
		Matcher m0 = p0.matcher(html);
		String viewState = null;
		if (m0.find()) {
			return m0.group(1);
		} else {
			throw new IOException("Não foi encontrado o view state");
		}
	}
	
	// https://javapointers.com/tutorial/java-convert-image-to-base64-string-and-base64-to-image/
	/*private static Image decodeToImage(String imageString) {

        Image image = null;
        byte[] imageByte;
        try {
            //BASE64Decoder decoder = new BASE64Decoder();
            //imageByte = decoder.decodeBuffer(imageString);
        	imageByte = Base64.getDecoder().decode(imageString.getBytes());
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }*/
	
	// https://stackoverflow.com/questions/469695/decode-base64-data-in-java
	/**
     * Translates the specified Base64 string into a byte array.
     *
     * @param s the Base64 string (not null)
     * @return the byte array (not null)
     */
	private static Image decodeToImage(String s) {
		char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	    int[] toInt = new int[128];

        for(int i=0; i< ALPHABET.length; i++){
            toInt[ALPHABET[i]]= i;
        }
	        
        int delta = s.endsWith( "==" ) ? 2 : s.endsWith( "=" ) ? 1 : 0;
        byte[] buffer = new byte[s.length()*3/4 - delta];
        int mask = 0xFF;
        int index = 0;
        for(int i=0; i< s.length(); i+=4){
            int c0 = toInt[s.charAt( i )];
            int c1 = toInt[s.charAt( i + 1)];
            buffer[index++]= (byte)(((c0 << 2) | (c1 >> 4)) & mask);
            if(index >= buffer.length){
                break;
            }
            int c2 = toInt[s.charAt( i + 2)];
            buffer[index++]= (byte)(((c1 << 4) | (c2 >> 2)) & mask);
            if(index >= buffer.length){
                break;
            }
            int c3 = toInt[s.charAt( i + 3 )];
            buffer[index++]= (byte)(((c2 << 6) | c3) & mask);
        }
        
        Image image = null;
        try { 
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
        	image = ImageIO.read(bis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    } 	
		
	
	public static void main(String[] args) throws IOException, ValidationException {
		ParserCertidaoCRF parser = new ParserCertidaoCRF();
		File f = new File("pdf/internet/crf/ASSOCIACAO MINEIRA DE MUNICIPIOS.pdf");
		Certidao d = CertidaoFactory.parse(f, parser);
		parser.validate(d);
		//parser.validate(d);
	}
}
