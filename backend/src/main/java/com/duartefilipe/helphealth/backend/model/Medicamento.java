package com.duartefilipe.helphealth.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "medicamentos")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 14)
    @Column(unique = true)
    private String ean;

    @NotNull
    @Size(max = 150)
    @Column(name = "nome_comercial", nullable = false)
    private String nomeComercial;

    @NotNull
    @Column(name = "principio_ativo", columnDefinition = "TEXT", nullable = false)
    private String principioAtivo;

    @Size(max = 100)
    private String concentracao;

    @Size(max = 100)
    @Column(name = "forma_farmaceutica")
    private String formaFarmaceutica;

    @Size(max = 50)
    @Column(name = "categoria_regulatoria")
    private String categoriaRegulatoria;

    @Size(max = 50)
    private String tarja;

    @Column(name = "retencao_receita")
    private Boolean retencaoReceita = false;

    @Column(name = "precisa_refrigeracao")
    private Boolean precisaRefrigeracao = false;

    @Column(name = "link_bula_paciente", columnDefinition = "TEXT")
    private String linkBulaPaciente;

    @Column(name = "faz_parte_farmacia_popular")
    private Boolean fazParteFarmaciaPopular = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnpj_fabricante", referencedColumnName = "cnpj")
    private Fabricante fabricante;

    @Size(max = 20)
    @Column(name = "status_registro")
    private String statusRegistro;

    public Medicamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEan() { return ean; }
    public void setEan(String ean) { this.ean = ean; }

    public String getNomeComercial() { return nomeComercial; }
    public void setNomeComercial(String nomeComercial) { this.nomeComercial = nomeComercial; }

    public String getPrincipioAtivo() { return principioAtivo; }
    public void setPrincipioAtivo(String principioAtivo) { this.principioAtivo = principioAtivo; }

    public String getConcentracao() { return concentracao; }
    public void setConcentracao(String concentracao) { this.concentracao = concentracao; }

    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }

    public String getCategoriaRegulatoria() { return categoriaRegulatoria; }
    public void setCategoriaRegulatoria(String categoriaRegulatoria) { this.categoriaRegulatoria = categoriaRegulatoria; }

    public String getTarja() { return tarja; }
    public void setTarja(String tarja) { this.tarja = tarja; }

    public Boolean getRetencaoReceita() { return retencaoReceita; }
    public void setRetencaoReceita(Boolean retencaoReceita) { this.retencaoReceita = retencaoReceita; }

    public Boolean getPrecisaRefrigeracao() { return precisaRefrigeracao; }
    public void setPrecisaRefrigeracao(Boolean precisaRefrigeracao) { this.precisaRefrigeracao = precisaRefrigeracao; }

    public String getLinkBulaPaciente() { return linkBulaPaciente; }
    public void setLinkBulaPaciente(String linkBulaPaciente) { this.linkBulaPaciente = linkBulaPaciente; }

    public Boolean getFazParteFarmaciaPopular() { return fazParteFarmaciaPopular; }
    public void setFazParteFarmaciaPopular(Boolean fazParteFarmaciaPopular) { this.fazParteFarmaciaPopular = fazParteFarmaciaPopular; }

    public Fabricante getFabricante() { return fabricante; }
    public void setFabricante(Fabricante fabricante) { this.fabricante = fabricante; }

    public String getStatusRegistro() { return statusRegistro; }
    public void setStatusRegistro(String statusRegistro) { this.statusRegistro = statusRegistro; }
}
