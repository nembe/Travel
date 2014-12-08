package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Membership;

public interface MembershipDao {

    public void saveValidatedMembership(Membership membership);
}
