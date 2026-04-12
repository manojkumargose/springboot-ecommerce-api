package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoiceService {

    private final OrderRepository orderRepository;

    public InvoiceService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public byte[] generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId));

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Paragraph storeName = new Paragraph("ShopAI Store", headerFont);
            storeName.setAlignment(Element.ALIGN_CENTER);
            document.add(storeName);

            Paragraph storeEmail = new Paragraph(
                    "Email: store@shopai.com | Phone: +91 9876543210", smallFont);
            storeEmail.setAlignment(Element.ALIGN_CENTER);
            document.add(storeEmail);
            document.add(Chunk.NEWLINE);

            Paragraph divider = new Paragraph(
                    "────────────────────────────────────────────────", normalFont);
            divider.setAlignment(Element.ALIGN_CENTER);
            document.add(divider);
            document.add(Chunk.NEWLINE);

            PdfPTable orderInfoTable = new PdfPTable(2);
            orderInfoTable.setWidthPercentage(100);

            addTableCell(orderInfoTable,
                    "Order ID: #" + order.getId(), headerFont, false);
            addTableCell(orderInfoTable,
                    "Date: " + order.getCreatedAt().toLocalDate(), normalFont, true);
            addTableCell(orderInfoTable,
                    "Customer: " + order.getUser().getUsername(), normalFont, false);
            addTableCell(orderInfoTable,
                    "Status: " + order.getStatus(), normalFont, true);

            document.add(orderInfoTable);
            document.add(Chunk.NEWLINE);

            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4f, 1f, 2f, 2f});

            addTableHeader(itemsTable, "Product", headerFont);
            addTableHeader(itemsTable, "Qty", headerFont);
            addTableHeader(itemsTable, "Unit Price", headerFont);
            addTableHeader(itemsTable, "Total", headerFont);

            for (OrderItem item : order.getItems()) {
                itemsTable.addCell(new Phrase(
                        item.getProduct().getName(), normalFont));
                itemsTable.addCell(new Phrase(
                        String.valueOf(item.getQuantity()), normalFont));
                itemsTable.addCell(new Phrase(
                        "Rs. " + String.format("%.2f", item.getPrice()), normalFont));
                itemsTable.addCell(new Phrase(
                        "Rs. " + String.format("%.2f", item.getPrice() * item.getQuantity()), normalFont));
            }

            document.add(itemsTable);
            document.add(Chunk.NEWLINE);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addSummaryRow(summaryTable,
                    "Subtotal:", "Rs. " + String.format("%.2f", order.getTotalAmount()), normalFont);

            if (order.getCouponCode() != null && order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                addSummaryRow(summaryTable,
                        "Coupon (" + order.getCouponCode() + "):",
                        "- Rs. " + String.format("%.2f", order.getDiscountAmount()), normalFont);
            }

            addSummaryRow(summaryTable,
                    "Total:", "Rs. " + String.format("%.2f", order.getFinalAmount()), headerFont);

            document.add(summaryTable);
            document.add(Chunk.NEWLINE);

            Paragraph divider2 = new Paragraph(
                    "────────────────────────────────────────────────", normalFont);
            divider2.setAlignment(Element.ALIGN_CENTER);
            document.add(divider2);
            document.add(Chunk.NEWLINE);

            Paragraph footer = new Paragraph(
                    "Thank you for shopping with us!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            Paragraph footerNote = new Paragraph(
                    "This is a computer generated invoice.", smallFont);
            footerNote.setAlignment(Element.ALIGN_CENTER);
            document.add(footerNote);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text,
                              Font font, boolean rightAlign) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label,
                               String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}