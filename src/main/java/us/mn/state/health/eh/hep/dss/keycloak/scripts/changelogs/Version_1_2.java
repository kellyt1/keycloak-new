package us.mn.state.health.eh.hep.dss.keycloak.scripts.changelogs;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.WebAppRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.ApiUtil;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning.VersionChangeLog;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import static us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper.getRealm;

public class Version_1_2 implements VersionChangeLog {

    public static final String VERSION = "1.2";

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void update(Keycloak keycloak, RealmParams params) {
        run(keycloak, params);
    }

    public static void run(Keycloak keycloak, RealmParams params) {
        addNewClient(keycloak, params);
    }

    private static void addNewClient(Keycloak keycloak, RealmParams params) {
        ClientRepresentation client = new ClientRepresentation();

        String clientId = RealmParams.WEBAPP_CLIENT_ID;

        client.setClientId(clientId);
        client.setName(clientId);
        client.setPublicClient(Boolean.FALSE);
        client.setEnabled(true);
        client.setServiceAccountsEnabled(true);


        ClientsResource clients = Helper.getClients(keycloak, params);
        clients.create(client);

        String id = clients.findByClientId(clientId).get(0).getId();
        ClientResource clientResource = clients.get(id);
        UserRepresentation serviceAccountUser = clientResource.getServiceAccountUser();
        ApiUtil.assignClientRoles(getRealm(keycloak, params), serviceAccountUser.getId(), RealmParams.WEBAPP_CLIENT_ID, WebAppRoles.USER.getRoleName());
    }
}
