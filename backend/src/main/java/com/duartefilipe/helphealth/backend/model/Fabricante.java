package com.duartefilipe.helphealth.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fabricantes")
public class Fabricante {

    @Id
    @Column(name = "cnpj", length = 50)
    private String cnpj;

    @Column(name = "razao_social", columnDefinition = "TEXT")
    private String razaoSocial;

    @Column(name = "nome_fantasia", columnDefinition = "TEXT")
    private String nomeFantasia;

    public Fabricante() {}

    public Fabricante(String cnpj, String razaoSocial, String nomeFantasia) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
}
