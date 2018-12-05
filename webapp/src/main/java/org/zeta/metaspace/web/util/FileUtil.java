package org.zeta.metaspace.web.util;

import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static String fileCode(InputStream input) throws IOException {
        byte[] buf = new byte[4096];
        InputStream fis = FileUtil.getInputStream(input);
        Throwable throwable = null;
        try {
            UniversalDetector detector = new UniversalDetector((CharsetListener) null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            detector.reset();
            String code = encoding;
            return code;
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            if (fis != null) {
                if (throwable != null) {
                    try {
                        fis.close();
                    } catch (Throwable var15) {
                        throwable.addSuppressed(var15);
                    }
                } else {
                    fis.close();
                }
            }
        }
    }

    public static InputStream getInputStream(InputStream input) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(input, Charset.forName("GBK"));
        ZipEntry zipEntry = null;
        do {
            if ((zipEntry = zipInputStream.getNextEntry()) == null) {
                zipInputStream.close();
                return new BufferedInputStream(input);
            }
        } while (zipEntry.isDirectory());
        return zipInputStream;
    }
}
