package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.PartnerReportResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final ReportService reportService;

    /**
     * Exporta relatório de parceiros para CSV
     */
    public byte[] exportPartnersToCsv(YearMonth referenceMonth) throws IOException {
        List<PartnerReportResponse> partners = reportService.generatePartnersReport(referenceMonth);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("partnerId", "partnerName", "totalCommission", "totalBonus", "grandTotal")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
            for (PartnerReportResponse partner : partners) {
                csvPrinter.printRecord(
                        partner.getPartnerId(),
                        partner.getPartnerName(),
                        formatCurrency(partner.getTotalCommission()),
                        formatCurrency(partner.getTotalBonus()),
                        formatCurrency(partner.getGrandTotal())
                );
            }
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        return String.format("%.2f", value).replace(".", ",");
    }
}