package com.duartefilipe.helphealth.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AnvisaAutoUpdaterService {

    private static final Logger log = LoggerFactory.getLogger(AnvisaAutoUpdaterService.class);

    private final AnvisaCmedIngestionService ingestionService;
    private final SqliteExporterService sqliteExporterService;
    private final HttpClient httpClient;

    // URLs Oficiais de Dados Abertos da Anvisa / CMED
    private static final String ANVISA_MEDICAMENTOS_CSV_URL = "https://dados.anvisa.gov.br/dados/TA_CAMARA_REGULACAO_MEDICAMENTOS.csv";

    public AnvisaAutoUpdaterService(AnvisaCmedIngestionService ingestionService,
                                   SqliteExporterService sqliteExporterService) {
        this.ingestionService = ingestionService;
        this.sqliteExporterService = sqliteExporterService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Executado automaticamente todo dia 1º de cada mês às 03:00 da manhã.
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void scheduledMonthlyUpdate() {
        log.info("Iniciando rotina mensal automática de atualização com a Anvisa/CMED...");
        try {
            syncWithAnvisaOfficialSource();
        } catch (Exception e) {
            log.error("Erro durante a atualização mensal automática com a Anvisa: {}", e.getMessage(), e);
        }
    }

    public boolean syncWithAnvisaOfficialSource() {
        try {
            log.info("Baixando base de dados aberta da Anvisa em: {}", ANVISA_MEDICAMENTOS_CSV_URL);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANVISA_MEDICAMENTOS_CSV_URL))
                    .timeout(Duration.ofMinutes(5))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                log.info("Download concluído. Iniciando higienização e ingestão no PostgreSQL...");
                ingestionService.processAnvisaMedicamentosCsv(response.body());

                log.info("Ingestão concluída. Recompilando banco SQLite compactado para o app móvel...");
                sqliteExporterService.generateCompressedSqliteDatabase();
                log.info("Sincronização mensal concluída com sucesso!");
                return true;
            } else {
                log.warn("Falha ao obter dados da Anvisa. Código HTTP: {}", response.statusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Falha na sincronização direta com a Anvisa: {}", e.getMessage(), e);
            return false;
        }
    }
}
