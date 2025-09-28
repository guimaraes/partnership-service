package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.ImportError;
import br.com.ecomercial.partnership.dto.ImportRequest;
import br.com.ecomercial.partnership.dto.ImportResponse;
import br.com.ecomercial.partnership.entity.*;
import br.com.ecomercial.partnership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final PartnerRepository partnerRepository;
    private final ClientRepository clientRepository;
    private final MetricsService metricsService;

    /**
     * Inicia processo de importação de parceiros
     */
    @Transactional
    public ImportResponse importPartners(MultipartFile file) {
        String jobId = generateJobId();
        
        ImportJob job = ImportJob.builder()
                .jobId(jobId)
                .importType(ImportJob.ImportType.PARTNERS)
                .status(ImportJob.Status.ACCEPTED)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .startedAt(LocalDateTime.now())
                .message("Importação de parceiros iniciada")
                .build();

        importJobRepository.save(job);

        // Processar assincronamente
        processPartnersImportAsync(jobId, file);

        return ImportResponse.builder()
                .jobId(jobId)
                .importType("PARTNERS")
                .status("ACCEPTED")
                .fileName(file.getOriginalFilename())
                .totalLines(0) // Será atualizado durante o processamento
                .successLines(0)
                .errorLines(0)
                .message("Importação de parceiros iniciada com sucesso")
                .startedAt(job.getStartedAt())
                .build();
    }

    /**
     * Inicia processo de importação de clientes
     */
    @Transactional
    public ImportResponse importClients(MultipartFile file) {
        String jobId = generateJobId();
        
        ImportJob job = ImportJob.builder()
                .jobId(jobId)
                .importType(ImportJob.ImportType.CLIENTS)
                .status(ImportJob.Status.ACCEPTED)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .startedAt(LocalDateTime.now())
                .message("Importação de clientes iniciada")
                .build();

        importJobRepository.save(job);

        // Processar assincronamente
        processClientsImportAsync(jobId, file);

        return ImportResponse.builder()
                .jobId(jobId)
                .importType("CLIENTS")
                .status("ACCEPTED")
                .fileName(file.getOriginalFilename())
                .totalLines(0) // Será atualizado durante o processamento
                .successLines(0)
                .errorLines(0)
                .message("Importação de clientes iniciada com sucesso")
                .startedAt(job.getStartedAt())
                .build();
    }

    /**
     * Processa importação de parceiros de forma assíncrona
     */
    @Async
    @Transactional
    public void processPartnersImportAsync(String jobId, MultipartFile file) {
        ImportJob job = importJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado"));

        try {
            job.setStatus(ImportJob.Status.PROCESSING);
            importJobRepository.save(job);

            List<ImportError> errors = new ArrayList<>();
            int successCount = 0;
            int totalLines = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

                for (CSVRecord record : csvParser) {
                    totalLines++;
                    try {
                        Partner partner = parsePartnerFromCsv(record);
                        
                        // Verificar se já existe parceiro com o mesmo documento
                        if (partnerRepository.existsByDocument(partner.getDocument())) {
                            errors.add(ImportError.builder()
                                    .lineNumber((int) record.getRecordNumber())
                                    .lineContent(record.toString())
                                    .field("document")
                                    .errorMessage("Documento já existe")
                                    .invalidValue(partner.getDocument())
                                    .build());
                            continue;
                        }

                        partnerRepository.save(partner);
                        successCount++;

                    } catch (Exception e) {
                        errors.add(ImportError.builder()
                                .lineNumber((int) record.getRecordNumber())
                                .lineContent(record.toString())
                                .field("general")
                                .errorMessage("Erro ao processar linha: " + e.getMessage())
                                .invalidValue("")
                                .build());
                    }
                }
            }

            // Atualizar job com resultados
            job.setTotalLines(totalLines);
            job.setSuccessLines(successCount);
            job.setErrorLines(errors.size());
            job.setStatus(ImportJob.Status.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setMessage(String.format("Importação concluída: %d sucessos, %d erros", successCount, errors.size()));
            
            if (!errors.isEmpty()) {
                job.setErrorDetails(formatErrorsAsJson(errors));
            }

            importJobRepository.save(job);

            // Incrementar métricas de importação
            metricsService.incrementImportPartner(successCount);
            if (successCount > 0) {
                metricsService.incrementImportSuccess("PARTNERS");
            }
            if (errors.size() > 0) {
                metricsService.incrementImportFailure("PARTNERS", "Validation errors");
            }

            log.info("Importação de parceiros concluída - Job: {}, Sucessos: {}, Erros: {}", 
                    jobId, successCount, errors.size());

        } catch (Exception e) {
            job.setStatus(ImportJob.Status.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setMessage("Erro durante importação: " + e.getMessage());
            importJobRepository.save(job);

            log.error("Erro durante importação de parceiros - Job: {}", jobId, e);
        }
    }

    /**
     * Processa importação de clientes de forma assíncrona
     */
    @Async
    @Transactional
    public void processClientsImportAsync(String jobId, MultipartFile file) {
        ImportJob job = importJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado"));

        try {
            job.setStatus(ImportJob.Status.PROCESSING);
            importJobRepository.save(job);

            List<ImportError> errors = new ArrayList<>();
            int successCount = 0;
            int totalLines = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

                for (CSVRecord record : csvParser) {
                    totalLines++;
                    try {
                        Client client = parseClientFromCsv(record);
                        
                        // Verificar se o parceiro existe
                        if (!partnerRepository.existsByPartnerId(client.getPartnerId())) {
                            errors.add(ImportError.builder()
                                    .lineNumber((int) record.getRecordNumber())
                                    .lineContent(record.toString())
                                    .field("partnerId")
                                    .errorMessage("Parceiro não encontrado")
                                    .invalidValue(client.getPartnerId())
                                    .build());
                            continue;
                        }

                        clientRepository.save(client);
                        successCount++;

                    } catch (Exception e) {
                        errors.add(ImportError.builder()
                                .lineNumber((int) record.getRecordNumber())
                                .lineContent(record.toString())
                                .field("general")
                                .errorMessage("Erro ao processar linha: " + e.getMessage())
                                .invalidValue("")
                                .build());
                    }
                }
            }

            // Atualizar job com resultados
            job.setTotalLines(totalLines);
            job.setSuccessLines(successCount);
            job.setErrorLines(errors.size());
            job.setStatus(ImportJob.Status.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setMessage(String.format("Importação concluída: %d sucessos, %d erros", successCount, errors.size()));
            
            if (!errors.isEmpty()) {
                job.setErrorDetails(formatErrorsAsJson(errors));
            }

            importJobRepository.save(job);

            // Incrementar métricas de importação
            metricsService.incrementImportClient(successCount);
            if (successCount > 0) {
                metricsService.incrementImportSuccess("CLIENTS");
            }
            if (errors.size() > 0) {
                metricsService.incrementImportFailure("CLIENTS", "Validation errors");
            }

            log.info("Importação de clientes concluída - Job: {}, Sucessos: {}, Erros: {}", 
                    jobId, successCount, errors.size());

        } catch (Exception e) {
            job.setStatus(ImportJob.Status.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setMessage("Erro durante importação: " + e.getMessage());
            importJobRepository.save(job);

            log.error("Erro durante importação de clientes - Job: {}", jobId, e);
        }
    }

    /**
     * Busca status de um job de importação
     */
    public ImportResponse getImportStatus(String jobId) {
        ImportJob job = importJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado"));

        return ImportResponse.builder()
                .jobId(job.getJobId())
                .importType(job.getImportType().name())
                .status(job.getStatus().name())
                .fileName(job.getFileName())
                .totalLines(job.getTotalLines())
                .successLines(job.getSuccessLines())
                .errorLines(job.getErrorLines())
                .message(job.getMessage())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    private Partner parsePartnerFromCsv(CSVRecord record) {
        return Partner.builder()
                .partnerId(record.get("partnerId"))
                .name(record.get("name"))
                .email(record.get("email"))
                .phone(record.get("phone"))
                .document(record.get("document"))
                .status(Partner.Status.ACTIVE)
                .build();
    }

    private Client parseClientFromCsv(CSVRecord record) {
        return Client.builder()
                .name(record.get("name"))
                .email(record.get("email"))
                .phone(record.get("phone"))
                .document(record.get("document"))
                .partnerId(record.get("partnerId"))
                .clientType(Client.ClientType.valueOf(record.get("clientType")))
                .monthlyBilling(new java.math.BigDecimal(record.get("monthlyBilling")))
                .status(Client.Status.ACTIVE)
                .build();
    }

    private String generateJobId() {
        return "import-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String formatErrorsAsJson(List<ImportError> errors) {
        // Implementação simples para serializar erros como JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < errors.size(); i++) {
            ImportError error = errors.get(i);
            json.append("{")
                .append("\"lineNumber\":").append(error.getLineNumber()).append(",")
                .append("\"field\":\"").append(error.getField()).append("\",")
                .append("\"errorMessage\":\"").append(error.getErrorMessage()).append("\",")
                .append("\"invalidValue\":\"").append(error.getInvalidValue()).append("\"")
                .append("}");
            if (i < errors.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }
}
