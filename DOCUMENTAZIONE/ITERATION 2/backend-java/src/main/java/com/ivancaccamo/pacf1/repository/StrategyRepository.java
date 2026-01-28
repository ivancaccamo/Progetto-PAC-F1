package com.ivancaccamo.pacf1.repository;

import com.ivancaccamo.pacf1.model.SavedStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaccia di persistenza per l'entità {@link SavedStrategy}.
 * <p>
 * Questo repository gestisce l'interazione con il database relazionale, astraendo
 * le operazioni SQL sottostanti. Estendendo {@link JpaRepository}, eredita automaticamente
 * un set completo di metodi CRUD (Create, Read, Update, Delete) standard, come
 * {@code save()}, {@code findAll()} e {@code deleteById()}, senza richiedere
 * alcuna implementazione manuale.
 * </p>
 *
 * @author Team SPS-F1
 */
// Questa interfaccia ci da i metodi .save(), .findAll(), .delete()...
@Repository
public interface StrategyRepository extends JpaRepository<SavedStrategy, Long> {
}