package us.mn.state.health.eh.hep.dss.keycloak.scripts;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.changelogs.ChangeLogs;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning.VersionChangeLog;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.Optional;

public class RealmGenerator {
    public final static transient Logger LOGGER = Logger.getLogger(RealmGenerator.class.getName());

    public static void main(String[] args) throws IOException {
        String location = "C:/mdhapps/mdhappbk/dev/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/localrealm.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/nonprod.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/prod.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/prod-uat.properties";
        RealmParams params = RealmParams.from(location);

        Keycloak keycloak = Keycloak.getInstance(params.getUrl(), "master", params.getUser(), params.getPassword(), "admin-cli");

        createOrUpdateRealm(params, keycloak);
    }

    private static void createOrUpdateRealm(RealmParams params, Keycloak keycloak) {
        RealmRepresentation realm;
        try {
            realm = Helper.getRealm(keycloak, params).toRepresentation();
        } catch (NotFoundException e) {
            LOGGER.info(String.format("Realm %s missing...", params.getRealmName()));
            createRealm(params, keycloak);
            return;
        } catch (Exception e) {
            LOGGER.info(String.format("Realm %s missing...", params.getRealmName()));
            createRealm(params, keycloak);
            return;
        }
        updateRealm(params, keycloak, realm);
    }

    private static void updateRealm(RealmParams params, Keycloak keycloak, RealmRepresentation realm) {
        String version = realm.getAttributes().get("version");

        if (StringUtils.isBlank(version)) {
            throw new IllegalStateException(String.format("Realm %s has no version set!", params.getRealmName()));
        }

        Optional<VersionChangeLog> log = ChangeLogs.CHANGE_LOGS.stream()
                .filter(versionChangeLog -> versionChangeLog.getVersion().equals(version))
                .findAny();

        if (!log.isPresent()) {
            throw new IllegalStateException(String.format("Realm %s has version %s that can't be found in the changelog list!", params.getRealmName(), version));
        }

        //run the remaining logs to be executed
        int indexOfCurrentVersion = log
                .map(ChangeLogs.CHANGE_LOGS::indexOf)
                .get();

        if (indexOfCurrentVersion == ChangeLogs.CHANGE_LOGS.size() - 1) {
            LOGGER.info(String.format("Ream %s has the latest version %s", params.getRealmName(), version));
        }

        for (int i = 0; i < ChangeLogs.CHANGE_LOGS.size(); i++) {
            if (i > indexOfCurrentVersion) {
                ChangeLogs.CHANGE_LOGS.get(i).updateAndVersion(keycloak, params);
            }
        }
    }

    private static void createRealm(RealmParams params, Keycloak keycloak) {
        ChangeLogs.CHANGE_LOGS.forEach(versionChangeLog -> versionChangeLog.updateAndVersion(keycloak, params));
    }

}


