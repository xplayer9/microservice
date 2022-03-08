package com.aaaTradeApi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="`tradeTable`")
public class aaaTradeModel {
	
	@Id
	@Column(name="name")
    private String name;
}
