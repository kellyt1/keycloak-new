package us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface VersionChangeLog {
    Logger log = Logger.getLogger(VersionChangeLog.class.getName());

    String getVersion();

    void update(Keycloak keycloak, RealmParams params);

    default void updateAndVersion(Keycloak keycloak, RealmParams params) {
        log.info(String.format("Update realm %s to version %s: Starting",params.getRealmName(), getVersion()));
        update(keycloak, params);
        setVersion(keycloak, params);
        log.info(String.format("Update realm %s to version %s: Finished",params.getRealmName(), getVersion()));
    }

    ;

    default void setVersion(Keycloak keycloak, RealmParams params) {
        RealmResource realm = keycloak.realms().realm(params.getRealmName());
        RealmRepresentation representation = realm.toRepresentation();
        Map<String, String> attributes = Optional.of(representation.getAttributes())
                .orElse(new HashMap<>());
        attributes.put("version", getVersion());
        representation.setAttributes(attributes);
        realm.update(representation);
    }

}
