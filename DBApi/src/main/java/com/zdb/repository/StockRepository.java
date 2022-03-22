package com.zdb.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zdb.dto.SingleDTO;

@Repository
public interface StockRepository extends JpaRepository<SingleDTO, Long> {

	List<SingleDTO> findBySymbol(String sym);
	
	@Query(value="select * from stockhistory WHERE symbol = :sym order by date asc", nativeQuery=true)
	List<SingleDTO> findBySymbolAscding(String sym);
	
	@Query(value="select exists(select 1 from stockhistory WHERE symbol = :sym AND date = :date)", nativeQuery=true)
    Boolean isDateExist(String sym, LocalDate date);
	
	@Query(value="select exists(select 1 from stocksymbol where name = :name)", nativeQuery=true)
    Boolean isSymbolExist(String name);
	
	@Query(value="select stock from userstock where name = :name", nativeQuery=true)
	List<String> findUserStock(String name);
	
	@Query(value="select date from stockhistory where symbol = :sym order by date asc limit 1", nativeQuery=true)
    LocalDate findFromDate(String sym);
	
	@Query(value="select date from stockhistory where symbol = :sym order by date desc limit 1", nativeQuery=true)
    LocalDate findToDate(String sym);
	
	@Query(value="select * from stockhistory WHERE symbol = :sym AND date >= :from AND date <= :to", nativeQuery=true)
	List<SingleDTO> findAllWithinDate(String sym, LocalDate from, LocalDate to);
	
	
	//@Query(value="select * from inventory a where a.item = :item", nativeQuery=true)
    //List<shopModel> getItem(String item);
}
