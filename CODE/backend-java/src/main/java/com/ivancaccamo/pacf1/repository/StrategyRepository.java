package com.ivancaccamo.pacf1.repository;

import com.ivancaccamo.pacf1.model.SavedStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Questa interfaccia ci da i metodi .save(), .findAll(), .delete()...
@Repository
public interface StrategyRepository extends JpaRepository<SavedStrategy, Long> {
}