package nl.yellowbrick.admin.service;

import nl.yellowbrick.admin.domain.SleeveOrderExportRecord;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.data.dao.CardOrderDao;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SleeveOrderExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleeveOrderExportService.class);

    // TODO eventually externalize this
    private static final String COUNTRY = "Nederland";

    @Autowired private CardOrderDao cardOrderDao;
    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao customerAddressDao;
    @Autowired private SleeveOrderCsvExporter csvExporter;

    public void export() {
        List<SleeveOrderExportRecord> exports = cardOrderDao.findPendingExport()
                .stream()
                .filter(order -> order.getCardType() == CardType.SLEEVE)
                .map(this::createExportRecord)
                .collect(Collectors.toList());

        if(exports.isEmpty()) {
            LOGGER.info("skipping requested sleeve order export: none pending export");
        } else {
            csvExporter.exportRecords(exports);
            exports.forEach(this::updateOrderStatus);
        }
    }

    private void updateOrderStatus(SleeveOrderExportRecord export) {
        cardOrderDao.updateOrderStatus(export.getOrder().getId(), CardOrderStatus.EXPORTED);
    }

    private SleeveOrderExportRecord createExportRecord(CardOrder order) {
        Customer customer = customerDao
                .findById(order.getCustomerId())
                .orElseThrow(() -> new InconsistentDataException("couldn't find customer with id: " + order.getCustomerId()));

        // try to get business address, fallback to main address
        CustomerAddress address = customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.BILLING)
                .orElseGet(() -> customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.MAIN).get());

        return new SleeveOrderExportRecord(order, customer, address, COUNTRY);
    }
}
