package org.jbake.app.configuration;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jbake.app.JBakeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.function.BiConsumer;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class ConfigUtil {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private static final String LEGACY_CONFIG_FILE = "custom.properties";
    private static final String CONFIG_FILE = "jbake.properties";
    private static final String DEFAULT_CONFIG_FILE = "default.properties";
    private static final char DELIMITER = ',';
    private static final ListDelimiterHandler LIST_HANDLER = new DefaultListDelimiterHandler(DELIMITER);

    private Configuration readAsConf(File file) throws ConfigurationException {
        return readAsConf(file, FileBasedBuilderProperties::setFile);
    }

    private Configuration readAsConf(URL url) throws ConfigurationException {
        return readAsConf(url, FileBasedBuilderProperties::setURL);
    }

    private <S> Configuration readAsConf(S source, BiConsumer<PropertiesBuilderParameters, S> setter) throws ConfigurationException {
        Parameters parameters = new Parameters();
        PropertiesBuilderParameters pbp = parameters.properties()
            .setListDelimiterHandler(LIST_HANDLER)
            .setEncoding(DEFAULT_ENCODING);
        setter.accept(pbp, source);
        return new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class).configure(pbp).getConfiguration();
    }

    private CompositeConfiguration load(File source) throws ConfigurationException {

        if (!source.exists()) {
            throw new JBakeException("The given source folder '" + source.getAbsolutePath() + "' does not exist.");
        }
        if (!source.isDirectory()) {
            throw new JBakeException("The given source folder is not a directory.");
        }

        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiterHandler(LIST_HANDLER);
        File customConfigFile = new File(source, LEGACY_CONFIG_FILE);
        if (customConfigFile.exists()) {
            displayLegacyConfigFileWarningIfRequired();
            config.addConfiguration(readAsConf(customConfigFile));
        }
        customConfigFile = new File(source, CONFIG_FILE);
        if (customConfigFile.exists()) {
            config.addConfiguration(readAsConf(customConfigFile));
        }
        URL defaultPropertiesLocation = this.getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE);
        config.addConfiguration(readAsConf(defaultPropertiesLocation));
        config.addConfiguration(new SystemConfiguration());
        return config;
    }

    private void displayLegacyConfigFileWarningIfRequired() {
        LOGGER.warn("You have defined a part of your JBake configuration in {}", LEGACY_CONFIG_FILE);
        LOGGER.warn("Usage of this file is being deprecated, please rename this file to: {} to remove this warning", CONFIG_FILE);
    }

    public JBakeConfiguration loadConfig(File source) throws ConfigurationException {
        CompositeConfiguration configuration = load(source);
        return new DefaultJBakeConfiguration(source, configuration);
    }

}
