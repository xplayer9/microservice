package com.zdb.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.zdb.dto.SingleDTO;

@EnableKafka
@Configuration
public class KafkaConsumerConfiguration {

    public static final String GROUP_1 = "group_1";
    public static final String GROUP_2 = "group_2";
    
	@Value("${my.kafkatopic}")
	private String TOPIC;
	
	@Value("${my.kafkauri}")
	private String SERVER;

	// UserDTO consumer
    @Bean
    public ConsumerFactory<String, SingleDTO> userDTOConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_2);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
     // use ErrorHandlingDeserializer
        ErrorHandlingDeserializer<SingleDTO> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(SingleDTO.class));
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SingleDTO> userDTOFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SingleDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userDTOConfig());
        return factory;
    }
}
