package nl.yellowbrick.data.dao.impl;

import nl.yellowbrick.data.dao.WhitelistImportDao;
import nl.yellowbrick.data.domain.WhitelistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

import static nl.yellowbrick.data.dao.impl.ResultSetUtil.getLongOrNull;

@Component
public class WhitelistImportJdbcDao implements WhitelistImportDao {

    private static final Object[] NO_ARGS = {};

    @Autowired
    private JdbcTemplate template;

    @Override
    public void markAllAsObsolete() {
        template.update("update travelcard_whitelist_import set obsolete = 'Y'");
    }

    @Override
    public void deleteAllObsolete() {
        template.update("delete from travelcard_whitelist_import where obsolete = 'Y'");
    }

    @Override
    public Optional<WhitelistEntry> findByTravelcardNumber(String tcNumber) {
        String sql = "select * from travelcard_whitelist_import where tc_number = ?";

        return template.query(sql, rowMapper(), tcNumber).stream().findFirst();
    }

    @Override
    public void updateEntry(WhitelistEntry entry) {
        String sql = "update travelcard_whitelist_import " +
                "set license_plate = ?, transpondercardidfk = ?, obsolete = ? " +
                "where tc_number = ?";

        template.update(sql,
                entry.getLicensePlate(),
                entry.getTransponderCardId(),
                entry.isObsolete() ? "Y" : "N",
                entry.getTravelcardNumber());
    }

    @Override
    public void createEntry(WhitelistEntry entry) {
        String sql = "insert into travelcard_whitelist_import " +
                "(tc_number, license_plate, transpondercardidfk, obsolete, creation_date) " +
                "values(?, ?, ?, ?, SYSDATE)";

        template.update(sql,
                entry.getTravelcardNumber(),
                entry.getLicensePlate(),
                entry.getTransponderCardId(),
                entry.isObsolete() ? "Y" : "N");
    }

    @Override
    public void scanObsolete(Consumer<WhitelistEntry> callback) {
        String sql = "select * from travelcard_whitelist_import where obsolete = 'Y'";

        template.query(sql, NO_ARGS, mapForConsumer(callback));
    }

    private RowCallbackHandler mapForConsumer(Consumer<WhitelistEntry> callback) {
        RowMapper<WhitelistEntry> rowMapper = rowMapper();

        return rs -> {
            final WhitelistEntry obsoleteEntry = rowMapper.mapRow(rs, rs.getRow());
            callback.accept(obsoleteEntry);
        };
    }

    private RowMapper<WhitelistEntry> rowMapper() {
        return (rs, rowNum) -> {
            String tcNumber = rs.getString("tc_number");
            String licensePlate = rs.getString("license_plate");
            Long cardId = getLongOrNull(rs, "transpondercardidfk");
            boolean isObsolete = rs.getString("obsolete").equals("Y");

            WhitelistEntry entry = new WhitelistEntry(tcNumber, licensePlate, cardId);
            entry.setObsolete(isObsolete);

            return entry;
        };
    }
}
