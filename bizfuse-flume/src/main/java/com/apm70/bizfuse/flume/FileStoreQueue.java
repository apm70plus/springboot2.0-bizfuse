package com.apm70.bizfuse.flume;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.time.DateFormatUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileStoreQueue implements IQueue<byte[]> {

    private static final Charset utf8 = Charset.forName("UTF-8");
    private static final byte[] END_MARK = ("@END@" + System.lineSeparator()).getBytes(FileStoreQueue.utf8);
    private final File metaFile;
    private final File storeFile;
    private BufferedRandomAccessFile metaData;
    private BufferedRandomAccessFile storeReader;
    private BufferedRandomAccessFile storeWriter;
    private int batchCount = 1000;

    private final ReentrantLock lock = new ReentrantLock();

    private AtomicInteger size;
    private OfferInfo cachedLatestOffer;

    /**
     * 构造函数
     *
     * @param metadata
     * @param store
     * @throws IOException
     */
    public FileStoreQueue(final File metadata, final File store) throws IOException {
        this.metaFile = metadata;
        this.storeFile = store;
        this.metaData = new BufferedRandomAccessFile(this.metaFile, "rw");
        this.storeWriter = new BufferedRandomAccessFile(this.storeFile, "rw");
        this.storeReader = new BufferedRandomAccessFile(this.storeFile, "rw");
        this.init();
    }

    private void init() throws IOException {
        long readPosition;
        long writePosition;
        if (this.metaData.length() == 0) {
            this.size = new AtomicInteger(0);
            readPosition = 0L;
            writePosition = 0L;
        } else {
            this.size = new AtomicInteger(this.metaData.readIntb());
            if (this.size.get() < 0) {
                FileStoreQueue.log.error("消息队列元数据读取错误，队列size小于0.");
                throw new RuntimeException("消息队列元数据读取错误，队列size小于0.");
            }
            readPosition = this.metaData.readLong();
            writePosition = this.metaData.readLong();
        }
        this.storeReader.seek(readPosition);
        this.storeWriter.seek(writePosition);
    }

    @Override
    public int size() {
        return this.size.get();
    }

    @Override
    public boolean add(final byte[] bytes) {
        if (bytes == null) {
            return false;
        }
        this.lock.lock();
        if (this.size() <= 0) {
            this.tryArchive();
        }
        try {
            this.storeWriter.writeIntb(bytes.length);
            this.storeWriter.write(bytes);
            this.storeWriter.write(FileStoreQueue.END_MARK);
            this.storeWriter.flush();
            this.size.incrementAndGet();
            this.persistMetaData();
        } catch (final IOException e) {
            FileStoreQueue.log.error("写文件失败，消息内容：" + new String(bytes, FileStoreQueue.utf8), e);
            return false;
        } finally {
            this.lock.unlock();
        }
        return true;
    }

    @Override
    public List<byte[]> batchPeek() {
        if (this.size() <= 0) {
            this.tryArchive();
            return null;
        }

        this.lock.lock();
        try {
            // 使用缓存的数据
            if ((this.cachedLatestOffer != null)
                    && (this.cachedLatestOffer.readStartPosition == this.storeReader.getFilePointer())) {
                final List<byte[]> values = this.cachedLatestOffer.values;
                return values;
            }
            final OfferInfo offer = new OfferInfo();
            offer.readStartPosition = this.storeReader.getFilePointer();
            int batchSize = 0;
            for (int i = 0; i < this.batchCount; i++) {
                final byte[] value = this.nextValue();
                if ((value == null) || ((batchSize += value.length) > 5120000)) {
                    break;
                }
                offer.values.add(value);
            }
            offer.readEndPosition = this.storeReader.getFilePointer();
            this.cachedLatestOffer = offer;
            this.storeReader.seek(offer.readStartPosition);
            return offer.values;
        } catch (final IOException e) {
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public List<byte[]> batchPoll() {
        if (this.size() <= 0) {
            this.tryArchive();
            return null;
        }
        this.lock.lock();
        List<byte[]> values = null;
        try {
            if ((this.cachedLatestOffer != null)
                    && (this.cachedLatestOffer.readStartPosition == this.storeReader.getFilePointer())) {
                // 使用缓存的数据
                values = this.cachedLatestOffer.values;
                this.storeReader.seek(this.cachedLatestOffer.readEndPosition);
            } else {
                values = new ArrayList<>();
                int batchSize = 0;
                for (int i = 0; i < this.batchCount; i++) {
                    final byte[] value = this.nextValue();
                    if ((value == null) || ((batchSize += value.length) > 5120000)) {
                        break;
                    }
                    values.add(value);
                }
            }
            this.cachedLatestOffer = null;
            this.size.addAndGet(0 - values.size());
            this.persistMetaData();
            return values;
        } catch (final IOException e) {
            FileStoreQueue.log.error("读文件失败", e);
            return null;
        } finally {
            this.lock.unlock();
        }

    }

    @Override
    public byte[] poll() {
        if (this.size() <= 0) {
            this.tryArchive();
            return null;
        }
        this.lock.lock();
        this.cachedLatestOffer = null;
        try {
            final byte[] value = this.nextValue();
            this.size.decrementAndGet();
            this.persistMetaData();
            return value;
        } catch (final IOException e) {
            FileStoreQueue.log.error("读文件失败", e);
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    public void close() {
        this.lock.lock();
        try {
            this.closeQuietly(this.metaData);
            this.closeQuietly(this.storeReader);
            this.closeQuietly(this.storeWriter);
        } finally {
            this.lock.unlock();
        }
    }

    public void setBatchSize(final int batchCount) {
        this.batchCount = batchCount;
    }

    private void tryArchive() {
        try {
            if (this.storeWriter.getFilePointer() < 102400000L) {
                return;
            }
        } catch (final Exception e) {
        }
        this.lock.lock();
        try {
            if (this.size.get() > 0) {
                return;
            }
            this.closeQuietly(this.metaData);
            this.closeQuietly(this.storeReader);
            this.closeQuietly(this.storeWriter);
            final String time = DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd'T'HH:mm:ss");
            File targetFile = new File(this.metaFile.getParent(), this.metaFile.getName() + "-" + time);
            Files.move(this.metaFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            targetFile = new File(this.storeFile.getParent(), this.storeFile.getName() + "-" + time);
            Files.move(this.storeFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            this.metaData = new BufferedRandomAccessFile(this.metaFile, "rw");
            this.storeWriter = new BufferedRandomAccessFile(this.storeFile, "rw");
            this.storeReader = new BufferedRandomAccessFile(this.storeFile, "rw");
        } catch (final IOException e) {
            FileStoreQueue.log.warn("文件归档失败", e);
        } finally {
            this.lock.unlock();
        }
    }

    private void persistMetaData() {
        try {
            this.metaData.seek(0);
            this.metaData.writeIntb(this.size.get());
            this.metaData.writeLongb(this.storeReader.getFilePointer());
            this.metaData.writeLongb(this.storeWriter.getFilePointer());
            this.metaData.flush();
        } catch (final IOException e) {
        }
    }

    private byte[] nextValue() throws IOException {
        final int size = this.storeReader.readIntb();
        if (size == 0) {
            return null;
        }
        final byte[] value = new byte[size];
        this.storeReader.read(value);
        this.storeReader.seek(this.storeReader.getFilePointer() + FileStoreQueue.END_MARK.length);
        return value;
    }

    private void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    class OfferInfo {
        List<byte[]> values = new ArrayList<>();
        long readStartPosition;
        long readEndPosition;
    }
}
