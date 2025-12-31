package com.formation.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

/**
 * Configuration du cache Spring
 * Utilise ConcurrentMapCacheManager en mémoire pour le développement
 * En production, on peut utiliser Redis ou Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Cache manager en mémoire pour le développement
     * En production, utiliser Redis ou Caffeine pour de meilleures performances
     */
    @Bean
    @Profile("!prod")
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            "users",           // Cache pour les utilisateurs
            "students",        // Cache pour les étudiants
            "courses",         // Cache pour les cours
            "trainers",        // Cache pour les formateurs
            "specialties",     // Cache pour les spécialités
            "groups",          // Cache pour les groupes
            "sessions",        // Cache pour les sessions
            "statistics"       // Cache pour les statistiques
        ));
        return cacheManager;
    }
    
    /**
     * Pour la production, on peut utiliser Redis ou Caffeine
     * Exemple avec Caffeine (nécessite la dépendance):
     * 
     * @Bean
     * @Profile("prod")
     * public CacheManager caffeineCacheManager() {
     *     CaffeineCacheManager cacheManager = new CaffeineCacheManager();
     *     cacheManager.setCaffeine(Caffeine.newBuilder()
     *         .expireAfterWrite(10, TimeUnit.MINUTES)
     *         .maximumSize(1000));
     *     return cacheManager;
     * }
     */
}

