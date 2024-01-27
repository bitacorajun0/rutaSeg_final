package com.jumani.rutaseg.service.order;

import com.jumani.rutaseg.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderReportService {
    private final OrderRepository orderRepo;

    public final byte[] generate(@Nullable Long clientId, LocalDate dateFrom, LocalDate dateTo, boolean admin) {
        final List<Object[]> raw = orderRepo.getReport(clientId, dateFrom, dateTo);

        return this.generateCsv(raw, admin);
    }

    private byte[] generateCsv(List<Object[]> raw, boolean admin) {
        final StringBuilder csvContent = new StringBuilder();
        final List<String> columns = new ArrayList<>(List.of(
                "op", "cliente", "f.turno", "h.turno", "de", "a", "c.suelta", "destinaciones", "ctr/patente",
                "tipo", "factura nombre", "factura cuit"
        ));

        if (admin) {
            columns.addAll(List.of("e.tte", "e.pema", "g.pto"));
        }

        csvContent.append(String.join(",", columns));
        csvContent.append("\n");

        for (Object[] r : raw) {
            for (int i = 0; i < columns.size(); i++) {
                csvContent.append(Optional.ofNullable(r[i])
                        .map(String::valueOf)
                        .filter(StringUtils::isNotBlank)
                        .map(s -> s.replace(",", ";"))
                        .orElse("-"));

                if (i < columns.size() - 1) {
                    csvContent.append(",");
                }
            }

            csvContent.append("\n");
        }

        return csvContent.toString().getBytes();
    }

}