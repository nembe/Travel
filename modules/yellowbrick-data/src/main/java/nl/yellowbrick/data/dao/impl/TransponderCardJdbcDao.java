package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.audit.Mutator;
import nl.yellowbrick.data.dao.TransponderCardDao;
import nl.yellowbrick.data.domain.CardStatus;
import nl.yellowbrick.data.domain.TransponderCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransponderCardJdbcDao implements TransponderCardDao {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private Mutator mutator;

    @Override
    public void updateLicensePlate(long transponderCardId, String licensePlate) {
        String sql = "update transpondercard set licenseplate = ? where transpondercardid = ?";

        template.update(sql, licensePlate, transponderCardId);
    }

    @Override
    public TransponderCard createCard(TransponderCard card) {
        String sql = "insert into transpondercard " +
                "(transpondercardid, customeridfk, cardnr, licenseplate, licenseplatecountry, " +
                "cardstatusidfk, mutator, mutation_date, statuslastchanged) " +
                "values (?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";

        Long id = template.queryForObject("select transpondercard_seq.nextval from dual", Long.class);

        template.update(sql,
                id,
                card.getCustomerId(),
                card.getCardNumber(),
                card.getLicenseplate(),
                card.getCountry(),
                card.getStatus().code(),
                mutator.get());

        return findById(id).get();
    }

    public Optional<TransponderCard> findById(Long id) {
        String sql = "select * from transpondercard where transpondercardid = ?";

        return template.query(sql, rowMapper(), id).stream().findFirst();
    }

    @Override
    public void cancelCard(long transponderCardId) {
        String sql = "update transpondercard set cardstatusidfk = ? where transpondercardid = ?";

        template.update(sql, CardStatus.INACTIVE.code(), transponderCardId);
    }

    private RowMapper<TransponderCard> rowMapper() {
        return (rs, rowNum) -> {
            TransponderCard card = new TransponderCard();
            card.setId(rs.getLong("transpondercardid"));
            card.setCustomerId(rs.getLong("customeridfk"));
            card.setCardNumber(rs.getString("cardnr"));
            card.setStatus(CardStatus.byCode(rs.getInt("cardstatusidfk")));
            card.setLicenseplate(rs.getString("licenseplate"));
            card.setCountry(rs.getString("licenseplatecountry"));
            card.setMutator(rs.getString("mutator"));
            card.setMutationDate(rs.getDate("mutation_date"));

            return card;
        };
    }
}
