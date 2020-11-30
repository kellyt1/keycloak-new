package us.mn.state.health.eh.hep.dss.keycloak.scripts.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RealmParams {
    String url = "http://localhost:5555/auth";
    String user = "";
    String password = "";

    public static final String WEBAPP_CLIENT_ID = "HEPDataPortal";
    public static final String HEPDATAPORTAL_ADMINS_GROUP = "HEPDATAPORTAL Admins";

    String realmName = "HEPDataPortal-apps-realm-test";
    List<String> webAppRedirectURIs = Lists.newArrayList("http://localhost:8080/*","https://hepdataPortalui.nonprod.health.state.mn.us/*");
    List<String> webOrginsURIs = Lists.newArrayList("*");
    String[] defaultRoles = {"USER"};
    String baseUrl = "http://localhost:8080";


    public static RealmParams from(String fileLocation) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(fileLocation));
        return RealmParams.from(properties);
    }

    public static RealmParams from(Properties properties) {
        RealmParams params = new RealmParams();
        params
                .withLocation(properties.getProperty("keycloak.url", params.url))
                .withUser(properties.getProperty("keycloak.user", params.user))
                .withPassword(properties.getProperty("keycloak.password", params.password))

                .withRealmName(properties.getProperty("realm-name", params.realmName))
                .withWebAppRedirectURIs(convertToList(properties.getProperty("webapp.redirect-uris"), params.webAppRedirectURIs))
                .withBaseUrl(properties.getProperty("webapp.base-url", params.baseUrl))
        ;
        return params;
    }

    private static List<String> convertToList(String value, List<String> defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        Set<String> values = Sets.newHashSet(value.split(","));
        values.remove(null);
        return values.stream()
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
    }

    public RealmParams withLocation(String location) {
        this.url = location;
        return this;
    }

    public RealmParams withUser(String user) {
        this.user = user;
        return this;
    }

    public RealmParams withPassword(String password) {
        this.password = password;
        return this;
    }

    public RealmParams withRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }

    public RealmParams withWebAppRedirectURIs(List<String> webAppRedirectURIs) {
        this.webAppRedirectURIs = webAppRedirectURIs;
        return this;
    }

    public RealmParams withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRealmName() {
        return realmName;
    }

    public List<String> getWebAppRedirectURIs() {
        return webAppRedirectURIs;
    }

    public List<String> getWebOrginsURIsURIs() {
        return webOrginsURIs;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String[] getdefaultRoles() {
        return defaultRoles;
    }

}
