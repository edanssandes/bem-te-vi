package bemtevi.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import bemtevi.model.Certidao;
import bemtevi.model.CertidaoFactory;
import bemtevi.parsers.IParserCertidao;


public class ParserUtil {

	public static final String REGEX_CPF = "\\d{3}\\.\\d{3}\\.\\d{3}-\\s*\\d{2}";
	public static final String REGEX_CNPJ = "\\d{2}\\.\\d{3}\\.\\d{3}[/.]\\d{4}-\\s*\\d{2}";
	public static final String REGEX_CPF_OU_CNPJ = REGEX_CPF + "|" + REGEX_CNPJ;

	static {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
        try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
 
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);		
	}
	
	public static String simplifyText(String text) {
		// Normalização de caracteres
		text = text.replaceAll("(?m)[ \t\u00a0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]+", " "); // espaços
		text = text.replaceAll("[\u00ad\u2010-\u2015]", "-"); // hiphen

		// Remove linhas vazias.
		text = text.replaceAll("(?m)^\\s*$", ""); 
		// Une quebras de linha consecutivas em uma só.
		text = text.replaceAll("(?md)[\r\n]+", "\n"); 
		// Para cada linha, une os espaços consecutivos em um só.
		text = text.replaceAll("(?m)[ ]+", " ");
		return text;
	}
	
	public static String normalize(String documentText) {
		// Normalização de texto Unicode
		documentText = Normalizer.normalize(documentText, Form.NFKC);
		// Outras Normalizações
		//documentText = documentText.replaceAll("\u00ad", "-");
		return documentText;
	}
	

	public static String joinLines(String documentText) {
		// Une espaçoes em branco e quebras de linhas consecutivas em um espaço.
		documentText = documentText.replaceAll("(?md)\\s+", " "); 
		return documentText;
	}


	public static WebResponse downloadFromURL(URL url, String postParams, String cookie)
			throws MalformedURLException, IOException,
			UnsupportedEncodingException {
		//CookieManager msCookieManager = new CookieManager();
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);
		if (cookie != null) {
			con.setRequestProperty("Cookie", cookie);
		}
		if (postParams != null) {
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(postParams);
			writer.flush();		
		}

		
		return new WebResponse(con);

	}


	public static Certidao downloadCertidao(URL url, String postParams, String cookie, IParserCertidao parser)
			throws MalformedURLException, IOException,
			UnsupportedEncodingException {
		WebResponse response3 = downloadFromURL(url, postParams, cookie);
		File name = File.createTempFile("downloadTmpBTV", ".pdf");
		if (response3.saveFile(name)) {
			Certidao certidao = CertidaoFactory.parse(name, parser);
			name.delete();
			return certidao;
		} else {
			JOptionPane.showMessageDialog(null, "Erro: " + parser.getParserInfo().getName());
			throw new IOException("Erro ao efetuar download da certidão original: " + url);
		}
	}
	
	public static String askCaptcha(Image image) {
		final JDialog frame = new JDialog();
		frame.setTitle("Código");
		frame.setPreferredSize(new Dimension(200,150));
		frame.getContentPane().setBackground(Color.BLACK);
	    JLabel label = new JLabel(new ImageIcon(image));
	    frame.getContentPane().add(label, BorderLayout.CENTER);
	    JTextField captcha = new JTextField(10);
	    captcha.setFont(captcha.getFont().deriveFont(20.0f));
	    captcha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
	    frame.getContentPane().add(captcha, BorderLayout.SOUTH);
	    frame.pack();
	    frame.setModal(true);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);	
	    
	    String captchaTxt = captcha.getText();
		return captchaTxt;
	}	
	
	public static String join(String separator, List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String str : list) {
			if (sb.length() != 0) {
				sb.append(separator);
			}
			sb.append(str);
		}
		return sb.toString();
	}

}
