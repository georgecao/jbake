package org.jbake.app.configuration;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.jbake.TestUtils;
import org.jbake.app.JBakeException;
import org.jbake.app.LoggingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@ExtendWith(TempDirectory.class)
public class ConfigUtilTest extends LoggingTest {

    private Path sourceFolder;
    private ConfigUtil util;

    @BeforeEach
    public void setup(@TempDir Path folder) {
        this.sourceFolder = folder;
        this.util = new ConfigUtil();
    }

    @Test
    public void shouldLoadSiteHost() throws Exception {
        JBakeConfiguration config = util.loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        assertThat(config.getSiteHost()).isEqualTo("http://www.jbake.org");
    }

    @Test
    public void shouldLoadADefaultConfiguration() throws Exception {
        JBakeConfiguration config = util.loadConfig(TestUtils.getTestResourcesAsSourceFolder());
        assertDefaultPropertiesPresent(config);
    }

    @Test
    public void shouldLoadACustomConfiguration() throws Exception {
        File customConfigFile = new File(sourceFolder.toFile(), "jbake.properties");

        BufferedWriter writer = new BufferedWriter(new FileWriter(customConfigFile));
        writer.append("test.property=12345");
        writer.close();

        JBakeConfiguration configuration = util.loadConfig(sourceFolder.toFile());

        assertThat(configuration.get("test.property")).isEqualTo("12345");
        assertDefaultPropertiesPresent(configuration);
    }

    @Test
    public void shouldThrowAnExceptionIfSourcefolderDoesNotExist() throws Exception {
        File nonExistentSourceFolder = mock(File.class);
        when(nonExistentSourceFolder.getAbsolutePath()).thenReturn("/tmp/nonexistent");
        when(nonExistentSourceFolder.exists()).thenReturn(false);

        try {
            util.loadConfig(nonExistentSourceFolder);
            fail("Exception should be thrown, as source folder does not exist");
        } catch (JBakeException e) {

            assertThat(e.getMessage()).isEqualTo("The given source folder '/tmp/nonexistent' does not exist.");
        }
    }

