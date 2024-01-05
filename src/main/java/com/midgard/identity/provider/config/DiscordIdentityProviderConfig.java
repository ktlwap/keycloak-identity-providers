package com.midgard.identity.provider.config;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class DiscordIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public DiscordIdentityProviderConfig() {
        super();
    }

    public DiscordIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

}
