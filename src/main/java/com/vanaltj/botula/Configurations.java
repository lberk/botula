/*
 * Copyright 2012 Jon VanAlten
 *
 * This file is part of Botula.
 *
 * Botula is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Botula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Botula.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vanaltj.botula;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public class Configurations {
    // TODO support more than one server.

    private static final String ENV_BOTULA_HOME = "BOTULA_HOME";
    private static final String BOTULA_HOME = ".botula";
    private static final String CONFIG_FILE = "conf.properties";

    private static final String PROPERTY_NETWORKS = "networks";
    private static final String PROPERTY_NAME = ".name";
    private static final String PROPERTY_FINGER = ".finger";
    private static final String PROPERTY_ADMIN = ".admin";
    private static final String PROPERTY_SERVER = ".server";
    private static final String PROPERTY_CHANNELS = ".channels";

    private static final String DEFAULT_NETWORK = "example";
    private static final String DEFAULT_NAME = "CountBotula";
    private static final String DEFAULT_FINGER = "I'm Henry the Eighth I Am!";
    private static final String DEFAULT_ADMIN = "root";
    private static final String DEFAULT_SERVER = "irc.example.com";
    private static final String DEFAULT_CHANNELS = "#chat,#help";

    private static final String DEFAULT_COMMENT =
            "This is an example configuration.\n" +
            "Botula created it because you didn't have one.\n" +
            "Note that symbols in channel names may need to be escaped.";

    private Configurations() {} // Not to be instantiated.

    public static Collection<BotConfig> getBotConfigs() throws IOException, MadeNewPropertiesException {
        HashSet<BotConfig> configs = new HashSet<BotConfig>();
        Properties properties = getConfigProperties();
        String networksProperty = properties.getProperty(PROPERTY_NETWORKS);
        for (String network : splitByComma(networksProperty)) {
            configs.add(getConfigFromProperties(network, properties));
            
        }
        return configs;
    }

    private static Properties getConfigProperties() throws IOException, MadeNewPropertiesException {
        Properties properties = new Properties();
        String home = System.getenv(ENV_BOTULA_HOME);
        if (home == null) {
            home = getUserBotulaHome();
        }
        File botHome = new File(home);
        if (!botHome.exists()) {
            botHome.mkdirs();
        }
        File propsFile = new File(botHome, CONFIG_FILE);
        try {
            properties.load(new FileReader(propsFile));
        } catch (FileNotFoundException fnf) {
            createPropertiesFromDefault(properties, propsFile);
            throw new MadeNewPropertiesException("Just Because.");
        }
        return properties;
    }

    private static String getUserBotulaHome() {
        String userHome = System.getProperty("user.home");
        File userHomeFile = new File(userHome);
        File botHomeFile = new File(userHomeFile, BOTULA_HOME);
        return botHomeFile.getAbsolutePath();
    }

    private static void createPropertiesFromDefault(Properties properties, File propsFile) throws IOException {
        System.out.println("No configuration file present!");
        System.out.println("Creating configuration file from default values.");
        System.out.println("These values are almost certainly not what you want.");
        populateDefaultProperties(properties);
        storeProperties(properties, propsFile);
        System.out.println("Please edit the configuration file:");
        System.out.println("    " + propsFile.getAbsolutePath());
    }

    private static void populateDefaultProperties(Properties properties) {
        properties.put(DEFAULT_NETWORK + PROPERTY_FINGER, DEFAULT_FINGER);
        properties.put(DEFAULT_NETWORK + PROPERTY_NAME, DEFAULT_NAME);
        properties.put(DEFAULT_NETWORK + PROPERTY_CHANNELS, DEFAULT_CHANNELS);
        properties.put(DEFAULT_NETWORK + PROPERTY_ADMIN, DEFAULT_ADMIN);
        properties.put(DEFAULT_NETWORK + PROPERTY_SERVER, DEFAULT_SERVER);
        properties.put(PROPERTY_NETWORKS, DEFAULT_NETWORK);
    }

    private static void storeProperties(Properties properties, File propsFile) throws IOException {
        properties.store(new FileWriter(propsFile), DEFAULT_COMMENT);
    }

    private static BotConfig getConfigFromProperties(String network, Properties properties) {
        String name = properties.getProperty(network + PROPERTY_NAME);
        String finger = properties.getProperty(network + PROPERTY_FINGER);
        String adminNick = properties.getProperty(network + PROPERTY_ADMIN);
        String server = properties.getProperty(network + PROPERTY_SERVER);
        String channelsProperty = properties.getProperty(network + PROPERTY_CHANNELS);
        Collection<String> channels = splitByComma(channelsProperty);
        return new BotConfig(name, finger, adminNick, server, channels);
    }

    private static Collection<String> splitByComma(String input) {
        HashSet<String> toReturn = new HashSet<String>();
        String[] inputSplit = input.split(",");
        for (String item : inputSplit) {
            toReturn.add(item.trim());
        }
        return toReturn;
    }

    static class MadeNewPropertiesException extends Exception {

        private static final long serialVersionUID = -4408620134410497530L;

        public MadeNewPropertiesException(String message) {
            super(message);
        }
    }
}
