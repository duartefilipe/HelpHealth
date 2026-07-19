package com.duartefilipe.helphealth.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class AnvisaAutoUpdaterService {

    private static final Logger log = LoggerFactory.getLogger(AnvisaAutoUpdaterService.class);

    private final AnvisaCmedIngestionService ingestionService;
    private final SqliteExporterService sqliteExporterService;
    private final AnvisaScraperService scraperService;

    public AnvisaAutoUpdaterService(AnvisaCmedIngestionService ingestionService,
                                   SqliteExporterService sqliteExporterService,
                                   AnvisaScraperService scraperService) {
        this.ingestionService = ingestionService;
        this.sqliteExporterService = sqliteExporterService;
        this.scraperService = scraperService;
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
            String datasetUrl = scraperService.discoverLatestCmedDatasetUrl();
            log.info("Iniciando download automático da tabela Anvisa CMED em: {}", datasetUrl);
            InputStream stream = scraperService.downloadDatasetStream(datasetUrl);

            log.info("Download concluído. Processando ingestão das tabelas da Anvisa no PostgreSQL...");
            ingestionService.processAnvisaMedicamentosCsv(stream);

            log.info("Ingestão concluída. Recompilando banco SQLite compactado para o aplicativo móvel...");
            sqliteExporterService.generateCompressedSqliteDatabase();
            log.info("Sincronização com a Anvisa concluída com sucesso!");
            return true;
        } catch (Exception e) {
            log.warn("Tentativa de download automático falhou: {}. Recompilando base atual para garantia.", e.getMessage());
            try {
                sqliteExporterService.generateCompressedSqliteDatabase();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
