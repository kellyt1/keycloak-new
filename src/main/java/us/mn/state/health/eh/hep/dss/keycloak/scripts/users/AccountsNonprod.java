package us.mn.state.health.eh.hep.dss.keycloak.scripts.users;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.WebAppRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.ApiUtil;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;

import static us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper.getRealm;
import static us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper.keycloak;

/**
 * Setting known secrets for nonprod
 */
public class AccountsNonprod {

    public static void main(String[] args) throws IOException {
        String location = "C:/mdhapps/mdhappbk/dev/keycloak/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/localrealm.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/keycloak/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/nonprod.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/keycloak/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/prod.properties";
//        String location = "C:/mdhapps/mdhappbk/dev/keycloak/workspace/hepdataportal/hepdataportal-keycloak-scripts/src/main/resources/prod-uat.properties";

        RealmParams params = RealmParams.from(location);

        RealmResource realm = Helper.getRealm(Helper.keycloak(params), params);

        ApiUtil.setClientSecret(realm, RealmParams.WEBAPP_CLIENT_ID, "f90bb7c1-f06e-48ab-827c-19d1f82598f5");
        createAdminUser(params, keycloak(params));
    }

    private static void createAdminUser(RealmParams params, Keycloak keycloak) {
        UserRepresentation admin = new UserRepresentation();
        admin.setUsername("admin");
        admin.setEnabled(Boolean.TRUE);
        RealmResource realm = getRealm(keycloak, params);

        String userId = ApiUtil.createUserWithAdminClient(realm, admin);
        ApiUtil.resetUserPassword(realm.users().get(userId), "123", false);
        ApiUtil.assignClientRoles(realm, userId, RealmParams.WEBAPP_CLIENT_ID, WebAppRoles.ADMIN.getRoleName());
        realm.users().get(userId).joinGroup(ApiUtil.findGroupByName(realm, RealmParams.HEPDATAPORTAL_ADMINS_GROUP).toRepresentation().getId());
    }
}
