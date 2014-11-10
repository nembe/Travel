package nl.yellowbrick.database;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is absolutely NOT thread-safe but that shouldn't matter for tests
 */
public class Functions {

    public static int VALIDATE_MEMBERSHIP_RETVAL = 1;

    public static List<Consumer<FunctionCall>> CALL_RECORDERS = new ArrayList<>();

    public static void customerValidateMembership(int customerId, String customerNr, int parkadammerTotal,
                                                  int numberOfTCards, int numberOfQCards, int creditLimit,
                                                  int subscriptionFee, int registrationFee, int initialTCardFee,
                                                  int additionalTCardFee, int initialRtpCardFee, int additionalRtpCardFee,
                                                  String pinCode, String password, String mutator,
                                                  Integer[] returnOut) {

        CALL_RECORDERS.forEach((recorder) -> {
            Object[] args = { customerId, customerNr, parkadammerTotal, numberOfTCards, numberOfQCards,
                    creditLimit, subscriptionFee, registrationFee, initialTCardFee, additionalTCardFee,
                    initialRtpCardFee, additionalRtpCardFee, pinCode, password, mutator };

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

    public static void cardOrderValidate(int customerId, int cardFee, int numberOfTCards,
                                         String typeOfCard, Integer[] returnOut) {
        CALL_RECORDERS.forEach((recorder) -> {
            recorder.accept(new FunctionCall("cardOrderValidate",
                    customerId, cardFee, numberOfTCards, typeOfCard));
        });

        returnOut[0] = -1;
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
