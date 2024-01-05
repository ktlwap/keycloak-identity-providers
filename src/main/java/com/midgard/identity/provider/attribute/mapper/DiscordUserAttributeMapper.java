package com.midgard.identity.provider.attribute.mapper;

import com.midgard.identity.provider.DiscordIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class DiscordUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] COMPATIBLE_PROVIDERS = new String[]{ DiscordIdentityProviderFactory.DISCORD_PROVIDER_ID };

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "discord-user-attribute-mapper";
    }
}
