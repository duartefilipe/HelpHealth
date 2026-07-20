package com.duartefilipe.helphealth.backend.repository;

public interface MedicamentoFlat {
    String getEan();
    String getNomeComercial();
    String getPrincipioAtivo();
    String getConcentracao();
    String getFormaFarmaceutica();
    String getCategoriaRegulatoria();
    String getTarja();
    Boolean getRetencaoReceita();
    Boolean getPrecisaRefrigeracao();
    String getLinkBulaPaciente();
    Boolean getFazParteFarmaciaPopular();
    String getCnpjFabricante();
    String getRazaoSocial();
    Double getPmcZeroSp();
    Double getPmc18Sp();
    Double getPmcZeroRs();
    Double getPmc18Rs();
}
