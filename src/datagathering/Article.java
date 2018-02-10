package footsiebot.datagatheringcore;

public class Article {
  private String headline;
  private String url;
  private String digest;

  public Article() {
    
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
}
