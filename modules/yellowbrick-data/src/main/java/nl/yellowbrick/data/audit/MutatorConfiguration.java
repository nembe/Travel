package nl.yellowbrick.data.audit;


import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class MutatorConfiguration {

    @Bean
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    public Mutator securityContextAwareMutator(@Value("${mutator}") String mutator) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            return authentication != null ? Joiner.on(":").join(mutator, authentication.getName()) : mutator;
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public Mutator staticMutator(@Value("${mutator}") String mutator) {
        return () -> mutator;
    }
}
