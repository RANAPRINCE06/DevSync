package com.devsync.coding.integration.registry;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Registry holding and dispatching platform-specific adapters dynamically,
 * eliminating conditional if-else / switch cascades across platform names.
 */
@Component
public class PlatformIntegrationRegistry {

    private final Map<CodingPlatform, CodingPlatformAdapter> adapterMap = new EnumMap<>(CodingPlatform.class);

    public PlatformIntegrationRegistry(List<CodingPlatformAdapter> adapters) {
        for (CodingPlatformAdapter adapter : adapters) {
            adapterMap.put(adapter.getPlatform(), adapter);
        }
    }

    /**
     * Resolves an adapter for the given coding platform if registered.
     */
    public Optional<CodingPlatformAdapter> getAdapter(CodingPlatform platform) {
        return Optional.ofNullable(adapterMap.get(platform));
    }

    /**
     * Resolves the required adapter for the platform or throws PlatformUnsupportedException.
     */
    public CodingPlatformAdapter getRequiredAdapter(CodingPlatform platform) {
        CodingPlatformAdapter adapter = adapterMap.get(platform);
        if (adapter == null) {
            throw new PlatformUnsupportedException("No integration adapter registered for platform: " + platform);
        }
        return adapter;
    }

    /**
     * Returns all registered adapters.
     */
    public Collection<CodingPlatformAdapter> getAllAdapters() {
        return Collections.unmodifiableCollection(adapterMap.values());
    }
}
