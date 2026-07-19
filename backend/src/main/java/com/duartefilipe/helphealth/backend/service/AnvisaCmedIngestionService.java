package com.duartefilipe.helphealth.backend.service;

import com.duartefilipe.helphealth.backend.model.Fabricante;
import com.duartefilipe.helphealth.backend.model.Medicamento;
import com.duartefilipe.helphealth.backend.model.PrecoCmed;
import com.duartefilipe.helphealth.backend.repository.FabricanteRepository;
import com.duartefilipe.helphealth.backend.repository.MedicamentoRepository;
import com.duartefilipe.helphealth.backend.repository.PrecoCmedRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class AnvisaCmedIngestionService {

    private final FabricanteRepository fabricanteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final PrecoCmedRepository precoCmedRepository;

    public AnvisaCmedIngestionService(FabricanteRepository fabricanteRepository,
                                      MedicamentoRepository medicamentoRepository,
                                      PrecoCmedRepository precoCmedRepository) {
        this.fabricanteRepository = fabricanteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.precoCmedRepository = precoCmedRepository;
    }

    @Transactional
    public void processAnvisaMedicamentosCsv(InputStream csvInputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8))
                .withSkipLines(1) // Ignora cabeçalho
                .build()) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 13) continue;

                String cnpj = sanitizeString(line[0]);
                String razaoSocial = sanitizeString(line[1]);
                String nomeFantasia = sanitizeString(line[2]);
                String ean = sanitizeString(line[3]);
                String nomeComercial = sanitizeString(line[4]);
                String principioAtivo = sanitizeString(line[5]);
                String concentracao = sanitizeString(line[6]);
                String formaFarmaceutica = sanitizeString(line[7]);
                String categoriaRegulatoria = sanitizeString(line[8]);
                String tarja = sanitizeString(line[9]);
                boolean retencaoReceita = parseBoolean(line[10]);
                boolean precisaRefrigeracao = parseBoolean(line[11]);
                String linkBula = sanitizeString(line[12]);

                if (cnpj != null && !cnpj.isEmpty()) {
                    Fabricante fabricante = fabricanteRepository.findById(cnpj)
                            .orElseGet(() -> fabricanteRepository.save(new Fabricante(cnpj, razaoSocial, nomeFantasia)));

                    if (ean != null && !ean.isEmpty()) {
                        Optional<Medicamento> existingMed = medicamentoRepository.findByEan(ean);
                        Medicamento med = existingMed.orElseGet(Medicamento::new);
                        med.setEan(ean);
                        med.setNomeComercial(nomeComercial != null ? nomeComercial : "DESCONHECIDO");
                        med.setPrincipioAtivo(principioAtivo != null ? principioAtivo : "NAO INFORMADO");
                        med.setConcentracao(concentracao);
                        med.setFormaFarmaceutica(formaFarmaceutica);
                        med.setCategoriaRegulatoria(categoriaRegulatoria);
                        med.setTarja(tarja);
                        med.setRetencaoReceita(retencaoReceita);
                        med.setPrecisaRefrigeracao(precisaRefrigeracao);
                        med.setLinkBulaPaciente(linkBula);
                        med.setFabricante(fabricante);
                        med.setStatusRegistro("ATIVO");

                        medicamentoRepository.save(med);
                    }
                }
            }
        }
    }

    @Transactional
    public void processCmedPrecosCsv(InputStream csvInputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8))
                .withSkipLines(1)
                .build()) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 4) continue;

                String ean = sanitizeString(line[0]);
                String uf = sanitizeString(line[1]);
                BigDecimal pmcZero = parseBigDecimal(line[2]);
                BigDecimal pmc18 = parseBigDecimal(line[3]);

                if (ean != null && uf != null) {
                    Optional<Medicamento> medOpt = medicamentoRepository.findByEan(ean);
                    if (medOpt.isPresent()) {
                        PrecoCmed preco = new PrecoCmed();
                        preco.setMedicamento(medOpt.get());
                        preco.setUf(uf.toUpperCase());
                        preco.setPmcZeroIcms(pmcZero);
                        preco.setPmc18Icms(pmc18);
                        precoCmedRepository.save(preco);
                    }
                }
            }
        }
    }

    private String sanitizeString(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean parseBoolean(String input) {
        if (input == null) return false;
        String val = input.trim().toLowerCase();
        return val.equals("sim") || val.equals("true") || val.equals("1") || val.equals("s");
    }

    private BigDecimal parseBigDecimal(String input) {
        if (input == null) return BigDecimal.ZERO;
        try {
            String sanitized = input.trim().replace(".", "").replace(",", ".");
            return new BigDecimal(sanitized);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
