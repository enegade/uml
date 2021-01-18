package enegade.uml;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author enegade
 */
public class Config
{
    private Properties properties;

    public Config(Properties properties)
    {
        this.properties = properties;
    }

    public static Config fromFile()
    {
        Path path = Paths.get("config.properties");
        File file = new File( path.toUri() );

        Properties properties = new Properties();
        try(InputStream stream = new FileInputStream(file)) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Config config = new Config(properties);

        return config;
    }

    public String getProperty(String name)
    {
        return this.properties.getProperty(name, "");
    }
}
