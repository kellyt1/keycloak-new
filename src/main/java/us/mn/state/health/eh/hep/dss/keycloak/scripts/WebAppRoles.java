package us.mn.state.health.eh.hep.dss.keycloak.scripts;

import com.google.common.collect.Lists;

import java.util.List;

public enum WebAppRoles {
    ADMIN("ADMIN"),
    USER("USER")
    ;
    String roleName;

    WebAppRoles(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }


}
