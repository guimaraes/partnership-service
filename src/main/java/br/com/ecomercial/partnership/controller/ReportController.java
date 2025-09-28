package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.PartnerDetailReportResponse;
import br.com.ecomercial.partnership.dto.PartnerReportResponse;
import br.com.ecomercial.partnership.service.CsvExportService;
import br.com.ecomercial.partnership.service.PdfExportService;
import br.com.ecomercial.partnership.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Relatórios e exportações")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/closings/{month}/partners")
    @Operation(summary = "Relatório consolidado por parceiros", description = "Retorna relatório consolidado de todos os parceiros para um fechamento mensal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento mensal não encontrado"),
            @ApiResponse(responseCode = "400", description = "Fechamento não está completo")
    })
    public ResponseEntity<List<PartnerReportResponse>> getPartnersReport(
            @Parameter(description = "Mês de referência (YYYY-MM)", example = "2025-10")
            @PathVariable String month) {
        
        validateAdminAccess();
        
        YearMonth referenceMonth = YearMonth.parse(month);
        List<PartnerReportResponse> report = reportService.generatePartnersReport(referenceMonth);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/closings/{month}/partners.csv")
    @Operation(summary = "Exportar CSV por parceiros", description = "Exporta relatório consolidado de parceiros em formato CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento mensal não encontrado"),
            @ApiResponse(responseCode = "400", description = "Fechamento não está completo")
    })
    public ResponseEntity<byte[]> exportPartnersToCsv(
            @Parameter(description = "Mês de referência (YYYY-MM)", example = "2025-10")
            @PathVariable String month) {
        
        validateAdminAccess();
        
        try {
            YearMonth referenceMonth = YearMonth.parse(month);
            byte[] csvData = csvExportService.exportPartnersToCsv(referenceMonth);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    String.format("partners_report_%s.csv", month));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/closings/{month}/partner/{partnerId}")
    @Operation(summary = "Relatório detalhado de um parceiro", description = "Retorna relatório detalhado de um parceiro específico para um fechamento mensal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório detalhado gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento mensal ou parceiro não encontrado"),
            @ApiResponse(responseCode = "400", description = "Fechamento não está completo")
    })
    public ResponseEntity<PartnerDetailReportResponse> getPartnerDetailReport(
            @Parameter(description = "Mês de referência (YYYY-MM)", example = "2025-10") @PathVariable String month,
            @Parameter(description = "ID do parceiro", example = "P-1001") @PathVariable String partnerId) {

        validateAdminAccess();

        YearMonth referenceMonth = YearMonth.parse(month);
        PartnerDetailReportResponse report = reportService.generatePartnerDetailReport(referenceMonth, partnerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping(value = "/closings/{month}/partner/{partnerId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exportar extrato detalhado de parceiro em PDF", description = "Exporta extrato detalhado de um parceiro em formato PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento mensal ou parceiro não encontrado"),
            @ApiResponse(responseCode = "400", description = "Fechamento não está completo")
    })
    public ResponseEntity<byte[]> exportPartnerDetailReportPdf(
            @Parameter(description = "Mês de referência (YYYY-MM)", example = "2025-10") @PathVariable String month,
            @Parameter(description = "ID do parceiro", example = "P-1001") @PathVariable String partnerId) {

        validateAdminAccess();

        try {
            YearMonth referenceMonth = YearMonth.parse(month);
            PartnerDetailReportResponse report = reportService.generatePartnerDetailReport(referenceMonth, partnerId);
            byte[] pdfData = pdfExportService.exportPartnerDetailToPdf(report);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    String.format("partner_detail_report_%s_%s.pdf", partnerId, month));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void validateAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof br.com.ecomercial.partnership.entity.User)) {
            throw new RuntimeException("Authentication required");
        }
        
        br.com.ecomercial.partnership.entity.User user = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        if (user.getRole() != br.com.ecomercial.partnership.entity.User.Role.ADMIN) {
            throw new RuntimeException("Admin access required for reports");
        }
    }
}