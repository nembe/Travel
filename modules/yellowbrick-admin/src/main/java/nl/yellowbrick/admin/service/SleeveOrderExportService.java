package nl.yellowbrick.admin.service;

import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class SleeveOrderExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleeveOrderExportService.class);

    // TODO eventually externalize this
    private static final String COUNTRY = "Nederland";

    @Autowired private CardOrderDao cardOrderDao;
    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao customerAddressDao;
    @Autowired private ConfigDao configDao;
    @Autowired private SleeveOrderCsvExporter csvExporter;

    public void export() {
        Stream<CardOrder> orders = cardOrderDao.findPendingExport()
                .stream()
                .filter(order -> order.getCardType() == CardType.SLEEVE);

        List<CardOrderExportRecord> exports = orders
                .flatMap(this::createExportRecords)
                .collect(Collectors.toList());

        if(exports.isEmpty()) {
            LOGGER.info("skipping requested sleeve order export: none pending export");
        } else {
            csvExporter.exportRecords(exports);
            exports.forEach(this::updateOrderStatus);
        }
    }

    private void updateOrderStatus(CardOrderExportRecord export) {
        cardOrderDao.updateOrderStatus(export.getOrder().getId(), CardOrderStatus.EXPORTED);
    }

    private Stream<CardOrderExportRecord> createExportRecords(CardOrder order) {
        Customer customer = customerDao
                .findById(order.getCustomerId())
                .orElseThrow(() -> new InconsistentDataException("couldn't find customer with id: " + order.getCustomerId()));

        // try to get business address, fallback to main address
        CustomerAddress address = customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.BILLING)
                .orElseGet(() -> customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.MAIN).get());

        Config defaultLocale = configDao.findSectionField(ConfigSection.YB, "DEFAULT_LOCALE")
                .orElseThrow(() -> new InconsistentDataException("couldn't determine default locale"));

        return IntStream.rangeClosed(1, order.getAmount()).mapToObj(idx -> {
            return new CardOrderExportRecord.Builder(order)
                    .customer(customer)
                    .address(address)
                    .locale(defaultLocale.getValue())
                    .country(COUNTRY)
                    .build();
        });
    }
}
