package org.jbake.app.configuration;

import org.jbake.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultJBakeConfigurationTest {

    private File rootPath;
    private DefaultJBakeConfiguration conf;

    @Before
    public void setUp() throws Exception {
        rootPath = TestUtils.getTestResourcesAsSourceFolder();
        ConfigUtil cu = new ConfigUtil();
        conf = (DefaultJBakeConfiguration) cu.loadConfig(rootPath);
    }

    @Test
    public void getImgPathPrependHost() {
        assertThat(conf.getImgPathPrependHost()).isEqualTo(conf.getRelativePathPrependHost());
        assertThat(conf.getRelativePathPrependHost()).isEqualTo(conf.getImgPathPrependHost());
    }

    @Test
    public void setImgPathPrependHost() {
        conf.setImgPathPrependHost(true);
        getImgPathPrependHost();
        conf.setImgPathPrependHost(false);
        getImgPathPrependHost();
    }

    @Test
    public void getImgPathUpdate() {
        assertThat(conf.getImgPathUpdate()).isEqualTo(conf.getRelativePathUpdate());
        assertThat(conf.getRelativePathUpdate()).isEqualTo(conf.getImgPathUpdate());
    }

    @Test
    public void setImgPathUpdate() {
        conf.setImgPathUpdate(true);
        getImgPathUpdate();
        conf.setImgPathUpdate(false);
        getImgPathUpdate();
    }

    @Test
    public void getRelativePathUpdate() {
        assertThat(conf.getRelativePathUpdate()).isEqualTo(conf.getImgPathUpdate());
        assertThat(conf.getImgPathUpdate()).isEqualTo(conf.getRelativePathUpdate());
    }

    @Test
    public void setRelativePathUpdate() {
        conf.setRelativePathUpdate(true);
        getRelativePathUpdate();
        conf.setRelativePathUpdate(false);
        getRelativePathUpdate();
    }

    @Test
    public void getRelativePathPrependHost() {
        assertThat(conf.getRelativePathPrependHost()).isEqualTo(conf.getImgPathPrependHost());
        assertThat(conf.getImgPathPrependHost()).isEqualTo(conf.getRelativePathPrependHost());
    }

    @Test
    public void setRelativePathPrependHost() {
        conf.setRelativePathPrependHost(true);
        getRelativePathPrependHost();
        conf.setRelativePathPrependHost(false);
        getRelativePathPrependHost();
    }

    @Test
    public void testDefaultValue() {
        boolean yes = conf.getImgPathUpdate();
        assertThat(yes).isTrue();
        boolean no = conf.getImgPathPrependHost();
        assertThat(no).isFalse();

        yes = conf.getRelativePathUpdate();
        assertThat(yes).isTrue();
        no = conf.getRelativePathPrependHost();
        assertThat(no).isFalse();
    }
}
