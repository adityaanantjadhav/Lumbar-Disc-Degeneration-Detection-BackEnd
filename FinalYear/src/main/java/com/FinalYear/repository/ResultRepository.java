package com.FinalYear.repository;

import com.FinalYear.Dto.ResultResponseDto;
import com.FinalYear.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ResultRepository extends JpaRepository<Result,Long> {


    Result findResultByResultId(Long resultId);



    @Query("SELECT new com.FinalYear.Dto.ResultResponseDto(r.resultId, r.imageName, r.timestamp) FROM Result r WHERE r.user.id = :id")
    List<ResultResponseDto> findByUserId(@Param("id") Long id);
}
