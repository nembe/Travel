package nl.yellowbrick.admin.service;

import com.google.common.collect.Maps;
import nl.yellowbrick.admin.domain.CardOrderExportRecord;
import nl.yellowbrick.data.dao.CustomerAddressDao;
import nl.yellowbrick.data.dao.CustomerDao;
import nl.yellowbrick.data.dao.WelcomeLetterSettingsDao;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;
import nl.yellowbrick.data.domain.ProductGroup;
import nl.yellowbrick.data.function.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
public class WelcomeLetterExportService {

    @Autowired private CustomerDao customerDao;
    @Autowired private CustomerAddressDao customerAddressDao;
    @Autowired private WelcomeLetterCsvExporter csvExporter;
    @Autowired private WelcomeLetterSettingsDao settings;

    public Either<Exception, Path> exportForProductGroup(ProductGroup productGroup, long fromCustomerIdExclusive) {
        String exportName = String.format("from-customer-%s", fromCustomerIdExclusive);

        try(WelcomeLetterCsvExporter.Appender appender = csvExporter.createAppender(productGroup, exportName)) {
            // have to hold latest customer outside of the stack
            final Map<Integer, Customer> customerHolder = Maps.newHashMap();
            Consumer<Customer> trackCustomer = customer -> customerHolder.put(0, customer);
            Consumer<Customer> appendRecord = customer -> appender.append(record(customer));

            customerDao.scanActive(productGroup, fromCustomerIdExclusive, appendRecord.andThen(trackCustomer));

            if(customerHolder.isEmpty()) {
                return Either.left(new IllegalStateException("no data to write"));
            } else {
                settings.updateLatestExportedCustomer(customerHolder.get(0).getCustomerId());
                return Either.right(appender.getPath());
            }
        } catch(Exception e) {
            return Either.left(e);
        }
    }

    public Either<Exception, Path> exportForProductGroup(ProductGroup productGroup, Date fromDateInclusive, Date toDateExclusive) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String exportName = String.format("between-dates-%s-%s",
                dateFormat.format(fromDateInclusive),
                dateFormat.format(toDateExclusive));

        try(WelcomeLetterCsvExporter.Appender appender = csvExporter.createAppender(productGroup, exportName)) {
            customerDao.scanActive(
                    productGroup,
                    fromDateInclusive,
                    toDateExclusive,
                    customer -> appender.append(record(customer)));

            return appender.isWriting()
                    ? Either.right(appender.getPath())
                    : Either.left(new IllegalStateException("no data to write"));
        } catch(Exception e) {
            return Either.left(e);
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
