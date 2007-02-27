/*
 * Copyright (C) 2007 Deutsche Telekom AG Laboratories
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.telekom.laboratories.multitouch.demo;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.Threading;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 *
 * @author Michael Nischt
 * @version 0.1
 */
public class Techniques {

    enum TextureFormat {
        INTENSITY_8 {
            int internalFormat() { return 1; }// ? GL.GL_RED ?;
            int format()         { return GL_LUMINANCE; }// ? GL.GL_RED ?;
            int size()           { return 1; }
        },
        LUMINANCE_8_ALPHA_8 {
            int internalFormat() { return 2; }
            int format()         { return GL_LUMINANCE_ALPHA; }
            int size()           { return 2; }
        },
        RED_8_GREEN_8_BLUE_8 {
            int internalFormat() { return 3; }
            int format()         { return GL_RGB; }
            int size()           { return 3; }
        },
        RED_8_GREEN_8_BLUE_8_ALPHA_8 {
            int internalFormat() { return 4; }
            int format()         { return GL_RGBA; }
            int size()           { return 4; }
        };

        abstract int size(); // in Bytes

        abstract int internalFormat();
        abstract int format();
        int type() { return GL.GL_UNSIGNED_BYTE; }
        
    }

    public static interface Writer {
        void write(Buffer data, int x, int y, int z, int with, int height, int depth);
    }

    public static interface Reader {
        void read(Buffer data);
    }

    public static interface Performer<T> {
        void peform(T action);
    }


    static class GLTexture2D {

        private final int width, height;
        private final TextureFormat format;

        private final int[] textures = new int[1];

        public GLTexture2D(int width, int height, TextureFormat format) {
            this(width, height, format, null);
        }

        public GLTexture2D(int width, int height, TextureFormat format, Buffer buffer) {
            if(width < 0 || height < 0) {
                throw new IllegalArgumentException();
            } else if(format == null) {
                throw new NullPointerException();
            }
            // only for byte buffers
//            else if(buffer.remaining() < (width*height*format.size()) ) {
//                throw new IllegalArgumentException();
//            }

            this.width = width;
            this.height = height;
            this.format = format;

            final Buffer data = buffer;

            final Runnable create = new Runnable() {
                public void run() {
                    final int width = GLTexture2D.this.width, height = GLTexture2D.this.height;
                    final TextureFormat format = GLTexture2D.this.format;

                    final GL gl = GLU.getCurrentGL();
                    
                    gl.glGenTextures(1, textures, 0);
                    gl.glBindTexture(GL_TEXTURE_2D, textures[0]);                    
                    gl.glTexImage2D(GL_TEXTURE_2D, 0, format.internalFormat(), width, height, 0, format.format(), format.type(), data);                                        
                    gl.glBindTexture(GL_TEXTURE_2D, 0);
                }
            };

            if(Threading.isOpenGLThread()) {
                create.run();
            } else {
                Threading.invokeOnOpenGLThread(create);
            }
        }

        public void read(Performer<Reader> reader) {
                        
            final Reader read = new Reader() {

                public void read(Buffer buffer) { //, int x, int y, int z, int with, int height, int depth) {
//                    if(x != 0 || y != 0 || z != 0) {
//                        throw new UnsupportedOperationException();
//                    } else if(width != GLTexture2D.this.width || height != GLTexture2D.this.height || depth != 1) {
//                        throw new UnsupportedOperationException();
//                    }
                    final GL gl = GLU.getCurrentGL();
                    gl.glBindTexture(GL_TEXTURE_2D, textures[0]);         
                    gl.glGetTexImage(GL_TEXTURE_2D, 0, format.format(), format.type(), buffer);
                    gl.glBindTexture(GL_TEXTURE_2D, 0);
                }
            };

            if(Threading.isOpenGLThread()) {
                reader.peform(read);
            } else {
                final Performer<Reader> _reader = reader;
                Threading.invokeOnOpenGLThread(new Runnable() {
                    public void run() { _reader.peform(read); }
                });
            }
            
        }
        public void write(Performer<Writer> writer) {

            final Writer write = new Writer() {

                public void write(Buffer buffer, int x, int y, int z, int with, int height, int depth) {
                    if(z > 0 || depth != 1) {
                        throw new UnsupportedOperationException();
                    }
                    final GL gl = GLU.getCurrentGL();
                    gl.glBindTexture(GL_TEXTURE_2D, textures[0]);         
                    gl.glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, format.format(), format.type(), buffer);
                    gl.glBindTexture(GL_TEXTURE_2D, 0);
                }
            };

            if(Threading.isOpenGLThread()) {
                writer.peform(write);
            } else {
                final Performer<Writer> _writer = writer;
                Threading.invokeOnOpenGLThread(new Runnable() {
                    public void run() { _writer.peform(write); }
                });
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Attributes ">

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Constructors ">

    private Techniques()
    {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Methods ">

    public static void run() 
    throws InterruptedException, InvocationTargetException 
    {
        
        // <editor-fold defaultstate="collapsed" desc=" Graphcis ">

        final Runnable graphics = new Runnable()
        {
            public void run()
            {

                final JFrame frame = new JFrame("T-Demo: Multitouch Techniques");
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

                frame.setSize(640, 480);

                // <editor-fold defaultstate="collapsed" desc=" OpenGL ">

                final GLCapabilities caps = new GLCapabilities();
                caps.setHardwareAccelerated(true);
                caps.setDoubleBuffered(true);

                final GLCanvas canvas = new GLCanvas(caps);
                canvas.setSize(640, 480);
                canvas.setPreferredSize(new Dimension(640, 480));                                

                // </editor-fold>

                frame.getContentPane().add(canvas);

                // <editor-fold defaultstate="collapsed" desc=" Fullscreen ">

                final boolean fullscreen = false;
                final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();           

                if(fullscreen) {// && device.isFullScreenSupported()) {
                    try
                    {
                        frame.setUndecorated(true);
                        device.setFullScreenWindow(frame);
                    }
                    finally
                    {
                        device.setFullScreenWindow(null);
                    }
                } else {
                    frame.pack();
                }

                // </editor-fold>

                frame.setVisible(true);

            }
        };
        EventQueue.invokeAndWait(graphics);

        // </editor-fold>
        
    }


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Main: Entry Point ">


    public static void main(String... args) throws Exception
    {
        

        final int width = 256, height = 256;
        final TextureFormat format = TextureFormat.INTENSITY_8;

        final ByteBuffer shared = ByteBuffer.allocateDirect( width * height * format.size() ).order(ByteOrder.nativeOrder());


        final GLTexture2D src = new GLTexture2D(width, height, format);
        src.read(new Performer<Reader>() {
            public void peform(Techniques.Reader reader) {
                synchronized(shared) {
                    reader.read(shared);
                }
            }
        });

        final GLTexture2D dst = new GLTexture2D(width, height, format);
        dst.write(new Performer<Writer>() {
            public void peform(Techniques.Writer writer) {
                synchronized(shared) {
                    writer.write(shared, 0,0,0, width, height, 1);
                }
            }
        });

        run();
    }

// </editor-fold>
}