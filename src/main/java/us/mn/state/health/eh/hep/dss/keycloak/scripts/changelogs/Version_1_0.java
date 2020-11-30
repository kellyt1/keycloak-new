package us.mn.state.health.eh.hep.dss.keycloak.scripts.changelogs;

import com.google.common.collect.Lists;
import org.keycloak.common.enums.SslRequired;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.RealmRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.WebAppRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.ApiUtil;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning.VersionChangeLog;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Sets.newHashSet;
import static us.mn.state.health.eh.hep.dss.keycloak.scripts.util.Helper.getRealm;

public class Version_1_0 implements VersionChangeLog {

    public static final String VERSION = "1.0";

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void update(Keycloak keycloak, RealmParams params) {
        run(keycloak, params);
    }

    public static void run(Keycloak keycloak, RealmParams params) {
        createRealm(keycloak, params);
        createWebAppClient(keycloak, params);
        createHepdataportalGroup(keycloak, params);
        createMonitoringRole(keycloak, params);
    }

    private static void createMonitoringRole(Keycloak keycloak, RealmParams params) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(RealmRoles.MONITORING);
        role.setDescription("Used for monitoring (Java Melody, etc.)");
        keycloak.realms().realm(params.getRealmName()).roles().create(role);
    }

    private static void createHepdataportalGroup(Keycloak keycloak, RealmParams params) {
        GroupRepresentation group = new GroupRepresentation();


        group.setName(RealmParams.HEPDATAPORTAL_ADMINS_GROUP);
        GroupsResource groups = Helper.getRealm(keycloak, params).groups();

        groups.add(group);

        GroupRepresentation theGroup = Helper.getGroup(RealmParams.HEPDATAPORTAL_ADMINS_GROUP, keycloak, params);

        List<RoleRepresentation> webAppRoles = Helper.getClients(keycloak, params).get(Helper.getClientUuid(RealmParams.WEBAPP_CLIENT_ID, keycloak, params)).roles().list();

        Collection<RoleRepresentation> rolesToBeAdded = filter(webAppRoles, r -> {
            return newHashSet(WebAppRoles.ADMIN.getRoleName()).contains(r.getName());
        });

        groups.group(theGroup.getId())
                .roles()
                .clientLevel(Helper.getClientUuid(RealmParams.WEBAPP_CLIENT_ID, keycloak, params))
                .add(Lists.newArrayList(rolesToBeAdded));

    }

    private static void createWebAppClient(Keycloak keycloak, RealmParams params) {
        ClientRepresentation client = new ClientRepresentation();
        //settings
        client.setClientId(RealmParams.WEBAPP_CLIENT_ID);
        client.setName(RealmParams.WEBAPP_CLIENT_ID);
        client.setPublicClient(Boolean.TRUE);
        client.setStandardFlowEnabled(Boolean.TRUE);
        client.setDirectAccessGrantsEnabled(Boolean.TRUE);
        client.setRedirectUris(params.getWebAppRedirectURIs());
        client.setBaseUrl(params.getBaseUrl());
        client.setWebOrigins(params.getWebOrginsURIsURIs());
//        client.setClientAuthenticatorType("client-secret");
        client.setDefaultRoles(params.getdefaultRoles());

        ClientsResource clients = Helper.getClients(keycloak, params);

        clients.create(client);

        String id = clients.findByClientId(RealmParams.WEBAPP_CLIENT_ID).get(0).getId();

        ClientResource clientResource = clients.get(id);



        //roles
        RoleRepresentation adminRole = new RoleRepresentation(
                WebAppRoles.ADMIN.getRoleName(),
                "Used By HEPdataportal users to upload files,etc.",
                false);

        RoleRepresentation userRole = new RoleRepresentation(
                WebAppRoles.USER.getRoleName(),
                "Used By HEPdataportal users to upload files,etc.",
                false);

        clientResource.roles().create(adminRole);
        clientResource.roles().create(userRole);

    }

    private static void createRealm(Keycloak keycloak,RealmParams realmParams) {
        RealmRepresentation realm = new RealmRepresentation();
        String realmName = realmParams.getRealmName();
        realm.setId(realmName);
        realm.setRealm(realmName);
        realm.setDisplayName("HEP Data Portal");
        realm.setEnabled(Boolean.TRUE);
        realm.setLoginTheme("mdh");
        realm.setEmailTheme("mdh");
        realm.setAccountTheme("mdh");
        realm.setAdminTheme("mdh");

        realm.setRegistrationAllowed(true);
        realm.setRegistrationEmailAsUsername(true);
        realm.setResetPasswordAllowed(true);
        realm.setSslRequired(SslRequired.ALL.toString());

        Map<String, String> smtpServer = new HashMap<>();
        smtpServer.put("host", "localhost");
        smtpServer.put("from", "do.not.reply.authenticator4.hepdataportal-notifications-realm@state.mn.us");

        realm.setSmtpServer(smtpServer);

        keycloak.realms().create(realm);

        enableEvents(keycloak, realmParams);
    }

    private static void enableEvents(Keycloak keycloak, RealmParams realmParams) {
        RealmRepresentation realm;
        //enable events
        RealmResource realmResource = getRealm(keycloak, realmParams);
        List<String> enabledEventTypes = realmResource.getRealmEventsConfig().getEnabledEventTypes();
        List<String> eventsListeners = realmResource.getRealmEventsConfig().getEventsListeners();
        realm = realmResource.toRepresentation();
        realm.setEventsEnabled(true);
        //270 days
        realm.setEventsExpiration(23328000);
        realm.setEventsListeners(eventsListeners);

        realm.setEnabledEventTypes(enabledEventTypes);

        realm.setAdminEventsEnabled(Boolean.TRUE);
        realm.setAdminEventsDetailsEnabled(Boolean.TRUE);
        realmResource.update(realm);
    }


}
