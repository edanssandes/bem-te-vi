package bemtevi.parsers;

import bemtevi.model.Certidao;
import bemtevi.model.Documento;

/**
 * Classe utilizada para agrupar vários parser em um único analisador.
 * Esta classe é util quando um órgão possui diferentes
 * versões de certidão. 
 * 
 * @author edans
 */
public abstract class NestedParserCertidao implements IParserCertidao {
	private IParserCertidao childParsers[];
	
	public NestedParserCertidao(IParserCertidao[] childParsers) {
		this.childParsers = childParsers;
	}
	
	public Certidao parse(Documento doc) {
		// Para cada um dos parsers, tenta extrair uma certidão.
		Certidao certidao = null;
		for (IParserCertidao parser : childParsers) {
			certidao = parser.parse(doc);
			if (certidao != null) {
				certidao.setParser(parser);
				break;
			}
		}
		return certidao;
	}
	
	public void setNestedConfig(AbstractParserConfig config) {
		for (IParserCertidao parser : childParsers) {
			if (parser instanceof IParserConfigurable) {
				IParserConfigurable configurable = (IParserConfigurable)parser;
				configurable.setConfig(config);
			}

		}
	}
}
