package com.duartefilipe.helphealth.backend.service;

import com.duartefilipe.helphealth.backend.model.Fabricante;
import com.duartefilipe.helphealth.backend.model.Medicamento;
import com.duartefilipe.helphealth.backend.model.PrecoCmed;
import com.duartefilipe.helphealth.backend.repository.FabricanteRepository;
import com.duartefilipe.helphealth.backend.repository.MedicamentoRepository;
import com.duartefilipe.helphealth.backend.repository.PrecoCmedRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

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

    public void processAnvisaMedicamentosCsv(InputStream csvInputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .withSkipLines(1) // Ignora cabeçalho
                .build()) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 5) continue;

                String principioAtivo = sanitizeString(line[0]);
                String cnpj = sanitizeString(line[1]);
                String razaoSocial = sanitizeString(line[2]);
                String ean = sanitizeString(line[3]);
                String nomeComercial = sanitizeString(line[4]);
                String apresentacao = line.length > 5 ? sanitizeString(line[5]) : null;
                String categoriaRegulatoria = line.length > 7 ? sanitizeString(line[7]) : "SIMILAR";
                String tarja = line.length > 8 ? sanitizeString(line[8]) : "ISENTO";
                BigDecimal pmcZero = line.length > 9 ? parseBigDecimal(line[9]) : BigDecimal.ZERO;
                BigDecimal pmc18 = line.length > 10 ? parseBigDecimal(line[10]) : BigDecimal.ZERO;

                if (cnpj == null || cnpj.isEmpty()) {
                    cnpj = "00000000000000";
                }

                final String finalCnpj = cnpj;
                final String finalRazaoSocial = razaoSocial != null ? razaoSocial : "LABORATORIO OFICIAL ANVISA";

                Fabricante fabricante = fabricanteRepository.findById(finalCnpj)
                        .orElseGet(() -> fabricanteRepository.save(new Fabricante(finalCnpj, finalRazaoSocial, finalRazaoSocial)));

                if (nomeComercial != null && !nomeComercial.isEmpty()) {
                    String cleanEan = (ean != null && !ean.isEmpty() && !ean.equals("nan")) ? ean : "EAN_" + System.nanoTime();

                    Optional<Medicamento> existingMed = medicamentoRepository.findByEan(cleanEan);
                    Medicamento med = existingMed.orElseGet(Medicamento::new);
                    med.setEan(cleanEan);
                    med.setNomeComercial(nomeComercial);
                    med.setPrincipioAtivo(principioAtivo != null ? principioAtivo : nomeComercial);
                    med.setConcentracao(apresentacao);
                    med.setFormaFarmaceutica(apresentacao);
                    med.setCategoriaRegulatoria(categoriaRegulatoria != null ? categoriaRegulatoria : "SIMILAR");
                    med.setTarja(tarja != null ? tarja : "ISENTO");
                    med.setFabricante(fabricante);
                    med.setStatusRegistro("ATIVO");

                    medicamentoRepository.save(med);

                    // Cadastra preços estaduais para RS e SP
                    if (pmc18.compareTo(BigDecimal.ZERO) > 0 || pmcZero.compareTo(BigDecimal.ZERO) > 0) {
                        PrecoCmed precoRs = new PrecoCmed();
                        precoRs.setMedicamento(med);
                        precoRs.setUf("RS");
                        precoRs.setPmcZeroIcms(pmcZero);
                        precoRs.setPmc18Icms(pmc18);
                        precoCmedRepository.save(precoRs);

                        PrecoCmed precoSp = new PrecoCmed();
                        precoSp.setMedicamento(med);
                        precoSp.setUf("SP");
                        precoSp.setPmcZeroIcms(pmcZero);
                        precoSp.setPmc18Icms(pmc18);
                        precoCmedRepository.save(precoSp);
                    }
                }
            }
        }
    }

    private String sanitizeString(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        return (trimmed.isEmpty() || trimmed.equalsIgnoreCase("nan") || trimmed.equalsIgnoreCase("null")) ? null : trimmed;
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
