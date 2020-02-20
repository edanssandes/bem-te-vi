package bemtevi.model.campos;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Campos da certidão que possuem formato de data.
 * @author edans
 */
public class CampoData implements ICampoCertidao {
	/**
	 * Objeto java para armazenar a data
	 */
	private Date date;
	
	/**
	 * Tipo de formato do texto original
	 */
	private int tipoFormato;
	
	/**
	 * Texto original
	 */
	private String dateStr;
	
	/* Formatos aceitos para o campo de data */
	
	private static final DateFormat formatDateTimeSec = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DateFormat formatDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final DateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat formatTime = new SimpleDateFormat("HH:mm");
	private static final DateFormat formatTimeSec = new SimpleDateFormat("HH:mm:ss");
	private static final DateFormat formatDateISO = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
	
	private static final DateFormat[] formats = {formatDateTimeSec, formatDateTime, formatDate};

	/**
	 * Cria um campo de data copiando os valores de outro campo.
	 * @param date campo original que será copiado.
	 */
	public CampoData(CampoData date) {
		this.date = date.date;
		this.tipoFormato = date.tipoFormato;
		this.dateStr = date.dateStr;
	}	
	
	/**
	 * Cria um campo de data a partir de um objeto de data Java.
	 * @param date
	 */
	private CampoData(Date date) {
		this.date = date;
		this.tipoFormato = 2;
	}
	
	/**
	 * Cria um campo de data a partir de duas strings.
	 * @param strDate string de data
	 * @param strTime string de hora
	 */
	public CampoData(String strDate, String strTime) {
		this(strDate + " " + strTime);
	}

	/**
	 * Cria um campo de data a partir de uma string. 
	 * Vários formatos são aceitos para o campo data.
	 * 
	 * @param dateStr string contendo a data.
	 */
	public CampoData(String dateStr) {
		this.dateStr = dateStr;
		
		date = null;
		for (int i = 0; i < formats.length; i++) {
			try {
				date = formats[i].parse(dateStr);
				tipoFormato = i;
				break;
			} catch (ParseException e) {
				// Ignora
			}
		}
	}

	/**
	 * Adiciona um número específico de dias na data.
	 * @param days número de dias a ser adicionado.
	 * @return campo com dias a mais.
	 */
	public CampoData addDate(int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days); 
		return new CampoData(c.getTime());
	}	
	
	/**
	 * Compara o objeto atual com uma outra data.
	 * @param data data a ser comparada.
	 * @return 0 se for igual, inteiro positivo se for maior, inteiro negativo se for menor.
	 */
	public int compareTo(CampoData data) {
		return compareTo(data.date);
	}
	
	/**
	 * Compara o objeto atual com um data java.
	 * @param data data a ser comparada.
	 * @return 0 se for igual, inteiro positivo se for maior, inteiro negativo se for menor.
	 */
	public int compareTo(Date date) {
		if (this.date == null) {
			return 0;
		}
		return this.date.compareTo(date);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (date == null) {
			return dateStr;
		} else {
			return formats[tipoFormato].format(date);
		}
	}

	/**
	 * Retorna uma string com a representação do horário no formato HH:mm:ss.
	 * @return string representando o horário.
	 */
	public String getHoraSec() {
		return formatTimeSec.format(date);	
	}
	
	/**
	 * Retorna uma string com a representação do horário no formato HH:mm.
	 * @return string representando o horário.
	 */
	public String getHora() {
		return formatTime.format(date);	
	}

	/**
	 * Retorna uma string com a representação do dia no formato dd/MM/yyyy.
	 * @return string representando o dia.
	 */
	public String getDia() {
		return formatDate.format(date);	
	}
	
	/**
	 * Retorna uma string com a representação do dia no formato ISO-8601: yyyy-MM-ddT00:00:00.
	 * @return string representando o dia.
	 */
	public String getDiaISO8601() {
		return formatDateISO.format(date);	
	}	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CampoData)) {
			return false;
		}
		CampoData campo = (CampoData) obj;
		return date.equals(campo.date)
				&& tipoFormato == campo.tipoFormato;
	}
}
