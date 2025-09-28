package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.PartnerDetailReportResponse;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    /**
     * Exporta relatório detalhado de um parceiro para PDF
     */
    public byte[] exportPartnerDetailToPdf(PartnerDetailReportResponse partnerDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Configurar fontes
            PdfFont titleFont = PdfFontFactory.createFont();
            PdfFont headerFont = PdfFontFactory.createFont();
            PdfFont normalFont = PdfFontFactory.createFont();

            // Cabeçalho
            addHeader(document, partnerDetail, titleFont, headerFont);

            // Informações do parceiro
            addPartnerInfo(document, partnerDetail, headerFont, normalFont);

            // Resumo financeiro
            addFinancialSummary(document, partnerDetail, headerFont, normalFont);

            // Detalhes dos clientes
            addClientDetails(document, partnerDetail, headerFont, normalFont);

            // Políticas de bônus
            addBonusPolicies(document, partnerDetail, headerFont, normalFont);

            // Rodapé
            addFooter(document, normalFont);

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addHeader(Document document, PartnerDetailReportResponse partnerDetail, PdfFont titleFont, PdfFont headerFont) {
        // Título principal
        Paragraph title = new Paragraph("EXTRATO DETALHADO DE PARCEIRO")
                .setFont(titleFont)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Subtítulo
        Paragraph subtitle = new Paragraph(partnerDetail.getPartnerName())
                .setFont(headerFont)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitle);

        // Mês de referência
        Paragraph month = new Paragraph("Mês de Referência: " + partnerDetail.getReferenceMonth())
                .setFont(headerFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(month);
    }

    private void addPartnerInfo(Document document, PartnerDetailReportResponse partnerDetail, PdfFont headerFont, PdfFont normalFont) {
        Paragraph sectionTitle = new Paragraph("INFORMAÇÕES DO PARCEIRO")
                .setFont(headerFont)
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table infoTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(infoTable, "ID:", partnerDetail.getPartnerId(), normalFont);
        addInfoRow(infoTable, "Nome:", partnerDetail.getPartnerName(), normalFont);
        addInfoRow(infoTable, "Email:", partnerDetail.getPartnerEmail(), normalFont);
        addInfoRow(infoTable, "Telefone:", partnerDetail.getPartnerPhone(), normalFont);
        addInfoRow(infoTable, "Status:", partnerDetail.getPartnerStatus(), normalFont);

        document.add(infoTable);
    }

    private void addFinancialSummary(Document document, PartnerDetailReportResponse partnerDetail, PdfFont headerFont, PdfFont normalFont) {
        Paragraph sectionTitle = new Paragraph("RESUMO FINANCEIRO")
                .setFont(headerFont)
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table summaryTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
        
        addSummaryRow(summaryTable, "Total de Comissões:", formatCurrency(partnerDetail.getTotalCommission()), normalFont);
        addSummaryRow(summaryTable, "Total de Bônus:", formatCurrency(partnerDetail.getTotalBonus()), normalFont);
        addSummaryRow(summaryTable, "Total Geral:", formatCurrency(partnerDetail.getGrandTotal()), normalFont, true);
        addSummaryRow(summaryTable, "Clientes Ativos:", String.valueOf(partnerDetail.getActiveClients()), normalFont);
        addSummaryRow(summaryTable, "Faturamento Total:", formatCurrency(partnerDetail.getTotalClientBilling()), normalFont);

        document.add(summaryTable);
    }

    private void addClientDetails(Document document, PartnerDetailReportResponse partnerDetail, PdfFont headerFont, PdfFont normalFont) {
        Paragraph sectionTitle = new Paragraph("DETALHES DOS CLIENTES")
                .setFont(headerFont)
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (partnerDetail.getClientDetails() == null || partnerDetail.getClientDetails().isEmpty()) {
            Paragraph noData = new Paragraph("Nenhum cliente encontrado para este parceiro.")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setItalic();
            document.add(noData);
            return;
        }

        Table clientTable = new Table(8).setWidth(UnitValue.createPercentValue(100));
        
        // Cabeçalho da tabela
        addClientHeader(clientTable, headerFont);
        
        // Dados dos clientes
        for (PartnerDetailReportResponse.ClientReportDetail client : partnerDetail.getClientDetails()) {
            addClientRow(clientTable, client, normalFont);
        }

        document.add(clientTable);
    }

    private void addBonusPolicies(Document document, PartnerDetailReportResponse partnerDetail, PdfFont headerFont, PdfFont normalFont) {
        if (partnerDetail.getBonusPoliciesApplied() == null || partnerDetail.getBonusPoliciesApplied().isEmpty()) {
            return;
        }

        Paragraph sectionTitle = new Paragraph("POLÍTICAS DE BÔNUS APLICADAS")
                .setFont(headerFont)
                .setFontSize(12)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        for (PartnerDetailReportResponse.BonusPolicyApplied policy : partnerDetail.getBonusPoliciesApplied()) {
            Paragraph policyText = new Paragraph(
                String.format("• %s: %s - %s", 
                    policy.getPolicyName(), 
                    policy.getCriteriaMet(), 
                    formatCurrency(policy.getBonusAmount()))
            )
            .setFont(normalFont)
            .setFontSize(10)
            .setMarginBottom(5);
            document.add(policyText);
        }
    }

    private void addFooter(Document document, PdfFont normalFont) {
        Paragraph footer = new Paragraph(
            "Relatório gerado automaticamente pelo Sistema de Gestão de Parceiros - " +
            "Ecomercial - " + 
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        )
        .setFont(normalFont)
        .setFontSize(8)
        .setTextAlignment(TextAlignment.CENTER)
        .setMarginTop(30);
        document.add(footer);
    }

    private void addInfoRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(font).setBold()).setPadding(5));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFont(font)).setPadding(5));
    }

    private void addSummaryRow(Table table, String label, String value, PdfFont font) {
        addSummaryRow(table, label, value, font, false);
    }

    private void addSummaryRow(Table table, String label, String value, PdfFont font, boolean isTotal) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(font).setBold()).setPadding(5);
        Cell valueCell = new Cell().add(new Paragraph(value).setFont(font)).setPadding(5);
        
        if (isTotal) {
            labelCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            valueCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            valueCell.setTextAlignment(TextAlignment.RIGHT);
        } else {
            valueCell.setTextAlignment(TextAlignment.RIGHT);
        }
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addClientHeader(Table table, PdfFont font) {
        String[] headers = {"ID", "Nome", "Tipo", "Status", "Faturamento", "Comissão", "Bônus", "Total"};
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(font).setBold()).setPadding(5);
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }
    }

    private void addClientRow(Table table, PartnerDetailReportResponse.ClientReportDetail client, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(String.valueOf(client.getClientId())).setFont(font)).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(client.getClientName()).setFont(font)).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(client.getClientType().name()).setFont(font)).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(client.getClientStatus().name()).setFont(font)).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(client.getMonthlyBilling())).setFont(font)).setPadding(3).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(client.getCommissionValue())).setFont(font)).setPadding(3).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(client.getBonusValue())).setFont(font)).setPadding(3).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(new Paragraph(formatCurrency(client.getTotalValue())).setFont(font)).setPadding(3).setTextAlignment(TextAlignment.RIGHT));
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "R$ 0,00";
        }
        return String.format("R$ %.2f", value).replace(".", ",");
    }
}
