package nl.yellowbrick.admin.service;

import nl.yellowbrick.activation.service.CardAssignmentService;
import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.admin.domain.CardOrderExportTarget;
import nl.yellowbrick.admin.exceptions.InconsistentDataException;
import nl.yellowbrick.data.dao.*;
import nl.yellowbrick.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class CardOrderExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardOrderExportService.class);

    // TODO eventually externalize this
    private static final String COUNTRY = "Nederland";

    @Autowired
    private CardOrderDao cardOrderDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private CardAssignmentService cardAssignmentService;

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private CardOrderCsvExporter csvExporter;

    public void exportForProductGroup(ProductGroup productGroup) {
        List<CardOrder> orders = cardOrderDao.findPendingExport(productGroup);

        if(orders.isEmpty()) {
            LOGGER.info("skipping requested card order export for product group {}: none pending export",
                    productGroup.getDescription());
            return;
        }

        orders.stream()
                .map(order -> createExportRecord(order, productGroup))
                .collect(groupingBy(CardOrderExportRecord::target, toList()))
                .forEach((target, exports) -> exportRecords(target, productGroup, exports));
    }

    private void exportRecords(CardOrderExportTarget target, ProductGroup productGroup, List<CardOrderExportRecord> exports) {
        if (target.equals(CardOrderExportTarget.OTHER))
            return;

        csvExporter.exportRecords(target, productGroup, exports);
        exports.forEach(export -> cardOrderDao.updateOrderStatus(export.getOrder().getId(), CardOrderStatus.EXPORTED));

    }

    private CardOrderExportRecord createExportRecord(CardOrder order, ProductGroup productGroup) {
        Customer customer = customerDao
                .findById(order.getCustomerId())
                .orElseThrow(() -> new InconsistentDataException("couldn't find customer with id: " + order.getCustomerId()));

        // assign qpark code if needed
        String qparkCode = order.getCardType().equals(CardType.QPARK_CARD)
                ? cardAssignmentService.assignQcardNumber(order)
                : "";

        // try to get business address, fallback to main address
        CustomerAddress address = customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.BILLING)
                .orElseGet(() -> customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.MAIN).get());

        Config defaultLocale = configDao.findSectionField(ConfigSection.YB, "DEFAULT_LOCALE")
                .orElseThrow(() -> new InconsistentDataException("couldn't determine default locale"));

        return new CardOrderExportRecord.Builder(order)
                .productGroup(productGroup)
                .customer(customer)
                .address(address)
                .locale(defaultLocale.getValue())
                .country(COUNTRY)
                .qparkCode(qparkCode)
                .build();
    }
}
