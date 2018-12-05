// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
package org.zeta.metaspace.web.listeners;

import org.apache.atlas.web.listeners.LoginProcessor;
import org.zeta.metaspace.web.timer.KerberosReloginTimer;
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