    @Test
    public void shouldAddSourcefolderToConfiguration() throws Exception {

        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getSourceFolder()).isEqualTo(sourceFolder);

    }

    @Test
    public void shouldThrowAnExceptionIfSourcefolderIsNotADirectory() throws Exception {

        File sourceFolder = mock(File.class);
        when(sourceFolder.exists()).thenReturn(true);
        when(sourceFolder.isDirectory()).thenReturn(false);

        try {
            util.loadConfig(sourceFolder);
            fail("Exception should be thrown if given source folder is not a directory.");
        } catch (JBakeException e) {
            assertThat(e.getMessage()).isEqualTo("The given source folder is not a directory.");
        }

    }

    @Test
    public void shouldReturnDestinationFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "output");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getDestinationFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnAssetFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "assets");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getAssetFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnTemplateFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "templates");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getTemplateFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldReturnContentFolderFromConfiguration() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedDestinationFolder = new File(sourceFolder, "content");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        assertThat(config.getContentFolder()).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldGetTemplateFileDoctype() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        File expectedTemplateFile = new File(sourceFolder, "templates/index.ftl");
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        File templateFile = config.getTemplateFileByDocType("masterindex");

        assertThat(templateFile).isEqualTo(expectedTemplateFile);
    }

    @Test
    public void shouldLogWarningIfDocumentTypeNotFound() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        JBakeConfiguration config = util.loadConfig(sourceFolder);

        config.getTemplateFileByDocType("none");

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("Cannot find configuration key '{}' for document type '{}'");

    }

    @Test
    public void shouldGetTemplateOutputExtension() throws Exception {

        String docType = "masterindex";
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setTemplateExtensionForDocType(docType, ".xhtml");

        String extension = config.getOutputExtensionByDocType(docType);

        assertThat(extension).isEqualTo(".xhtml");
    }

    @Test
    public void shouldGetMarkdownExtensionsAsList() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> markdownExtensions = config.getMarkdownExtensions();

        assertThat(markdownExtensions).containsExactly("HARDWRAPS", "AUTOLINKS", "FENCED_CODE_BLOCKS", "DEFINITIONS");
    }

    @Test
    public void shouldReturnConfiguredDocTypes() throws Exception {

        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        List<String> docTypes = config.getDocumentTypes();

        assertThat(docTypes).containsExactly("allcontent", "masterindex", "feed", "archive", "tag", "tagsindex", "sitemap", "post", "page");

    }

    @Test
    public void shouldReturnAListOfAsciidoctorOptionsKeys() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        List<String> options = config.getAsciidoctorOptionKeys();

        assertThat(options).contains("requires", "template_dirs");
    }

    @Test
    public void shouldReturnAnAsciidoctorOption() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("requires");

        assertThat(String.valueOf(option)).contains("asciidoctor-diagram");
    }

    @Test
    public void shouldReturnAnAsciidoctorOptionWithAListValue() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        config.setProperty("asciidoctor.option.template_dirs", "src/template1,src/template2");

        Object option = config.getAsciidoctorOption("template_dirs");

        assertTrue(option instanceof List);
        assertThat((List<String>) option).contains("src/template1", "src/template2");
    }

    @Test
    public void shouldReturnEmptyStringIfOptionNotAvailable() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        Object option = config.getAsciidoctorOption("template_dirs");

        assertThat(String.valueOf(option)).isEmpty();
    }

    @Test
    public void shouldLogAWarningIfAsciidocOptionCouldNotBeFound() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);

        config.getAsciidoctorOption("template_dirs");

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());

        LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getMessage()).isEqualTo("Cannot find asciidoctor option '{}.{}'");
    }

    @Test
    public void shouldHandleNonExistingFiles() throws Exception {

        File source = TestUtils.getTestResourcesAsSourceFolder();
        File expectedTemplateFolder = new File(source, "templates");
        File expectedAssetFolder = new File(source, "assets");
        File expectedContentFolder = new File(source, "content");
        File expectedDestinationFolder = new File(source, "output");
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        config.setTemplateFolder(null);
        config.setAssetFolder(null);
        config.setContentFolder(null);
        config.setDestinationFolder(null);

        File templateFolder = config.getTemplateFolder();
        File assetFolder = config.getAssetFolder();
        File contentFolder = config.getContentFolder();
        File destinationFolder = config.getDestinationFolder();

        assertThat(templateFolder).isEqualTo(expectedTemplateFolder);
        assertThat(assetFolder).isEqualTo(expectedAssetFolder);
        assertThat(contentFolder).isEqualTo(expectedContentFolder);
        assertThat(destinationFolder).isEqualTo(expectedDestinationFolder);
    }

    @Test
    public void shouldHandleCustomTemplateFolder() throws Exception {
        File source = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        config.setTemplateFolder(TestUtils.newFolder(sourceFolder.toFile(), "my_custom_templates"));
        assertThat(config.getTemplateFolderName()).isEqualTo("my_custom_templates");
    }

    @Test
    public void shouldHandleCustomContentFolder() throws Exception {
        File source = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        config.setContentFolder(TestUtils.newFolder(sourceFolder.toFile(), "my_custom_content"));

        assertThat(config.getContentFolderName()).isEqualTo("my_custom_content");
    }

    @Test
    public void shouldHandleCustomAssetFolder() throws Exception {
        File source = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(source);

        config.setAssetFolder(TestUtils.newFolder(sourceFolder.toFile(), "my_custom_asset"));

        assertThat(config.getAssetFolderName()).isEqualTo("my_custom_asset");
    }

    private void assertDefaultPropertiesPresent(JBakeConfiguration config) throws IllegalAccessException {
        for (Field field : JBakeConfiguration.class.getFields()) {

            if (field.isAccessible()) {
                String key = (String) field.get("");
                System.out.println("Key: " + key);
                assertThat(config.get(key)).isNotNull();
            }
        }
    }

    @Test
    public void loadPropertiesUtf8Encoding() throws Exception {
        File sourceFolder = TestUtils.getTestResourcesAsSourceFolder();
        DefaultJBakeConfiguration config = (DefaultJBakeConfiguration) util.loadConfig(sourceFolder);
        boolean yes = false;
        Iterator<String> it = config.getKeys();
        while (it.hasNext()) {
            String key = it.next();
            if ((yes = "site.about".equals(key))) {
                break;
            }
        }
        assertThat(yes).isTrue();
    }
}
