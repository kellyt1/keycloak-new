package us.mn.state.health.eh.hep.dss.keycloak.scripts.util;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.Objects;

import static com.google.common.collect.Collections2.filter;

public class Helper {

    public static Keycloak keycloak(RealmParams params) {
        Keycloak keycloak = Keycloak.getInstance(params.getUrl(), "master", params.getUser(), params.getPassword(), "admin-cli");
        return keycloak;
    }

    public static RealmResource getRealm(Keycloak keycloak, RealmParams params) {
        return keycloak.realms().realm(params.getRealmName());
    }

    public static ClientsResource getClients(Keycloak keycloak, RealmParams params) {
        return getRealm(keycloak, params).clients();
    }

    public static String getClientUuid(String clientId, Keycloak keycloak, RealmParams params) {
        String id = getClients(keycloak, params).findByClientId(clientId).get(0).getId();
        return id;
    }

    public static GroupRepresentation getGroup(String group_name, Keycloak keycloak, RealmParams params) {
        return filter(getRealm(keycloak, params).groups().groups(group_name, 0, 100), g -> {
            return Objects.equals(g.getName(), group_name);
        }).iterator().next();
    }
}
