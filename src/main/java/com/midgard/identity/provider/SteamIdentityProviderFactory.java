package com.midgard.identity.provider;

import com.midgard.identity.provider.config.SteamIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class SteamIdentityProviderFactory extends AbstractIdentityProviderFactory<SteamIdentityProvider>
        implements SocialIdentityProviderFactory<SteamIdentityProvider> {

    public static final String STEAM_PROVIDER_ID = "steam";
    private static final String STEAM = "Steam";

    @Override
    public String getId() {
        return STEAM_PROVIDER_ID;
    }

    @Override
    public String getName() {
        return STEAM;
    }

    @Override
    public SteamIdentityProvider create(KeycloakSession keycloakSession, IdentityProviderModel identityProviderModel) {
        return new SteamIdentityProvider(keycloakSession, new SteamIdentityProviderConfig(identityProviderModel));
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new SteamIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("steamApiKey")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Steam API Key")
                .required(true)
                .add()
                .build();
    }}
