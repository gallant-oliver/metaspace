package org.apache.atlas.web.listeners;

import org.apache.atlas.web.timer.KerberosReloginTimer;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import java.util.Timer;

public class KerberosReloginListener extends ContextLoaderListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LoginProcessor loginProcessor = new LoginProcessor();
        loginProcessor.login();
        KerberosReloginTimer kerberosReloginTimer = new KerberosReloginTimer();
        Timer timer = new Timer();
        timer.schedule(kerberosReloginTimer, 60 * 60 * 1000, 60 * 60 * 1000);
        super.contextInitialized(servletContextEvent);
    }


}
