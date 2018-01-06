package bemtevi.parsers;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;

/**
 * Parser default utilizado para criar as certidões inválidas (i.e. certidões
 * que não foram reconhecidas por nenhum outro parser). 
 * 
 * @author edans
 */
public class ParserCertidaoInvalida implements IParserCertidao {
	private static final String NOME_CERTIDAO = "Certidão não reconhecida";
	private static final ParserInfo parserInfo = new ParserInfo(NOME_CERTIDAO);

	public ParserInfo getParserInfo() {
		return parserInfo;
	}
	
	public Certidao parse(Documento doc) {
		Certidao nadaConsta = new Certidao(NOME_CERTIDAO);
		return nadaConsta;
	}

}
