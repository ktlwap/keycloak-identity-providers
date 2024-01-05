package com.midgard.identity.provider.attribute.mapper;

import com.midgard.identity.provider.SteamIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class SteamUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] COMPATIBLE_PROVIDERS = new String[]{ SteamIdentityProviderFactory.STEAM_PROVIDER_ID };

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "steam-user-attribute-mapper";
    }
}
