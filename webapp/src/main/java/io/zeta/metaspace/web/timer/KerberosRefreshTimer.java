package io.zeta.metaspace.web.timer;

import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;

public class KerberosRefreshTimer extends TimerTask {
    public static final Logger LOG = LoggerFactory.getLogger(KerberosRefreshTimer.class);
    private final int maxRetries = 3;

    @Override
    public void run() {
        for (int i = 1; i <= maxRetries; i++) {
            try {
                LOG.info("对username=" + UserGroupInformation.getLoginUser().getUserName()+"进行续约");
                if (UserGroupInformation.isLoginKeytabBased()) {
                    UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
                } else if (UserGroupInformation.isLoginTicketBased()) {
                    UserGroupInformation.getLoginUser().reloginFromTicketCache();
                }
                break;
            } catch (IOException e) {
                LOG.error("刷新kerberos认证失败", e);
                try {
                    Thread.sleep(1000 * i);
                } catch (InterruptedException ex) {
                    LOG.error("KerberosRefreshTimer thread sleep exception", ex);
                }
            }
        }
    }
}
