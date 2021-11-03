package com.catlogging.message.h2;


public class LanguageEntity {

	private Integer id;
	private String locale;
	private String messagekey;
	private String messagecontent;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMessagekey() {
		return messagekey;
	}

	public void setMessagekey(String messagekey) {
		this.messagekey = messagekey;
	}

	public String getMessagecontent() {
		return messagecontent;
	}

	public void setMessagecontent(String messagecontent) {
		this.messagecontent = messagecontent;
	}
}
