package com.duartefilipe.helphealth.backend.controller;

import com.duartefilipe.helphealth.backend.service.AnvisaAutoUpdaterService;
import com.duartefilipe.helphealth.backend.service.AnvisaCmedIngestionService;
import com.duartefilipe.helphealth.backend.service.SqliteExporterService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/database")
public class DatabaseSyncController {

    private final SqliteExporterService sqliteExporterService;
    private final AnvisaAutoUpdaterService autoUpdaterService;
    private final AnvisaCmedIngestionService ingestionService;

    public DatabaseSyncController(SqliteExporterService sqliteExporterService,
                                  AnvisaAutoUpdaterService autoUpdaterService,
                                  AnvisaCmedIngestionService ingestionService) {
        this.sqliteExporterService = sqliteExporterService;
        this.autoUpdaterService = autoUpdaterService;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/sync-now")
    public ResponseEntity<Map<String, Object>> syncNow() {
        boolean success = autoUpdaterService.syncWithAnvisaOfficialSource();
        if (success) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Base de dados sincronizada com a Anvisa e SQLite recompilado!"));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", "Falha ao baixar/sincronizar dados da Anvisa."));
        }
    }

    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            ingestionService.processAnvisaMedicamentosCsv(file.getInputStream());
            sqliteExporterService.generateCompressedSqliteDatabase();
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "CSV da Anvisa ingerido no PostgreSQL e SQLite recompilado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", "Erro ao processar arquivo CSV: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDatabase() {
        try {
            File compressedDb = sqliteExporterService.generateCompressedSqliteDatabase();
            Resource resource = new FileSystemResource(compressedDb);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"anvisa_app_database.db.gz\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(compressedDb.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getDatabaseVersion() {
        try {
            File compressedDb = sqliteExporterService.generateCompressedSqliteDatabase();
            byte[] fileBytes = Files.readAllBytes(compressedDb.toPath());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            String sha256Hex = HexFormat.of().formatHex(hash);

            Map<String, Object> response = Map.of(
                    "fileName", compressedDb.getName(),
                    "sizeBytes", compressedDb.length(),
                    "sha256", sha256Hex,
                    "lastModified", compressedDb.lastModified()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
