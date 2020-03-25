package org.jbake.launcher;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class BakerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File rootPath;
    private DefaultJBakeConfiguration config;

    @Before
    public void setUp() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);

    }

    @Test
    public void bake() {
        Baker baker = new Baker();
        config.setTemplateFolder(new File(rootPath, "freemarkerTemplates"));
        baker.bake(config);
    }

    @Test
    public void testBake() {
    }
}
