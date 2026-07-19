package com.duartefilipe.helphealth.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "precos_cmed")
public class PrecoCmed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ean", referencedColumnName = "ean")
    private Medicamento medicamento;

    @NotNull
    @Size(max = 2)
    @Column(nullable = false)
    private String uf;

    @Column(name = "pmc_zero_icms", precision = 10, scale = 2)
    private BigDecimal pmcZeroIcms;

    @Column(name = "pmc_18_icms", precision = 10, scale = 2)
    private BigDecimal pmc18Icms;

    public PrecoCmed() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public BigDecimal getPmcZeroIcms() { return pmcZeroIcms; }
    public void setPmcZeroIcms(BigDecimal pmcZeroIcms) { this.pmcZeroIcms = pmcZeroIcms; }

    public BigDecimal getPmc18Icms() { return pmc18Icms; }
    public void setPmc18Icms(BigDecimal pmc18Icms) { this.pmc18Icms = pmc18Icms; }
}
