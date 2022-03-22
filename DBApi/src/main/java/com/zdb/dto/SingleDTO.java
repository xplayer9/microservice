package com.zdb.dto;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;
import lombok.Data;

@Entity
@Data
@Table(name="`stockhistory`")
public class SingleDTO {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;
	
	@Column(name="symbol")
	private String symbol;

	@Column(name="date")
	private LocalDate date;
	
	@Column(name="close")
	private Double close;
	
	@Column(name="high")
	private Double high;
	
	@Column(name="low")
	private Double low;
	
	@Column(name="volume")
	private String volume;
	  
	  @Transient
	  private String open;
	  @Transient
	  private String adjClose;
	  @Transient
	  private String unadjustedVolume;
	  @Transient
	  private String change;
	  @Transient
	  private String changePercent;
	  @Transient
	  private String vwap;
	  @Transient
	  private String label;
	  @Transient
	  private String changeOverTime;
}
