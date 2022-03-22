package com.zstock.dto;

import java.util.List;

import lombok.Data;

@Data
public class ZstockDTO {
	private String symbol;
	private List<SingleDTO> historical;
}