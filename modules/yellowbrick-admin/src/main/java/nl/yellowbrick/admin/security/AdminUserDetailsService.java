package nl.yellowbrick.admin.security;

import nl.yellowbrick.data.dao.AdministratorDao;
import nl.yellowbrick.data.domain.Administrator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public class AdminUserDetailsService implements UserDetailsService {

    private static final List<GrantedAuthority> AUTHORITIES = AuthorityUtils.createAuthorityList("ROLE_USER");

    private final AdministratorDao administratorDao;

    public AdminUserDetailsService(AdministratorDao administratorDao) {
        this.administratorDao = administratorDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Administrator> maybeAdmin = administratorDao.findByUsername(username);

        return maybeAdmin
                .map((admin) -> new User(admin.getUsername(), admin.getPassword(), AUTHORITIES))
                .orElseThrow(() -> new UsernameNotFoundException("Couldn't find user with account named " + username));
    }
}
