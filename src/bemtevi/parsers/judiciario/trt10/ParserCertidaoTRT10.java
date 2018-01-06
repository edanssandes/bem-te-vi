package bemtevi.parsers.judiciario.trt10;

import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.NestedParserCertidao;
import bemtevi.parsers.ParserInfo;

public class ParserCertidaoTRT10 extends NestedParserCertidao {

	private static final String NOME_CERTIDAO = "Certidão de Distribuição de Ações Trabalhistas - TRT 10º Região";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);
	
	public ParserCertidaoTRT10() {
		super(new IParserCertidao[] {
				new ParserCertidaoTRT10v1(),
				new ParserCertidaoTRT10v2(),
		});
	}

	public ParserInfo getParserInfo() {
		return parserInfo;
	}

}
