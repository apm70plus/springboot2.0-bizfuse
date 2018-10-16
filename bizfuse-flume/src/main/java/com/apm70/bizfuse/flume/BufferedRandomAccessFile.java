package com.apm70.bizfuse.flume;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class BufferedRandomAccessFile extends RandomAccessFile {

    private final byte[] bs = new byte[8192];
    private final byte[] intBuffer = new byte[4];
    private final ByteBuffer buffer = ByteBuffer.wrap(this.bs);
    private final ByteBuffer intBuf = ByteBuffer.wrap(this.intBuffer);

    public BufferedRandomAccessFile(final File file, final String mode) throws FileNotFoundException {
        super(file, mode);
    }

    /**
     * write with buffer
     *
     * @param i
     * @throws IOException
     */
    public void writeIntb(final int i) throws IOException {
        if (this.buffer.remaining() < 4) {
            this.flush();
        }
        this.buffer.putInt(i);
    }

    /**
     * write with buffer
     *
     * @param l
     * @throws IOException
     */
    public void writeLongb(final long l) throws IOException {
        if (this.buffer.remaining() < 8) {
            this.flush();
        }
        this.buffer.putLong(l);
    }

    /**
     * read with buffer
     *
     * @return
     * @throws IOException
     */
    public int readIntb() throws IOException {
        final int size = this.read(this.intBuffer);
        if (size != 4) {
            return 0;
        }
        return this.intBuf.getInt(0);
    }

    @Override
    public void write(final byte b[]) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(final byte b[], final int off, int len) throws IOException {
        int offset = off;
        final int end = off + len;
        while (offset < end) {
            len = end - offset;
            if (len > this.buffer.remaining()) {
                len = this.buffer.remaining();
            }
            this.buffer.put(b, offset, len);
            offset += len;
            if (this.buffer.remaining() == 0) {
                this.flush();
            }
        }
    }

    public void flush() throws IOException {
        super.write(this.bs, 0, this.buffer.position());
        this.buffer.clear();
    }

}
