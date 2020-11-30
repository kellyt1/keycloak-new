package us.mn.state.health.eh.hep.dss.keycloak.scripts.changelogs;

import us.mn.state.health.eh.hep.dss.keycloak.scripts.versioning.VersionChangeLog;

import java.util.Arrays;
import java.util.List;

public interface ChangeLogs {
    //order is very important
    List<VersionChangeLog> CHANGE_LOGS = Arrays.asList(
            new Version_1_0()
//            new Version_1_1(),
//            new Version_1_2()
    );
}
