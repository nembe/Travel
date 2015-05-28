package nl.yellowbrick.admin.service;

import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import nl.yellowbrick.data.domain.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class WelcomeLetterExportService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private WelcomeLetterCsvExporter csvExporter;

    public Optional<Path> exportForProductGroup(ProductGroup productGroup, long fromCustomerIdExclusive) {
        String exportName = String.format("from-customer-%s", fromCustomerIdExclusive);

        try(WelcomeLetterCsvExporter.Appender appender = csvExporter.createAppender(productGroup, exportName)) {
            customerDao.scan(
                    productGroup,
                    fromCustomerIdExclusive,
                    customer -> appender.append(record(customer)));

            return Optional.ofNullable(appender.isWriting() ? appender.getPath() : null);
        }
    }

    public Optional<Path> exportForProductGroup(ProductGroup productGroup, Date fromDateInclusive, Date toDateExclusive) {
        String exportName = "between-dates-dateA-dateB";

        try(WelcomeLetterCsvExporter.Appender appender = csvExporter.createAppender(productGroup, exportName)) {
            customerDao.scan(
                    productGroup,
                    fromDateInclusive,
                    toDateExclusive,
                    customer -> appender.append(record(customer)));

            return Optional.ofNullable(appender.isWriting() ? appender.getPath() : null);
        }
    }

    public Stream<FileSystemResource> listExports(ProductGroup productGroup) {
        return csvExporter.listExports(productGroup)
                .map(Path::toFile)
                .map(FileSystemResource::new);
    }

    private CardOrderExportRecord record(Customer customer) {
        return new CardOrderExportRecord.Builder(customer)
                .address(customerAddress(customer))
                .build();
    }

    private CustomerAddress customerAddress(Customer customer) {
        return customerAddressDao
                .findByCustomerId(customer.getCustomerId(), AddressType.BILLING)
                .orElseGet(() -> customerAddressDao.findByCustomerId(customer.getCustomerId(), AddressType.MAIN).get());
    }
}
