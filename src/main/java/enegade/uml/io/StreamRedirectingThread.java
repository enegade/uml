package enegade.uml.io;

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

import java.io.*;

/**
 * @author enegade
 */
public class StreamRedirectingThread extends Thread
{
    private final Reader in;

    private final Writer out;

    private static final int BUFFER_SIZE = 2048;

    private boolean running = true;

    public StreamRedirectingThread(String name, InputStream in, OutputStream out)
    {
        super(name);
        this.in = new InputStreamReader(in);
        this.out = new OutputStreamWriter(out);
    }

    public void stopListening()
    {
        this.running = false;
    }

    @Override
    public void run()
    {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            while ( this.running ) {
                if( in.ready() ) {
                    count = in.read(cbuf, 0, BUFFER_SIZE);
                    if(count < 0) {
                        break;
                    }
                    out.write(cbuf, 0, count);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Child I/O Transfer - " + e);
        }
    }
}
