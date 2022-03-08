package com.aaaTradeApi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aaaTradeApi.model.aaaUserModel;

@Repository
public interface aaaUserApiRepository extends JpaRepository<aaaUserModel, Long> {
    
    @Query(value="select * from users where username = :name", nativeQuery=true)
    List<aaaUserModel> findByUsername(String name);
	
}