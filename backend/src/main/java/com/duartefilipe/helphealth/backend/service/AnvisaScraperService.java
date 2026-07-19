package com.duartefilipe.helphealth.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnvisaScraperService {

    private static final Logger log = LoggerFactory.getLogger(AnvisaScraperService.class);
    private static final String ANVISA_PRECOS_PORTAL_URL = "https://www.gov.br/anvisa/pt-br/assuntos/medicamentos/cmed/precos";
    private static final Pattern CSV_XLS_LINK_PATTERN = Pattern.compile("href=\"(https?://[^\"]+?\\.(?:csv|xls|xlsx))\"", Pattern.CASE_INSENSITIVE);

    private final HttpClient httpClient;

    public AnvisaScraperService() {
        this.httpClient = createTrustAllHttpClient();
    }

    private HttpClient createTrustAllHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
        } catch (Exception e) {
            return HttpClient.newHttpClient();
        }
    }

    public String discoverLatestCmedDatasetUrl() {
        try {
            log.info("Buscando link da tabela mais recente de medicamentos no portal da Anvisa: {}", ANVISA_PRECOS_PORTAL_URL);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANVISA_PRECOS_PORTAL_URL))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Matcher matcher = CSV_XLS_LINK_PATTERN.matcher(response.body());
                while (matcher.find()) {
                    String url = matcher.group(1);
                    if (url.toLowerCase().contains("conconformidade") || url.toLowerCase().contains("medicamento") || url.toLowerCase().contains("cmed")) {
                        log.info("Link oficial de dados da Anvisa localizado: {}", url);
                        return url;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao rastrear portal da Anvisa: {}. Utilizando fonte fallback de dados abertos.", e.getMessage());
        }
        // Fallback oficial de Dados Abertos
        return "https://dados.anvisa.gov.br/dados/TA_CAMARA_REGULACAO_MEDICAMENTOS.csv";
    }

    public InputStream downloadDatasetStream(String fileUrl) throws Exception {
        log.info("Baixando dados abertos da Anvisa a partir de: {}", fileUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .timeout(Duration.ofMinutes(10))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 200) {
            return response.body();
        }
        throw new IllegalStateException("Falha ao baixar arquivo da Anvisa. Código HTTP: " + response.statusCode());
    }
}
