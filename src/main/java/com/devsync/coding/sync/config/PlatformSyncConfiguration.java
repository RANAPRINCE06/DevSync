package com.devsync.coding.sync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration enabling scheduling for the automated coding synchronization engine.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(CodingSyncProperties.class)
public class PlatformSyncConfiguration {
}
