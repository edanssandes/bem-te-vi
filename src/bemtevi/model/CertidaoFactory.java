package bemtevi.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import bemtevi.configs.PreferenciaParser;
import bemtevi.configs.PreferenciasPerfil;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.parsers.NestedParserCertidao;
import bemtevi.parsers.ParserSingletons;


/**
 * Classe que processa um arquivo e gera objetos do tipo {@link Certidao}.
 * A ordem de processamento é a seguinte:
 * <ol>
 * <li>Conversão de arquivo para texto: criação do objeto {@link Documento}</li>
 * <li>Para cada analisador de certidão (parser), tenta-se converter um
 * {@link Documento} em {@link Certidao}. Se nenhum parser for capaz de realizar
 * a conversão, cria-se uma certidão inválida por meio do parser default.
 * </ol>
 * 
 * Obs. A lista de analisadores de certidão válidos é definda por meio 
 * de um objeto {@link PreferenciasPerfil 
 * (ver {@link #setPerfil(PreferenciasPerfil)}).
 *  
 * @author edans
 *
 */
public class CertidaoFactory {
	
	private static IParserCertidao defaultParser = ParserSingletons.getDefaultParsers();
	private static IParserCertidao[] parsers;
	//private static Tesseract instance;

	/**
	 * Define quais parsers estarão ativos no momento da conversão dos documentos.
	 * 
	 * @param preferenciasPerfil objeto contendo os parsers ativos e suas configurações.
	 */
	public static void setPerfil(PreferenciasPerfil preferenciasPerfil) {
		/*
		 * Seleciona apenas os analisadores (parsers) ativos de um determinado
		 * perfil e ativa as configurações associadas.
		 */
		ArrayList<IParserCertidao> array = new ArrayList<IParserCertidao>();
		for (PreferenciaParser preferencia : preferenciasPerfil.getPreferenciasAtivas()) {
			IParserCertidao parser = preferencia.getParser();
			array.add(parser);
			if (parser instanceof IParserConfigurable) {
				IParserConfigurable configurable = (IParserConfigurable)parser;
				configurable.setConfig(preferencia.getConfig());
			}
		}
		parsers = new IParserCertidao[array.size()];
		array.toArray(parsers);
	}	
	
	/**
	 * Converte um arquivo em Certidão utilizando os parsers defindiso pela função
	 * {@link #setPerfil(PreferenciasPerfil)}.
	 * 
	 * @param file arquivo a ser convertido.
	 * @return objeto {@link Certidao}.
	 * @throws IOException em caso de erro de IO.
	 */
	public static Certidao parse(File file) throws IOException {
		return parse(file, parsers);
	}

	/**
	 * Converte um arquivo em Certidão utilizando um parser específico.
	 * 
	 * @param file arquivo a ser convertido.
	 * @param parser analisador de certidões a ser utilizado para esta conversão.
	 * @return objeto {@link Certidao}.
	 * @throws IOException em caso de erro de IO.
	 */
	public static Certidao parse(File file, IParserCertidao parser) throws IOException {
		return parse(file, new IParserCertidao[] {parser});
	}

	/**
	 * Converte um arquivo em Certidão utilizando uma lista de parsers.
	 * 
	 * @param file arquivo a ser convertido.
	 * @param parsers lista de analisadores de certidões.
	 * @return objeto {@link Certidao}.
	 * @throws IOException em caso de erro de IO.
	 */
	private static Certidao parse(File file, IParserCertidao[] parsers) throws IOException {
		Documento doc = Documento.readPDF(file);
		Certidao certidao = CertidaoFactory.parse(doc, parsers);
		certidao.setDocumento(doc);
		return certidao;
	}
	
	private static Certidao parse(Documento doc, IParserCertidao[] parsers) throws IOException {
		// Para cada um dos parsers, tenta extrair uma certidão.
		Certidao certidao = null;
		int parserId = 0;
		for (IParserCertidao parser : parsers) {
			certidao = parser.parse(doc);
			if (certidao != null) {
				if (!(parser instanceof NestedParserCertidao)) { // Nested Parser are already set 
					certidao.setParser(parser);
				}
				certidao.setParserId(parserId);
				break;
			}
			parserId++;
		}
		if (certidao == null) {
			certidao = defaultParser.parse(doc);
			certidao.setParser(defaultParser);
			certidao.setParserId(Integer.MAX_VALUE);
		}
		return certidao;
	}

	private static Certidao parseOCR(Certidao certidaoInv) throws IOException {
		return parseOCR(certidaoInv, parsers);
	}	
	
	private static Certidao parseOCR(Certidao certidaoInv, IParserCertidao parser) throws IOException {
		return parseOCR(certidaoInv, new IParserCertidao[] {parser});
	}	
	
	private static Certidao parseOCR(Certidao certidaoInv, IParserCertidao[] parsers) throws IOException {
		Documento doc = certidaoInv.getDocumento();
		doc.doOCR();
		Certidao certidao = CertidaoFactory.parse(doc, parsers);
		certidao.setDocumento(doc);
		return certidao;
	}
	

}
