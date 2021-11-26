package io.zeta.metaspace.connector.oracle;

import org.testng.annotations.Test;

/**
 * @author 周磊
 * @version 1.0
 * @date 2021-11-15
 */
public class PassWordUtilsTest {
    
    @Test
    public void aesEncode() {
        System.out.println(PassWordUtils.aesEncode("MbAbN9anKsyOScjY"));
    }
    
    @Test
    public void aesDecode() {
        System.out.println(PassWordUtils.aesDecode("PryNkmHxgQ1B01bMQWktEHDNocuAEJeIrXutyCzmHTo="));
    }
}