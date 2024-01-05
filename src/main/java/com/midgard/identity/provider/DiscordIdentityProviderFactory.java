package com.midgard.identity.provider;

import com.midgard.identity.provider.config.DiscordIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class DiscordIdentityProviderFactory extends AbstractIdentityProviderFactory<DiscordIdentityProvider>
        implements SocialIdentityProviderFactory<DiscordIdentityProvider> {

    public static final String DISCORD_PROVIDER_ID = "discord";
    private static final String DISCORD = "Discord";

    @Override
    public String getId() {
        return DISCORD_PROVIDER_ID;
    }

    @Override
    public String getName() {
        return DISCORD;
    }

    @Override
    public DiscordIdentityProvider create(KeycloakSession keycloakSession, IdentityProviderModel identityProviderModel) {
        return new DiscordIdentityProvider(keycloakSession, new DiscordIdentityProviderConfig(identityProviderModel));
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new DiscordIdentityProviderConfig();
    }

}
