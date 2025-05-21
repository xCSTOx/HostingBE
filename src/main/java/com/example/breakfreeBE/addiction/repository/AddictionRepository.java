package com.example.breakfreeBE.addiction.repository;

import com.example.breakfreeBE.addiction.entity.Addiction;
import com.example.breakfreeBE.addiction.entity.AddictionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddictionRepository extends JpaRepository<Addiction, AddictionId> {
    @Query("SELECT a FROM Addiction a WHERE a.userId = :userId")
    List<Addiction> findAddictionsByUserId(@Param("userId") String userId);

    @Query("SELECT a FROM Addiction a WHERE a.userId = :userId AND a.addictionId = :addictionId")
    Optional<Addiction> findByUserIdAndAddictionId(@Param("userId") String userId, @Param("addictionId") String addictionId);

    @Query("SELECT COUNT(a) FROM Addiction a JOIN a.user u WHERE u.userId = :userId")
    long countByUserId(String userId);

    boolean existsByUserId(String userId);

}