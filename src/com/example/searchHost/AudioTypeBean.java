package com.example.searchHost;

public class AudioTypeBean {
	private String audioType;
	private int    audioValue;
	private int    audioMaxValue;
	
	
	public AudioTypeBean(String audioType, int audioValue, int audioMaxValue) {
		this.audioType = audioType;
		this.audioValue = audioValue;
		this.audioMaxValue = audioMaxValue;
	}
	
	public void setAudioType(String audioType) {
		this.audioType = audioType;
	}
	
	public String getAudioType() {
		return this.audioType;
	}
	
	public void setAudioVaule(int audioValue) {
		this.audioValue = audioValue;
	}
	
	public int getAudioVaule() {
		return this.audioValue;
	}
	
	public void setAudioMaxVaule(int audioMaxValue) {
		this.audioMaxValue = audioMaxValue;
	}
	
	public int getAudioMaxValue() {
		return this.audioMaxValue;
	}
}

