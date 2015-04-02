package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.TransponderCard;
import nl.yellowbrick.data.domain.UserAccountType;

import java.time.LocalDateTime;

public interface SystemUserDao {

    String createAndStoreUserToken(Customer customer, LocalDateTime validity);

    void deleteAppUserByCardId(long transponderCardId);

    void createAppUser(TransponderCard card, String username, String password, UserAccountType accountType);

    boolean existsAppUserForCard(long transponderCardId);
}
