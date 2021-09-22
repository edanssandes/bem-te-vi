package bemtevi.parsers.judiciario.trf1;

import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.NestedParserCertidao;
import bemtevi.parsers.ParserInfo;

public class ParserCertidaoTRF1 extends NestedParserCertidao implements IParserConfigurable<ParserConfigTRF1>{
	private static final String NOME_CERTIDAO = "Certidão Cíveis e Criminais - TRF";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	private ParserConfigDialogTRF1 configDialog = new ParserConfigDialogTRF1();
	private ParserConfigTRF1 config;	
	
	public ParserCertidaoTRF1() {
		super(new IParserCertidao[] {
				new ParserCertidaoTRF1v1(),
				new ParserCertidaoTRF1v2(),
				new ParserCertidaoTRF1v3(),
		});
	}

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public ParserConfigDialogTRF1 getConfigDialog() {
		return configDialog;
	}

	public void setConfig(ParserConfigTRF1 config) {
		super.setNestedConfig(config);
	}

	public ParserConfigTRF1 createConfig() {
		return new ParserConfigTRF1();
	}	

}
