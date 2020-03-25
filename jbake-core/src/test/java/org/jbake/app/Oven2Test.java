package org.jbake.app;

import org.jbake.launcher.Main;
import org.junit.Test;

public class Oven2Test {

    @Test
    public void testAsciidoc() {
        Main.main(new String[]{"-b", "-s"});
    }
}
