package bemtevi.parsers;

import bemtevi.parsers.caixa.fgts.ParserCertidaoCRF;
import bemtevi.parsers.df.rle.ParserCertidaoRLE;
import bemtevi.parsers.df.sefazdf.ParserCertidaoSefazDF;
import bemtevi.parsers.judiciario.tjdft.ParserCertidaoTJDFT;
import bemtevi.parsers.judiciario.trf1.ParserCertidaoTRF1;
import bemtevi.parsers.judiciario.trt10.ParserCertidaoTRT10;
import bemtevi.parsers.judiciario.tst.ParserCertidaoTST;
import bemtevi.parsers.receita.ParserCertidaoReceitaFederal;

/**
 * Classe contendo todos os parsers do aplicativo.
 * @author edans
 */
public class ParserSingletons {
	/**
	 * Lista de Parsers de Certidões
	 */
	private static final IParserCertidao[] parsers = { 
			new ParserCertidaoTJDFT(),
			new ParserCertidaoTRT10(), 
			new ParserCertidaoTRF1(),
			new ParserCertidaoTST(), 
			new ParserCertidaoReceitaFederal(),
			new ParserCertidaoCRF(), 
			new ParserCertidaoSefazDF(),
			new ParserCertidaoRLE(),
			};
	
	/**
	 * Parser default que representa uma certidão inválida.
	 */
	private static final IParserCertidao defaultParser = new ParserCertidaoInvalida();
	
	/**
	 * Obtém a lista de todos os parsers válidos.
	 * @return lista de todos os parsers válidos.
	 */
	public static IParserCertidao[] getParsers() {
		return parsers;
	}
	
	/**
	 * Obtém o parser default.
	 * @return parser default.
	 */
	public static IParserCertidao getDefaultParsers() {
		return defaultParser;
	}	
}
