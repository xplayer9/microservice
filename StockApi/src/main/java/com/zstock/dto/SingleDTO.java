package com.zstock.dto;

import lombok.Data;

@Data
public class SingleDTO {

	private String symbol;
	  private String date;
	  private String open;
	  private String high;
	  private String low;
	  private String close;
	  private String adjClose;
	  private String volume;
	  private String unadjustedVolume;
	  private String change;
	  private String changePercent;
	  private String vwap;
	  private String label;
	  private String changeOverTime;
}
