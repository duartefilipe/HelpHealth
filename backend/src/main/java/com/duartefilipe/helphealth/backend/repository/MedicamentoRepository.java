package com.duartefilipe.helphealth.backend.repository;

import com.duartefilipe.helphealth.backend.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByEan(String ean);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT m.ean, m.nome_comercial as nomeComercial, m.principio_ativo as principioAtivo, " +
                "m.concentracao, m.forma_farmaceutica as formaFarmaceutica, m.categoria_regulatoria as categoriaRegulatoria, " +
                "m.tarja, m.retencao_receita as retencaoReceita, m.precisa_refrigeracao as precisaRefrigeracao, " +
                "m.link_bula_paciente as linkBulaPaciente, m.faz_parte_farmacia_popular as fazParteFarmaciaPopular, " +
                "f.cnpj as cnpjFabricante, f.razao_social as razaoSocial, " +
                "p_sp.pmc_zero_icms as pmcZeroSp, p_sp.pmc_18_icms as pmc18Sp, " +
                "p_rs.pmc_zero_icms as pmcZeroRs, p_rs.pmc_18_icms as pmc18Rs " +
                "FROM medicamentos m " +
                "LEFT JOIN fabricantes f ON m.cnpj_fabricante = f.cnpj " +
                "LEFT JOIN precos_cmed p_sp ON m.ean = p_sp.ean AND p_sp.uf = 'SP' " +
                "LEFT JOIN precos_cmed p_rs ON m.ean = p_rs.ean AND p_rs.uf = 'RS'",
        countQuery = "SELECT count(*) FROM medicamentos",
        nativeQuery = true)
    org.springframework.data.domain.Page<MedicamentoFlat> findAllFlat(org.springframework.data.domain.Pageable pageable);
}
