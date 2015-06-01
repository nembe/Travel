package nl.yellowbrick.activation.bootstrap;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Configuration
public class LockConfiguration {

    @Bean
    @Profile("!test")
    public Function<String, Lock> hazelcastLockSupplier() {
        Config config = new Config();

        NetworkConfig networkConfig = config.getNetworkConfig();

        // disable AWS & multicast
        networkConfig.getJoin().getAwsConfig().setEnabled(false);
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        // only cluster within same host
        networkConfig.getInterfaces().addInterface("127.0.0.*");
        networkConfig.getJoin().getTcpIpConfig()
            .setEnabled(true)
            .addMember("127.0.0.1");

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        return hazelcastInstance::getLock;
    }

    @Bean
    @Profile("test")
    public Function<String, Lock> vanillaLockSupplier() {
        // use a vanilla lock instead of distributed lock in tests
        return lockName -> new ReentrantLock();
    }
}
