package nl.yellowbrick.data.dao;

import nl.yellowbrick.data.domain.Administrator;

import java.util.Optional;

public interface AdministratorDao {

    Optional<Administrator> findByUsername(String username);
}
