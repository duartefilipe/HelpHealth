package com.duartefilipe.helphealth.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicamentos")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ean", length = 50)
    private String ean;

    @Column(name = "nome_comercial", columnDefinition = "TEXT")
    private String nomeComercial;

    @Column(name = "principio_ativo", columnDefinition = "TEXT")
    private String principioAtivo;

    @Column(name = "concentracao", columnDefinition = "TEXT")
    private String concentracao;

    @Column(name = "forma_farmaceutica", columnDefinition = "TEXT")
    private String formaFarmaceutica;

    @Column(name = "categoria_regulatoria", length = 100)
    private String categoriaRegulatoria;

    @Column(name = "tarja", length = 100)
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

    @Column(name = "status_registro", length = 50)
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
