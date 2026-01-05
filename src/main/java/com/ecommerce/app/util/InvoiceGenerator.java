package com.ecommerce.app.util;

import com.ecommerce.app.entity.Order;
import net.sf.jasperreports.engine.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InvoiceGenerator {

    public void generateInvoice(Order order) throws JRException {
        String templatePath = "src/main/resources/templates/invoice_template.jrxml";

        JasperReport jasperReport = JasperCompileManager.compileReport(templatePath);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderId", order.getId());
        parameters.put("userName", order.getUser().getUsername());
        parameters.put("address", order.getAddress());
        parameters.put("totalPrice", order.getTotalPrice());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
        JasperExportManager.exportReportToPdfFile(jasperPrint, "invoices/invoice_" + order.getId() + ".pdf");
    }
}
