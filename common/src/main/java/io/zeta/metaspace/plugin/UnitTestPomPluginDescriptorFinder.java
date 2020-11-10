package io.zeta.metaspace.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.pf4j.util.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 解析插件 pom 中的配置来创建  PluginDescriptor
 */
@Slf4j
public class UnitTestPomPluginDescriptorFinder extends PropertiesPluginDescriptorFinder {
    public static final String DEFAULT_POM_FILE_NAME = "pom.xml";

    protected String pomFileNam;

    public UnitTestPomPluginDescriptorFinder(String pomFileNam) {
        this.pomFileNam = pomFileNam;
    }

    public UnitTestPomPluginDescriptorFinder() {
        this(DEFAULT_POM_FILE_NAME);
    }


    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && (Files.isDirectory(pluginPath));
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        Model model = readPomXml(pluginPath);
        return createPluginDescriptor(model);
    }

    protected Model readPomXml(Path pluginPath) {
        Path propertiesPath = getPropertiesPath(pluginPath, pomFileNam);
        if (propertiesPath == null) {
            throw new PluginRuntimeException("Cannot find the properties path");
        }

        Model model = null;
        try {
            log.debug("Lookup plugin descriptor in '{}'", propertiesPath);
            if (Files.notExists(propertiesPath)) {
                throw new PluginRuntimeException("Cannot find '{}' path", propertiesPath);
            }

            try (InputStream input = Files.newInputStream(propertiesPath)) {
                model = new MavenXpp3Reader().read(input);
            } catch (Exception e) {
                throw new PluginRuntimeException(e);
            }
        } finally {
            FileUtils.closePath(propertiesPath);
        }

        return model;
    }

    protected Path getPropertiesPath(Path pluginPath, String propertiesFileName) {
        return pluginPath.resolve(Paths.get(propertiesFileName));
    }


    protected PluginDescriptor createPluginDescriptor(Model model) {
        return super.createPluginDescriptor(model.getProperties());
    }
}
