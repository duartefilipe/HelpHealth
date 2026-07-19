package com.duartefilipe.helphealth.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "fabricantes")
public class Fabricante {

    @Id
    @Size(max = 14)
    private String cnpj;

    @NotNull
    @Size(max = 255)
    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Size(max = 255)
    @Column(name = "nome_fantasia")
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
