package com.zdb.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.zdb.dto.SingleDTO;
import com.zdb.repository.StockRepository;

@Service
public class KafkaConsumerService {
	
	@Autowired
	StockRepository rep;
    
    @KafkaListener(topics = "json_topic", clientIdPrefix = "json", containerFactory = "userDTOFactory")
    public void listenAsObject(ConsumerRecord<String, SingleDTO> cr, @Payload SingleDTO payload) {
    	//System.out.println("=== ConsumerRecord");
    	if(!rep.isDateExist(payload.getSymbol(), payload.getDate()))
    		rep.save(payload);
    }
    
}
