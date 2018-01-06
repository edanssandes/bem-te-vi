package bemtevi.parsers;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;

/**
 * Esta interface deve ser implementada por todos os parsers que validam
 * certidões.
 * 
 * @author edans
 */
public interface IParserCertidao {
	public ParserInfo getParserInfo();
	public Certidao parse(Documento doc);
}
