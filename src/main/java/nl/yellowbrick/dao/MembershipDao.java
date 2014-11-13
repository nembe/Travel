package nl.yellowbrick.dao;

import nl.yellowbrick.domain.Membership;

public interface MembershipDao {

    public void saveValidatedMembership(Membership membership);
}
