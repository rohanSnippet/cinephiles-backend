package com.projects.cinephiles.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Hibernate6Module hibernateModule = new Hibernate6Module();
        // 1. Force load the data from DB
        hibernateModule.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true);
        // 2. NEW: Force Jackson to save it as a standard ArrayList instead of a PersistentBag!
        hibernateModule.configure(Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS, true);

        objectMapper.registerModule(hibernateModule);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // FIX: Ignores unknown properties so Spring Security 'UserDetails' boolean getters don't crash deserialization
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. BASE CONFIGURATION: This will apply to all caches by default (e.g., 12 hours)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(serializer));

        // 4. SPECIFIC CONFIGURATIONS: Override the TTL for specific @Cacheable(value = "...") names
        Map<String, RedisCacheConfiguration> specificCacheConfigs = new HashMap<>();

        // Example: The "user" cache expires in 30 minutes
        specificCacheConfigs.put("user", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Example: If you have a "movies" cache, make it expire in 2 days
        specificCacheConfigs.put("movies", defaultConfig.entryTtl(Duration.ofDays(2)));

        // Example: Theatres might change less frequently, set to 5 hours
        specificCacheConfigs.put("theatres", defaultConfig.entryTtl(Duration.ofHours(5)));

        // 5. BUILD THE CACHE MANAGER
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(specificCacheConfigs)
                .build();
    }
}