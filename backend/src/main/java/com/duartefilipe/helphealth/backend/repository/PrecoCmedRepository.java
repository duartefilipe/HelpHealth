package com.duartefilipe.helphealth.backend.repository;

import com.duartefilipe.helphealth.backend.model.PrecoCmed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrecoCmedRepository extends JpaRepository<PrecoCmed, Long> {
    List<PrecoCmed> findByMedicamentoEan(String ean);
}
