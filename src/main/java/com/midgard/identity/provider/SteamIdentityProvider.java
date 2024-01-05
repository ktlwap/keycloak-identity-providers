package com.midgard.identity.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.midgard.identity.provider.config.SteamIdentityProviderConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.http.entity.ContentType;
import org.keycloak.broker.provider.*;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamIdentityProvider extends AbstractIdentityProvider<SteamIdentityProviderConfig> implements SocialIdentityProvider<SteamIdentityProviderConfig> {

    private static final String AUTH_URL = "https://steamcommunity.com/openid/login";
    private static final String USER_SUMMARY_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2";
    private static final Pattern LOGIN_RESPONSE_PATTERN = Pattern.compile("^(ns:http:\\/\\/specs\\.openid\\.net\\/auth\\/2.0\\nis_valid:true\\n)$");
    private static final Pattern IDENTITY_PATTERN = Pattern.compile("(https://steamcommunity\\.com/openid/id/)([0-9]{17,25})");

    public SteamIdentityProvider(KeycloakSession session, SteamIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new SteamEndpoint(session, callback, event, this);
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        URI uri = UriBuilder.fromUri(AUTH_URL)
                .scheme("https")
                .queryParam("openid.ns", "http://specs.openid.net/auth/2.0")
                .queryParam("openid.identity", "http://specs.openid.net/auth/2.0/identifier_select")
                .queryParam("openid.claimed_id", "http://specs.openid.net/auth/2.0/identifier_select")
                .queryParam("openid.return_to", request.getRedirectUri() + "/callback?state=" + request.getState().getEncoded())
                .queryParam("openid.realm", request.getRedirectUri())
                .queryParam("openid.mode", "checkid_setup")
                .build();

        return Response.seeOther(uri).build();
    }

    @Override
    public Response retrieveToken(KeycloakSession keycloakSession, FederatedIdentityModel federatedIdentityModel) {
        return null;
    }

    protected static class SteamEndpoint {
        private final AuthenticationCallback callback;
        private final EventBuilder event;
        private final KeycloakSession session;
        private final SteamIdentityProvider provider;

        public SteamEndpoint(KeycloakSession session, AuthenticationCallback callback, EventBuilder event, SteamIdentityProvider provider) {
            this.session = session;
            this.callback = callback;
            this.event = event;
            this.provider = provider;
        }

        @GET
        @Path("callback")
        public Response authResponse(@QueryParam("state") String state,
                                     @QueryParam("openid.ns") String ns,
                                     @QueryParam("openid.mode") String mode,
                                     @QueryParam("openid.op_endpoint") String opEndpoint,
                                     @QueryParam("openid.claimed_id") String claimedId,
                                     @QueryParam("openid.identity") String identity,
                                     @QueryParam("openid.return_to") String returnTo,
                                     @QueryParam("openid.response_nonce") String responseNonce,
                                     @QueryParam("openid.assoc_handle") String assocHandle,
                                     @QueryParam("openid.invalidate_handle") String invalidateHandle,
                                     @QueryParam("openid.signed") String signed,
                                     @QueryParam("openid.sig") String sig) throws IOException {

            SimpleHttp request = SimpleHttp.doPost(AUTH_URL, session)
                    .header("Accept-Language", "en-US")
                    .header("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                    .param("openid.ns", ns)
                    .param("openid.op_endpoint", opEndpoint)
                    .param("openid.claimed_id", claimedId)
                    .param("openid.identity", identity)
                    .param("openid.return_to", returnTo)
                    .param("openid.response_nonce", responseNonce)
                    .param("openid.assoc_handle", assocHandle)
                    .param("openid.signed", signed)
                    .param("openid.sig", sig)
                    .param("openid.mode", "check_authentication");

            String response = request.asString();
            if (!LOGIN_RESPONSE_PATTERN.matcher(response).matches()) {
                return callback.error("Unable to verify authentication with Steam");
            }

            Matcher identityMatcher = IDENTITY_PATTERN.matcher(identity);
            String steamId;
            if (identityMatcher.matches()) {
                steamId = identityMatcher.group(2);
            } else {
                return callback.error("Unable to determine SteamId");
            }

            JsonNode node = SimpleHttp.doGet(USER_SUMMARY_URL, session)
                    .param("key", provider.getConfig().getApiKey())
                    .param("steamids", steamId)
                    .asJson();

            String username = node.get("response").get("players").get(0).get("personaname").asText();

            BrokeredIdentityContext federatedIdentity = new BrokeredIdentityContext(steamId);
            federatedIdentity.setIdp(provider);
            federatedIdentity.setIdpConfig(provider.getConfig());
            federatedIdentity.setAuthenticationSession(createAuthenticationSessionModel(state));
            federatedIdentity.setBrokerUserId(steamId);
            federatedIdentity.setUsername(steamId);
            federatedIdentity.setUserAttribute("steamId", steamId);
            federatedIdentity.setUsername(username);

            return callback.authenticated(federatedIdentity);
        }

        private AuthenticationSessionModel createAuthenticationSessionModel(String state) {
            RealmModel realm = session.getContext().getRealm();
            IdentityBrokerState idpState = IdentityBrokerState.encoded(state, realm);
            String clientId = idpState.getClientId();
            String tabId = idpState.getTabId();
            ClientModel client = realm.getClientByClientId(clientId);

            return ClientSessionCode.getClientSession(state, tabId, session, realm, client, event, AuthenticationSessionModel.class);
        }
    }
}
