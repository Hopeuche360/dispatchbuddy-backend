package com.decagon.dispatchbuddy.config;
import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.AbstractSSMConfiguration;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl;
import com.google.code.ssm.providers.xmemcached.XMemcachedConfiguration;
import com.google.code.ssm.spring.ExtendedSSMCacheManager;
import com.google.code.ssm.spring.SSMCache;
import lombok.SneakyThrows;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.io.IOException;
import java.util.Arrays;

@RefreshScope
@Configuration
@EnableCaching
public class MemcachedConfig extends AbstractSSMConfiguration {

    @Value("${memcached.service.host}" )
    private String memcachedHost;

    @Value("${memcached.service.port}" )
    private int memcachedPort;

    Logger logger = LoggerFactory.getLogger(MemcachedConfig.class);

    @Bean
    public MemcachedClient memcachedClient() {

        MemcachedClient client = null;

        try {

            client = new XMemcachedClient(memcachedHost,memcachedPort);

        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.info("Memcached encountered an error : {}",e.getMessage());
        }

        return client;

    }

    @Bean
    @Override
    public CacheFactory defaultMemcachedClient() {
        String serverString = memcachedHost+":"+memcachedPort;
        final XMemcachedConfiguration conf = new XMemcachedConfiguration();
        conf.setUseBinaryProtocol(true);

        final CacheFactory cf = new CacheFactory();
        cf.setCacheClientFactory(new MemcacheClientFactoryImpl());
        cf.setAddressProvider(new DefaultAddressProvider(serverString));
        cf.setConfiguration(conf);
        return cf;
    }


    @Bean
    public CacheManagerCustomizer<ExtendedSSMCacheManager> cacheManagerCustomizer(){
        return cacheManager -> {
            Cache cache = null;
            try {
                cache = defaultMemcachedClient().getObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cacheManager.setCaches(Arrays.asList(new SSMCache(cache, 0, false)));
        };
    }

}
