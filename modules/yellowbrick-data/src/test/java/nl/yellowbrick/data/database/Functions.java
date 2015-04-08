package nl.yellowbrick.data.database;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is absolutely NOT thread-safe but that shouldn't matter for tests
 */
public class Functions {

    public static final String TEST_QCARD_NUMBER = "TEST_QCARD_NUMBER";
    public static int VALIDATE_MEMBERSHIP_RETVAL = -1;

    public static List<Consumer<FunctionCall>> CALL_RECORDERS = new ArrayList<>();

    public static void customerValidateMembership(int customerId, String customerNr, int numberOfTCards,
                                                  int numberOfQCards, String issuePhysicalCard, int registrationFee,
                                                  String pinCode, String password, String mutator, Integer[] returnOut) {

        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerId, customerNr, numberOfTCards, numberOfQCards, issuePhysicalCard,
                    registrationFee, pinCode, password, mutator };

            recorder.accept(new FunctionCall("customerValidateMembership", args));
        });

        returnOut[0] = VALIDATE_MEMBERSHIP_RETVAL;
    }

    public static void saveSignupSpecialRate(int customerId) {
        CALL_RECORDERS.forEach((recorder) -> {
            recorder.accept(new FunctionCall("saveSignupSpecialRate", customerId));
        });
    }

    public static void cardOrderUpdate(int newOrderId, String newOrderStatus, int newPricePerCard, int newAmount) {
        CALL_RECORDERS.forEach((recorder) -> {
            recorder.accept(new FunctionCall("cardOrderUpdate",
                    newOrderId, newOrderStatus, newPricePerCard, newAmount));
        });
    }

    public static void cardOrderValidate(int cardOrderId, String typeOfCard, Integer[] returnOut) {
        CALL_RECORDERS.forEach((recorder) -> {
            recorder.accept(new FunctionCall("cardOrderValidate", cardOrderId, typeOfCard));
        });

        returnOut[0] = -1;
    }

    public static void customerSaveAddress(int customerAddressId, int customerId, int addressTypeId, String street,
                                           String houseNr, String supplement, String poBox, String zipCode,
                                           String city, String countryCode, String extraInfo, String mutator) {

        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerAddressId, customerId, addressTypeId, street, houseNr, supplement, poBox,
                    zipCode, city, countryCode, extraInfo, mutator };

            recorder.accept(new FunctionCall("customerSaveAddress", args));
        });
    }

    public static void customerSavePrivateData(int customerId, String gender, String initials, String firstName,
                                               String infix, String lastName, String email, String phoneNr,
                                               String fax, Date dateOfBirth, int productGroupId, String mutator) {

        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerId, gender, initials, firstName, infix, lastName, email,
                    phoneNr, fax, dateOfBirth, productGroupId, mutator };

            recorder.accept(new FunctionCall("customerSavePrivateData", args));
        });
    }

    public static void customerSaveBusinessData(int customerId, String businessName, int businessTypeId,
                                                String gender, String initials, String firstName,
                                                String infix, String lastName, String email, String phoneNr,
                                                String fax, Date dateOfBirth, int productGroupId, String invoiceAttn,
                                                String invoiceEmail, String invoiceAnnotations, String mutator) {

        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerId, businessName, businessTypeId, gender, initials, firstName, infix,
                    lastName, email, phoneNr, fax, dateOfBirth, productGroupId, invoiceAttn,
                    invoiceEmail, invoiceAnnotations, mutator };

            recorder.accept(new FunctionCall("customerSaveBusinessData", args));
        });
    }

    public static void processTransponderCards(int customerId, String cardNr, String mutator, int updateMobileWithCard) {
        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerId, cardNr, mutator, updateMobileWithCard };

            recorder.accept(new FunctionCall("PROCESS_TRANSPONDERCARDS", args));
        });
    }

    public static void customerDeleteAddress(int addressId, String mutator) {
        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { addressId, mutator };

            recorder.accept(new FunctionCall("CustomerDeleteAddress", args));
        });
    }

    public static void getQcardNr(int customerId, String[] returnOut) {
        CALL_RECORDERS.forEach((recorder) -> {
            recorder.accept(new FunctionCall("getQcardNr", customerId));
        });

        returnOut[0] = TEST_QCARD_NUMBER;
    }

    public static class FunctionCall {

        public final String functionName;
        public final Object[] arguments;

        FunctionCall(String functionName, Object... arguments) {
            this.functionName = functionName;
            this.arguments = arguments;
        }

        public Number getNumericArg(int pos) {
            try {
                return NumberFormat.getInstance().parse(arguments[pos].toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
