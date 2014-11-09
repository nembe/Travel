package nl.yellowbrick.database;

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

    public static class FunctionCall {

        public final String functionName;
        public final Object[] arguments;

        FunctionCall(String functionName, Object[] arguments) {
            this.functionName = functionName;
            this.arguments = arguments;
        }
    }
}
