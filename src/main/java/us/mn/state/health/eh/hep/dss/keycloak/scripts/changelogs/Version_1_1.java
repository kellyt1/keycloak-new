package us.mn.state.health.eh.hep.dss.keycloak.scripts.changelogs;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.WebAppRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning.VersionChangeLog;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.representations.idm.RoleRepresentation;

public class Version_1_1 implements VersionChangeLog {

    public static final String VERSION = "1.1";

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void update(Keycloak keycloak, RealmParams params) {
        run(keycloak, params);
    }

    public static void run(Keycloak keycloak, RealmParams params) {
        addSendSmsRole(keycloak, params);
    }

    private static void addSendSmsRole(Keycloak keycloak, RealmParams params) {
        ClientsResource clients = Helper.getClients(keycloak, params);
        String id = clients.findByClientId(RealmParams.WEBAPP_CLIENT_ID).get(0).getId();
        ClientResource clientResource = clients.get(id);
        RoleRepresentation sendSmsRole = new RoleRepresentation(
                WebAppRoles.ADMIN.getRoleName(),
                "Sends generic SMS notification",
                false);

        clientResource.roles().create(sendSmsRole);
    }
}
