package com.zdb.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zdb.dto.SingleDTO;
import com.zdb.repository.StockRepository;

@RestController
public class ZdbRestController {
	
	@Value("${my.kafkatopic}")
	private String TOPIC;
	
	@Value("${my.kafkauri}")
	private String SERVER;
	
	@Autowired
	StockRepository rep;
	
	@Autowired
    private KafkaTemplate<String, Object> kafkaTemp;
	
	private DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@PostMapping("/findsymbol")
	public Boolean getFindSymbol(@RequestBody String sym) {
		System.out.println("=== getFindSymbol "+sym);
		return rep.isSymbolExist(sym);
	}
	
	@PostMapping("/listsymbol")
	public Object[] getListSymbol(@RequestBody String sym) {
		System.out.println("=== getListSymbol "+sym);
		//Pageable sortedByDate = PageRequest.of(0, 3, Sort.by("date").ascending());
		return rep.findBySymbolAscding(sym).toArray();
	}
	
	@PostMapping("/userstock")
	public Object[] getUserStock(@RequestBody String username) {
		//System.out.println("=== getUserStock "+ username);
		return rep.findUserStock(username).toArray();
	}
	
	@PostMapping("/saveHistoricalData")
	public Boolean saveHistoricalData(@RequestBody Object[] arry) {
		for(Object obj:arry) {
			try {
				//System.out.println(obj.toString());
				Map<String, String> map = (LinkedHashMap)obj;
				SingleDTO dto = new SingleDTO();
				dto.setSymbol(map.get("symbol"));
				dto.setDate(LocalDate.parse(map.get("date"), dt));
				dto.setVolume(map.get("volume"));
				dto.setClose(Double.parseDouble(map.get("close")));
				dto.setHigh(Double.parseDouble(map.get("high")));
				dto.setLow(Double.parseDouble(map.get("low")));
				//rep.save(dto);
				kafkaTemp.send(TOPIC, dto);
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	@PostMapping("/isDateRangeInDB")
	public Boolean checkDateRange(@RequestBody String str) {
		String[] arry = str.split(":");
		String sym = arry[0];
		try {
			LocalDate from_date = LocalDate.parse(arry[1], dt);
			LocalDate to_date = LocalDate.parse(arry[2], dt);
			LocalDate from_date_db = rep.findFromDate(sym);
			LocalDate to_date_db = rep.findToDate(sym);
			if(from_date.isBefore(from_date_db) || to_date.isAfter(to_date_db))
				return false;
		}
		catch(Exception e) {
			System.out.println("=== isDateRangeInDB: LocalDate error");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@PostMapping("/getDataFromDB")
	public Object[] getDataFromDB(@RequestBody String str) {
		String[] arry = str.split(":");
		String sym = arry[0];
		System.out.println("=== getDataFromDB "+ sym + " " + arry[1] + " " + arry[2]); 
		List<SingleDTO> ll = rep.findAllWithinDate(sym, LocalDate.parse(arry[1], dt), LocalDate.parse(arry[2], dt));
		return ll.toArray();
	}
}






