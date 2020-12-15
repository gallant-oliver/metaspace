package org.apache.atlas.model;

import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class DesensitizationAlgorithmTest {

    @Test
    public void testDesensitizationAlgorithm() {
        log.info(DesensitizationAlgorithm.MD5.getHandle().apply("test", Arrays.asList("DesensitizationAlgorithmKey")).toString());
        log.info(DesensitizationAlgorithm.SHA1.getHandle().apply("test", Arrays.asList("DesensitizationAlgorithmKey")).toString());
        log.info(DesensitizationAlgorithm.SHA256.getHandle().apply("test", Arrays.asList("DesensitizationAlgorithmKey")).toString());
        log.info(DesensitizationAlgorithm.HMAC.getHandle().apply("test", Arrays.asList("DesensitizationAlgorithmKey", "DesensitizationAlgorithmKey2")).toString());


        Assert.assertEquals(DesensitizationAlgorithm.NumericalTypeTruncation.getHandle().apply(1234567, Arrays.asList("4")).toString(), "1230000");
        Assert.assertEquals(DesensitizationAlgorithm.NumericalTypeTruncation.getHandle().apply(1234567, Arrays.asList("1")).toString(), "1234560");
        Assert.assertEquals(DesensitizationAlgorithm.NumericalTypeTruncation.getHandle().apply(1234567, Arrays.asList("9")).toString(), "0");


        Assert.assertEquals(DesensitizationAlgorithm.KeepOut.getHandle().apply(1234567, Arrays.asList("2", "2")).toString(), "12xxx67");
        Assert.assertEquals(DesensitizationAlgorithm.KeepOut.getHandle().apply(1234567, Arrays.asList("3", "4")).toString(), "1234567");
        Assert.assertEquals(DesensitizationAlgorithm.KeepOut.getHandle().apply(1234567, Arrays.asList("4", "4")).toString(), "1234567");


        Assert.assertEquals(DesensitizationAlgorithm.MaskOut.getHandle().apply(1234567, Arrays.asList("2", "2")).toString(), "xx345xx");
        Assert.assertEquals(DesensitizationAlgorithm.MaskOut.getHandle().apply(1234567, Arrays.asList("3", "4")).toString(), "xxxxxxx");
        Assert.assertEquals(DesensitizationAlgorithm.MaskOut.getHandle().apply(1234567, Arrays.asList("4", "4")).toString(), "xxxxxxx");

        Assert.assertEquals(DesensitizationAlgorithm.KeepTo.getHandle().apply(1234567, Arrays.asList("2", "5")).toString(), "x2345xx");
        Assert.assertEquals(DesensitizationAlgorithm.KeepTo.getHandle().apply(1234567, Arrays.asList("2", "2")).toString(), "x2xxxxx");
        Assert.assertEquals(DesensitizationAlgorithm.KeepTo.getHandle().apply(1234567, Arrays.asList("7", "9")).toString(), "xxxxxx7");
        Assert.assertEquals(DesensitizationAlgorithm.KeepTo.getHandle().apply(1234567, Arrays.asList("9", "9")).toString(), "xxxxxxx");
        Assert.assertEquals(DesensitizationAlgorithm.KeepTo.getHandle().apply(1234567, Arrays.asList("5", "2")).toString(), "xxxxxxx");

        Assert.assertEquals(DesensitizationAlgorithm.MaskTo.getHandle().apply(1234567, Arrays.asList("2", "5")).toString(), "1xxxx67");
        Assert.assertEquals(DesensitizationAlgorithm.MaskTo.getHandle().apply(1234567, Arrays.asList("2", "2")).toString(), "1x34567");
        Assert.assertEquals(DesensitizationAlgorithm.MaskTo.getHandle().apply(1234567, Arrays.asList("7", "9")).toString(), "123456x");
        Assert.assertEquals(DesensitizationAlgorithm.MaskTo.getHandle().apply(1234567, Arrays.asList("9", "9")).toString(), "1234567");
        Assert.assertEquals(DesensitizationAlgorithm.MaskTo.getHandle().apply(1234567, Arrays.asList("5", "2")).toString(), "1234567");


        Assert.assertEquals(DesensitizationAlgorithm.MaskTime.getHandle().apply("1987-02-24 12:50:44", Arrays.asList("yyyy-mm-dd hh:mm:ss", "yyyy-mm-'xx xx:xx:xx'")).toString(), "1987-50-xx xx:xx:xx");


        Assert.assertEquals(DesensitizationAlgorithm.MaskPhone.getHandle().apply("18812341234", null).toString(), "188xxxx1234");
        Assert.assertEquals(DesensitizationAlgorithm.MaskIdCard.getHandle().apply("110110199908083456", null).toString(), "110110xxxxxxxxxxxx");
        Assert.assertEquals(DesensitizationAlgorithm.MaskIp.getHandle().apply("192.168.100.68", null).toString(), "192.168.xxx.xxx");
        Assert.assertEquals(DesensitizationAlgorithm.MaskMac.getHandle().apply("0A:1S:2D:4F:5G:6H", null).toString(), "0A:1S:2D:xx:xx:xx");
        Assert.assertEquals(DesensitizationAlgorithm.MaskMac.getHandle().apply("0A-1S-2D-4F-5G-6H", null).toString(), "0A-1S-2D-xx-xx-xx");


    }
}
