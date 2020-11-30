package us.mn.state.health.eh.hep.dss.keycloak.scripts.users;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.WebAppRoles;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.config.RealmParams;
import us.mn.state.health.eh.hep.dss.keycloak.scripts.util.ApiUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.util.*;

/**
 * This will setup some users for a realm that will be created and exported for end to end tests.
 * <p>
 * It contains: <br/>
 * <ul>
 *     <li>a user for each web app role with the role as the username, password 123</li>
 *     <li>set the secret for client 'hepdataportal-notifications-webapp' to 123</li>
 * </ul>
 */
public class TestRealmUsers {

    public static final String PASSWORD = "123";

    public static void main(String[] args) throws IOException {
        String location = "/home/ochial1/dev/projects/IDEPC/hepdataportal-notifications-keycloak-scripts/src/main/resources/localrealm.properties";
        RealmParams params = RealmParams.from(location);
        Keycloak keycloak = Keycloak.getInstance(params.getUrl(), "master", params.getUser(), params.getPassword(), "admin-cli");
        RealmResource realm = keycloak.realms().realm(params.getRealmName());

        List<CreateUserCommand> userCommands = new ArrayList<>();

        Arrays.stream(WebAppRoles.values())
                .forEach(webAppRole -> addUserForWebAppRole(userCommands, webAppRole));


        userCommands.forEach(command -> {
            createUser(realm, command);
        });

        ApiUtil.setClientSecret(realm, RealmParams.WEBAPP_CLIENT_ID,"123");

        makeAdminCliFullyScoped(realm);
    }

    private static void makeAdminCliFullyScoped(RealmResource realm) {
        ClientResource adminClientResource = ApiUtil.findClientByClientId(realm, "admin-cli");
        ClientRepresentation adminCli = adminClientResource.toRepresentation();
        adminCli.setFullScopeAllowed(Boolean.TRUE);
        adminClientResource.update(adminCli);
    }

    private static void addUserForWebAppRole(List<CreateUserCommand> userCommands, WebAppRoles role) {
        userCommands.add(
                new CreateUserCommand().withUsername(role.name())
                        .withPassword(PASSWORD)
                        .withClientRoles(RealmParams.WEBAPP_CLIENT_ID, new String[]{role.getRoleName()})
        );
    }

    private static void createUser(RealmResource realm, CreateUserCommand command) {
        UserRepresentation user = new UserRepresentation();
//        user.setUsername(command.username);
//        user.setEnabled(Boolean.TRUE);
//        Response response = realm.users().create(user);
//
        user = ApiUtil.findUserByUsername(realm, command.username);
        UserResource userByUsernameId = ApiUtil.findUserByUsernameId(realm, command.username);
        ApiUtil.resetUserPassword(userByUsernameId, command.password, false);

        ApiUtil.assignRealmRoles(realm, user.getId(), command.realmRoles);

        UserRepresentation finalUser = user;

        command.clientRoles.forEach((clientName, clientRoles) -> {
            ApiUtil.assignClientRoles(realm, finalUser.getId(), clientName, clientRoles);
        });


    }

    static class CreateUserCommand {
        String username;
        String password = "123";
        Map<String, String[]> clientRoles = new HashMap<>();
        String[] realmRoles = {};

        public CreateUserCommand withUsername(String username) {
            this.username = username;
            return this;
        }

        public CreateUserCommand withPassword(String password) {
            this.password = password;
            return this;
        }

        public CreateUserCommand withClientRoles(String clientName, String[] clientRoles) {
            this.clientRoles.put(clientName, clientRoles);
            return this;
        }

        public CreateUserCommand withRealmRoles(String[] realmRoles) {
            this.realmRoles = realmRoles;
            return this;
        }
    }

}
