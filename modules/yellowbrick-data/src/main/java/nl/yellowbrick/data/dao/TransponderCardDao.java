package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.TransponderCard;

import java.util.Optional;

public interface TransponderCardDao {

    void updateLicensePlate(long transponderCardId, String licensePlate);

    TransponderCard createCard(TransponderCard card);

    Optional<TransponderCard> findById(Long id);

    void cancelCard(long transponderCardId);
}
