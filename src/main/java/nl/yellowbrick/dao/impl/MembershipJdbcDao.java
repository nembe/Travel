package nl.yellowbrick.dao.impl;

import nl.yellowbrick.dao.MembershipDao;
import nl.yellowbrick.domain.Membership;
import org.springframework.stereotype.Component;

@Component
public class MembershipJdbcDao implements MembershipDao {

    @Override
    public void saveValidatedMembership(Membership membership) {
        // TODO implement

//        int initialTCardFeeCents = priceModel.getInitTranspCardCost();
//        int additionalTCardFeeCents = priceModel.getTranspCardCost();
//        int initialRTPCardFeeCents = priceModel.getInitRtpCardCost();
//        int additionalRTPCardFeeCents = priceModel.getRtpCardCost();
//        int qParkCardFeeCents = priceModel.getQparkPassCost();
//
//        String pinCode = new RandomPinCode().get();
//        String password = new RandomPassword().get();

//        sql.saveValidatedMembership( customerId, customerNr, parkadammerTotal, numberOfTCards, numberOfQCards, creditLimit*100, membershipFeeCents, registrationFeeCents, initialTCardFeeCents, additionalTCardFeeCents, initialRTPCardFeeCents, additionalRTPCardFeeCents, pinCode, password, mutator )
    }
}
