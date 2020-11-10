package io.zeta.metaspace.plugin;

import org.pf4j.DevelopmentPluginRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于单元测试插件查找，传入的插件根路径即为插件
 */
public class UnitTestPluginRepository extends DevelopmentPluginRepository {
    public UnitTestPluginRepository(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    public List<Path> getPluginPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(pluginsRoot);
        return paths;
    }
}
