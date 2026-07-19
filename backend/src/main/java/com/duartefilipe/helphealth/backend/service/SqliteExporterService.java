package com.duartefilipe.helphealth.backend.service;

import com.duartefilipe.helphealth.backend.model.Fabricante;
import com.duartefilipe.helphealth.backend.model.Medicamento;
import com.duartefilipe.helphealth.backend.model.PrecoCmed;
import com.duartefilipe.helphealth.backend.repository.FabricanteRepository;
import com.duartefilipe.helphealth.backend.repository.MedicamentoRepository;
import com.duartefilipe.helphealth.backend.repository.PrecoCmedRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Service
public class SqliteExporterService {

    private final FabricanteRepository fabricanteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final PrecoCmedRepository precoCmedRepository;

    @Value("${helphealth.sqlite.db-path:./anvisa_app_database.db}")
    private String dbPath;

    public SqliteExporterService(FabricanteRepository fabricanteRepository,
                                 MedicamentoRepository medicamentoRepository,
                                 PrecoCmedRepository precoCmedRepository) {
        this.fabricanteRepository = fabricanteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.precoCmedRepository = precoCmedRepository;
    }

    @Transactional(readOnly = true)
    public File generateCompressedSqliteDatabase() throws Exception {
        File sqliteFile = new File(dbPath);
        if (sqliteFile.exists()) {
            sqliteFile.delete();
        }

        String sqliteUrl = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(sqliteUrl)) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE fabricantes (
                        cnpj TEXT PRIMARY KEY,
                        razao_social TEXT NOT NULL,
                        nome_fantasia TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE medicamentos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ean TEXT UNIQUE,
                        nome_comercial TEXT NOT NULL,
                        principio_ativo TEXT NOT NULL,
                        concentracao TEXT,
                        forma_farmaceutica TEXT,
                        categoria_regulatoria TEXT,
                        tarja TEXT,
                        retencao_receita INTEGER DEFAULT 0,
                        precisa_refrigeracao INTEGER DEFAULT 0,
                        link_bula_paciente TEXT,
                        faz_parte_farmacia_popular INTEGER DEFAULT 0,
                        cnpj_fabricante TEXT,
                        status_registro TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE precos_cmed (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ean TEXT,
                        uf TEXT NOT NULL,
                        pmc_zero_icms REAL,
                        pmc_18_icms REAL
                    );
                """);

                stmt.execute("CREATE INDEX idx_med_busca ON medicamentos (principio_ativo, concentracao, forma_farmaceutica);");
                stmt.execute("CREATE INDEX idx_med_ean ON medicamentos (ean);");
            }

            // Insert Fabricantes
            List<Fabricante> fabricantes = fabricanteRepository.findAll();
            String sqlFab = "INSERT INTO fabricantes (cnpj, razao_social, nome_fantasia) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlFab)) {
                for (Fabricante fab : fabricantes) {
                    ps.setString(1, fab.getCnpj());
                    ps.setString(2, fab.getRazaoSocial());
                    ps.setString(3, fab.getNomeFantasia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Insert Medicamentos
            List<Medicamento> medicamentos = medicamentoRepository.findAll();
            String sqlMed = """
                INSERT INTO medicamentos (ean, nome_comercial, principio_ativo, concentracao, forma_farmaceutica,
                categoria_regulatoria, tarja, retencao_receita, precisa_refrigeracao, link_bula_paciente,
                faz_parte_farmacia_popular, cnpj_fabricante, status_registro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlMed)) {
                for (Medicamento med : medicamentos) {
                    ps.setString(1, med.getEan());
                    ps.setString(2, med.getNomeComercial());
                    ps.setString(3, med.getPrincipioAtivo());
                    ps.setString(4, med.getConcentracao());
                    ps.setString(5, med.getFormaFarmaceutica());
                    ps.setString(6, med.getCategoriaRegulatoria());
                    ps.setString(7, med.getTarja());
                    ps.setInt(8, Boolean.TRUE.equals(med.getRetencaoReceita()) ? 1 : 0);
                    ps.setInt(9, Boolean.TRUE.equals(med.getPrecisaRefrigeracao()) ? 1 : 0);
                    ps.setString(10, med.getLinkBulaPaciente());
                    ps.setInt(11, Boolean.TRUE.equals(med.getFazParteFarmaciaPopular()) ? 1 : 0);
                    ps.setString(12, med.getFabricante() != null ? med.getFabricante().getCnpj() : null);
                    ps.setString(13, med.getStatusRegistro());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Insert Precos CMED
            List<PrecoCmed> precos = precoCmedRepository.findAll();
            String sqlPreco = "INSERT INTO precos_cmed (ean, uf, pmc_zero_icms, pmc_18_icms) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPreco)) {
                for (PrecoCmed p : precos) {
                    ps.setString(1, p.getMedicamento() != null ? p.getMedicamento().getEan() : null);
                    ps.setString(2, p.getUf());
                    ps.setObject(3, p.getPmcZeroIcms() != null ? p.getPmcZeroIcms().doubleValue() : null);
                    ps.setObject(4, p.getPmc18Icms() != null ? p.getPmc18Icms().doubleValue() : null);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        }

        // Compress file using GZIP
        File gzippedFile = new File(dbPath + ".gz");
        try (InputStream in = new FileInputStream(sqliteFile);
             GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzippedFile))) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        return gzippedFile;
    }
}
