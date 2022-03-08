package com.aaaTradeApi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aaaTradeApi.model.aaaTradeModel;

@Repository
public interface aaaTradeRepository extends JpaRepository<aaaTradeModel, Long> {
	
	@Query(value="select * from tradetable where length(name) = 1", nativeQuery=true)
    List<aaaTradeModel> getAll();
  
}
