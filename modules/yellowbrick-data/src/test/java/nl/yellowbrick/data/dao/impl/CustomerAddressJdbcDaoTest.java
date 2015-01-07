package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.BaseSpringTestCase;
import nl.yellowbrick.data.database.DbHelper;
import nl.yellowbrick.data.database.Functions;
import nl.yellowbrick.data.domain.AddressType;
import nl.yellowbrick.data.domain.CustomerAddress;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static nl.yellowbrick.data.database.Functions.CALL_RECORDERS;
import static nl.yellowbrick.data.database.Functions.FunctionCall;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomerAddressJdbcDaoTest extends BaseSpringTestCase {

    @Autowired
    CustomerAddressJdbcDao addressDao;

    @Autowired
    DbHelper db;

    @Test
    public void finds_customer_by_id() {
        CustomerAddress actualAddress = addressDao.findByCustomerId(2364, AddressType.MAIN).get();
        CustomerAddress expectedAddress = new CustomerAddress();

        expectedAddress.setAddress("Turnhoutplantsoen");
        expectedAddress.setCustomerAddressId(1723);
        expectedAddress.setCity("Amsterdam");
        expectedAddress.setCountryCode("NL");
        expectedAddress.setExtraInfo(null);
        expectedAddress.setHouseNr("182");
        expectedAddress.setSupplement(null);
        expectedAddress.setZipCode("1066 DG");
        expectedAddress.setPoBox(null);

        assertThat(actualAddress, equalTo(expectedAddress));
    }

    @Test
    public void filters_by_address_type() {
        db.accept((t) -> t.update("UPDATE CUSTOMERADDRESS SET ADDRESSTYPEIDFK = 1"));

        assertThat(addressDao.findByCustomerId(2364, AddressType.MAIN).isPresent(), is(true));
        assertThat(addressDao.findByCustomerId(2364, AddressType.BILLING).isPresent(), is(false));

        db.accept((t) -> t.update("UPDATE CUSTOMERADDRESS SET ADDRESSTYPEIDFK = 2"));

        assertThat(addressDao.findByCustomerId(2364, AddressType.BILLING).isPresent(), is(true));
        assertThat(addressDao.findByCustomerId(2364, AddressType.MAIN).isPresent(), is(false));
    }

    @Test
    public void returns_empty_if_id_unknown() {
        assertThat(addressDao.findByCustomerId(123456, AddressType.MAIN), equalTo(Optional.empty()));
    }

    @Test
    public void delegates_saving_private_customer_address_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(1);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        CustomerAddress address = testAddress();
        addressDao.savePrivateCustomerAddress(2364, address);

        lock.await(2, TimeUnit.SECONDS);

        assertStoredProcedureCall(calls.getFirst(), address, AddressType.MAIN);
    }

    @Test
    public void delegates_saving_business_customer_address_to_stored_procedure() throws Exception {
        CountDownLatch lock = new CountDownLatch(2);
        LinkedList<Functions.FunctionCall> calls = new LinkedList<>();

        CALL_RECORDERS.add((functionCall) -> {
            calls.add(functionCall);
            lock.countDown();
        });

        CustomerAddress address = testAddress();
        addressDao.saveBusinessCustomerAddress(2364, address, AddressType.MAIN);
        addressDao.saveBusinessCustomerAddress(2364, address, AddressType.BILLING);

        lock.await(2, TimeUnit.SECONDS);

        assertStoredProcedureCall(calls.get(0), address, AddressType.MAIN);
        assertStoredProcedureCall(calls.get(1), address, AddressType.BILLING);
    }

    private void assertStoredProcedureCall(FunctionCall call, CustomerAddress address, AddressType addressType) {
        Object[] args = call.arguments;

        assertThat(call.functionName, equalTo("customerSaveAddress"));
        assertThat(call.getNumericArg(0).longValue(), IsEqual.equalTo(address.getCustomerAddressId()));
        assertThat(args[1], IsEqual.equalTo(2364));
        assertThat(args[2], IsEqual.equalTo(addressType.code()));
        assertThat(args[3], IsEqual.equalTo(address.getAddress()));
        assertThat(args[4], IsEqual.equalTo(address.getHouseNr()));
        assertThat(args[5], IsEqual.equalTo(address.getSupplement()));
        assertThat(args[6], IsEqual.equalTo(address.getPoBox()));
        assertThat(args[7], IsEqual.equalTo(address.getZipCode()));
        assertThat(args[8], IsEqual.equalTo(address.getCity()));
        assertThat(args[9], IsEqual.equalTo(address.getCountryCode()));
        assertThat(args[10], IsEqual.equalTo(address.getExtraInfo()));
        assertThat(args[11], IsEqual.equalTo("TEST MUTATOR"));

    }

    private CustomerAddress testAddress() {
        CustomerAddress address = new CustomerAddress();

        address.setAddress("test street");
        address.setCustomerAddressId(12345);
        address.setCity("Amsterdam");
        address.setCountryCode("NL");
        address.setExtraInfo("next to the neighbour");
        address.setHouseNr("123");
        address.setSupplement("ABC");
        address.setZipCode("1020 ZZ");
        address.setPoBox("410");

        return address;
    }
}
