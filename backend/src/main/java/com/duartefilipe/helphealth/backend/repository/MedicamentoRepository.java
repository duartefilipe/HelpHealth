package com.duartefilipe.helphealth.backend.repository;

import com.duartefilipe.helphealth.backend.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByEan(String ean);
}
