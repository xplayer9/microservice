package com.aaaUserApi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aaaUserApi.model.aaaUserModel;

@Repository
public interface aaaUserRepository extends JpaRepository<aaaUserModel, Long> {

	Optional<aaaUserModel> findByUsername(String username);
	
	@Query(value="select exists(select 1 from users where username = :name)", nativeQuery=true)
    Boolean existByName(String name);
	
	//@Query(value="select * from inventory a where a.item = :item", nativeQuery=true)
    //List<shopModel> getItem(String item);
}