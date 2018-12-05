package org.zeta.metaspace.web.timer;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.tinkerpop.shaded.minlog.Log;

import java.io.IOException;
import java.util.TimerTask;

public class KerberosReloginTimer extends TimerTask {

    @Override
    public void run() {
        try {
            if (UserGroupInformation.isLoginKeytabBased()) {
                UserGroupInformation.getLoginUser().reloginFromKeytab();
            } else if (UserGroupInformation.isLoginTicketBased()) {
                UserGroupInformation.getLoginUser().reloginFromTicketCache();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Log.warn("username=" + UserGroupInformation.getLoginUser().getUserName());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
