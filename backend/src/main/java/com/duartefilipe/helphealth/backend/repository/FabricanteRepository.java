package com.duartefilipe.helphealth.backend.repository;

import com.duartefilipe.helphealth.backend.model.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, String> {
}
