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

import com.sun.jdi.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author enegade
 */
public class LineReader
{
    public static final LineReader INSTANCE = new LineReader();

    private String[] dirs = new String[]{
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.base",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.compiler",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.datatransfer",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.desktop",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.instrument",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.logging",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.management",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.management.rmi",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.naming",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.net.http",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.prefs",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.rmi",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.scripting",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.se",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.security.jgss",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.security.sasl",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.smartcardio",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.sql",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.sql.rowset",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.transaction.xa",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.xml",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/java.xml.crypto",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.accessibility",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.aot",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.attach",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.charsets",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.compiler",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.crypto.cryptoki",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.crypto.ec",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.dynalink",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.editpad",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.hotspot.agent",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.httpserver",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.ed",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.jvmstat",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.le",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.opt",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.vm.ci",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.vm.compiler",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.internal.vm.compiler.management",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jartool",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.javadoc",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jcmd",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jconsole",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jdeps",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jdi",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jdwp.agent",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jfr",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jlink",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jshell",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jsobject",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.jstatd",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.localedata",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.management",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.management.agent",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.management.jfr",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.naming.dns",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.naming.ldap",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.naming.rmi",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.net",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.pack",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.rmic",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.scripting.nashorn",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.scripting.nashorn.shell",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.sctp",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.security.auth",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.security.jgss",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.unsupported",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.unsupported.desktop",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.xml.dom",
            "/usr/lib/jvm/jdk-11.0.8/lib/src/jdk.zipfs",
    };

    private Map<String, List<String>> sourceFileMap = new HashMap<>();

    private LineReader() {}

    public String readLine(Location location)
    {
        String filename;
        try {
            filename = location.sourceName();
        } catch (Exception e) {
            return "";
        }

        String refName = location.declaringType().name();

        String key = refName + ' ' + filename;

        List<String> sourceLines = this.sourceFileMap.get(key);
        if(sourceLines == null) {
            File path = null;
            int iDot = refName.lastIndexOf('.');
            String pkgName = (iDot >= 0) ? refName.substring(0, iDot + 1) : "";
            String full = pkgName.replace('.', File.separatorChar) + filename;
            for (int i = 0; i < this.dirs.length; ++i) {
                path = new File(this.dirs[i], full);
                if (!path.exists()) {
                    path = null;
                } else {
                    break;
                }
            }

            if (path == null) {
                int stop = 1;
                return "";
            }


            try {
                BufferedReader reader = new BufferedReader(new FileReader(path));

                sourceLines = new ArrayList<>();
                String line = reader.readLine();
                while (line != null) {
                    sourceLines.add(line);
                    line = reader.readLine();
                }

                reader.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.sourceFileMap.put(key, sourceLines);
        }

        return sourceLines.get( location.lineNumber() - 1 );
    }
}
