package com.midgard.identity.provider.config;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class SteamIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public SteamIdentityProviderConfig() {
        super();
    }

    public SteamIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public String getApiKey() {
        return getConfig().get("steamApiKey");
    }

    public void setApiKey(String key) {
        getConfig().put("steamApiKey", key);
    }
}
