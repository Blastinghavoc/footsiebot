package footsiebot.datagathering;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class Article implements Comparable<Article> {
	private String headline;
	private String url;
	private String digest;
	private LocalDateTime pubDate;

	public Article(String headline, String url, String digest, LocalDateTime pubDate) {
		this.headline = headline;
		this.url = url;
		this.digest = digest;
		this.pubDate = pubDate;
	}

	/**
	* Returns value of headline
	* @return
	*/
	public String getHeadline() {
		return headline;
	}

	/**
	* Sets new value of headline
	* @param
	*/
	public void setHeadline(String headline) {
		this.headline = headline;
	}

	/**
	* Returns value of url
	* @return
	*/
	public String getUrl() {
		return url;
	}

	/**
	* Sets new value of url
	* @param
	*/
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	* Returns value of digest
	* @return
	*/
	public String getDigest() {
		return digest;
	}

	/**
	* Sets new value of digest
	* @param
	*/
	public void setDigest(String digest) {
		this.digest = digest;
	}

	/**
	* Returns value of pubDate
	* @return
	*/
	public LocalDateTime getPubDate() {
		return pubDate;
	}

	/**
	* Sets new value of pubDate
	* @param
	*/
	public void setPubDate(LocalDateTime pubDate) {
		this.pubDate = pubDate;
	}

	public String getPubDateAsString() {
		String dateString = "";
		dateString += pubDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " ";
		dateString += pubDate.getDayOfMonth() + " ";
		dateString += pubDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " ";
		dateString += pubDate.getYear() + " - ";
		dateString += String.format("%02d", pubDate.getHour()) + ":";
		dateString += String.format("%02d", pubDate.getMinute()) + ":";
		dateString += String.format("%02d", pubDate.getSecond());
		return dateString;
	}


	@Override
	public int compareTo(Article anotherArticle) {
    	return anotherArticle.getPubDate().compareTo(this.pubDate);
	}
}
