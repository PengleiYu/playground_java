package network;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * Created by yupenglei on 17/6/7.
 */
public class Server {

    public static void main(String[] args) {
//        new Thread(new ServerRunnable()).start();
//        new Thread(new SelectorRunnable()).start();
        new SelectorManager().start();
    }

    /**
     * 收到的所有socketChannel均注册为readable，若读到的是chat请求，则直接回写消息，
     * 若是文件下载请求，则注册至专门的文件传输selector
     * 注意selector在没有socketChannel注册的情况下select会阻塞，而注册需要等待selector唤醒，
     * 所以在socketChannel重新注册selector时需唤醒要注册的selector
     */

    public static class SelectorManager {
        //        FileTransferRunnable mFileTransferRunnable;
//        MainSelectorRunnable mMainSelectorRunnable;
        Selector mSelectorMain, mSelectorFileTransfer;

        public SelectorManager() {
            try {
                mSelectorMain = Selector.open();
                mSelectorFileTransfer = Selector.open();
//                mMainSelectorRunnable = new MainSelectorRunnable();
//                mFileTransferRunnable = new FileTransferRunnable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            new Thread(new MainSelectorRunnable()).start();
            new Thread(new FileTransferRunnable()).start();
        }

        public class MainSelectorRunnable implements Runnable {
            private boolean running = true;
            private final String TAG = getClass().getSimpleName();
            private ByteBuffer mReadBuffer = ByteBuffer.allocate(1024);

            @Override
            public void run() {
                try {
                    Constants.threadPrint("running");
                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.bind(new InetSocketAddress(8888));
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(mSelectorMain, SelectionKey.OP_ACCEPT);

                    while (running) {
                        if (mSelectorMain.select() == 0) {
                            Constants.threadPrint("select 0");
                            continue;
                        }
                        Iterator<SelectionKey> iterator = mSelectorMain.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey next = iterator.next();
                            processKey(next);
                            iterator.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void processKey(SelectionKey key) throws IOException {
                if (key.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel accept = serverSocketChannel.accept();
                    accept.configureBlocking(false);
                    accept.register(key.selector(), SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    mReadBuffer.clear();
                    int read = socketChannel.read(mReadBuffer);
                    if (read == -1) {
                        socketChannel.close();
                        System.out.println("printRead len =-1, closing...");
                        return;
                    }
                    mReadBuffer.flip();
                    String msgRead = Charset.forName("utf8").decode(mReadBuffer).toString();
                    Constants.printRead(msgRead);
                    if (Constants.ACTION_DOWNLOAD_LARGE.equals(msgRead) ||
                            Constants.ACTION_DOWNLOAD_LITTLE.equals(msgRead)) {
                        key.cancel();
                        String filename = Constants.ACTION_DOWNLOAD_LARGE.equals(msgRead) ?
                                "a.mp4" : "net.tar.gz";
                        /* wakeup很关键 */
                        socketChannel.register(mSelectorFileTransfer.wakeup(), SelectionKey
                                .OP_WRITE, filename);
                        System.out.println("register to FileTransferRunnable");
                    } else if (msgRead.contains(Constants.ACTION_CHAT) ||
                            msgRead.contains(Constants.MSG_CHAT_NEED_RESPOND)) {
                        String s = "Hello, welcome!";
                        socketChannel.write(ByteBuffer.wrap(s.getBytes()));
                        Constants.printWrite(s);
                    }
                }
            }
        }

        private class FileTransferRunnable implements Runnable {
            private boolean running = true;
            private final String TAG = getClass().getSimpleName();
            private ByteBuffer mWriteBuffer = ByteBuffer.allocate(1024 * 1024);

            @Override
            public void run() {
                try {
                    Constants.threadPrint("running");
                    while (running) {
                        if (mSelectorFileTransfer.select() == 0) {
                            Constants.threadPrint("select 0");
                            continue;
                        }
                        Iterator<SelectionKey> iterator = mSelectorFileTransfer.selectedKeys()
                                .iterator();
                        while (iterator.hasNext()) {
                            Constants.threadPrint("iterator has next");
                            SelectionKey next = iterator.next();
                            processKey(next);
                            iterator.remove();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void processKey(SelectionKey key) throws IOException {
                Constants.threadPrint("processKey");
                if (key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    String fileName = (String) key.attachment();
                    File file = new File(fileName);
                    FileChannel fileChannel = FileChannel.open(file.toPath(),
                            StandardOpenOption.READ);
                    while (fileChannel.read(mWriteBuffer) != -1 || mWriteBuffer.position() != 0) {
                        mWriteBuffer.flip();
                        int write = socketChannel.write(mWriteBuffer);
                        Constants.threadPrint("write " + write);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mWriteBuffer.compact();
                    }
                    Constants.threadPrint(String.format("transfer end, file len=%s",
                            file.length()));
                    socketChannel.close();
                } else {
                    System.out.println("transferRunnable不应该接收非可写类型");
                }
            }
        }

    }

    /**
     * 异步IO，对每个socket注册selector，单一线程轮流处理
     * 问题：若某个socket有大文件传输，可能会影响其他socket通信，待测
     * 已测试，确实会阻塞其他线程,so必须单开一个线程用于传输大文件
     */
    public static class SelectorRunnable implements Runnable {
        private final String ATTACH_READABLE = "ATTACH_READABLE";
        private final String ATTACH_WRITABLE = "ATTACH_WRITABLE";
        private final String ATTACH_ACCEPTABLE = "ATTACH_ACCEPTABLE";
        @Setter
        private boolean isRunning = true;

        @Override
        public void run() {
            try (Selector selector = Selector.open()) {
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(8888));
                serverSocketChannel.configureBlocking(false);
                SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey
                        .OP_ACCEPT);
                selectionKey.attach(ATTACH_ACCEPTABLE);


                while (isRunning) {
                    if (selector.select() == 0) {
                        return;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        processKey(iterator.next());
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private int mCount = 0;

        private void processKey(SelectionKey key) throws IOException {
            System.out.println("attach == " + key.attachment());
            if (key.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                SelectionKey selectionKey = socketChannel.register(key.selector(), SelectionKey
                        .OP_READ);
//                selectionKey.attach(mCount++);
                selectionKey.attach(ATTACH_READABLE);
            } else if (key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                //服务器无法知道客户端关闭连接
//                System.out.println(String.format("connect=%s ,open=%s", socketChannel.isConnected
//                        (), socketChannel.isOpen()));
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int read = socketChannel.read(byteBuffer);
                if (read == -1) {
                    System.out.println("printRead len =-1, closing...");
                    closeKey(key);
                    return;
                }
                byteBuffer.flip();
                String msgRead = Charset.forName("utf8").decode(byteBuffer).toString();
                Constants.printRead(msgRead);
                if (msgRead.contains(Constants.MSG_CHAT_NEED_RESPOND)) {
                    String msgWrite = "Hello, This is a response";
                    socketChannel.write(ByteBuffer.wrap(msgWrite.getBytes()));
                    Constants.printWrite(msgWrite);
                } else if (Constants.ACTION_DOWNLOAD_LARGE.equals(msgRead)) {
                    SelectionKey register = socketChannel.register(key.selector(), SelectionKey
                            .OP_WRITE);
                    register.attach(msgRead);
//                    transferFile(socketChannel, msgRead);
                } else if (Constants.ACTION_DOWNLOAD_LITTLE.equals(msgRead)) {
                    SelectionKey register = socketChannel.register(key.selector(), SelectionKey
                            .OP_WRITE);
                    register.attach(msgRead);
                } else if (Constants.ACTION_END.equals(msgRead)) {
                    closeKey(key);
                }
            } else if (key.isWritable()) {
                if (Constants.ACTION_DOWNLOAD_LITTLE.equals(key.attachment()) ||
                        Constants.ACTION_DOWNLOAD_LARGE.equals(key.attachment())) {
                    key.cancel();
                    transferFile((SocketChannel) key.channel(), key.attachment().toString());
                }
            }
        }

        private void transferFile(SocketChannel socketChannel, String fileType) throws
                IOException {
            File file = new File(Constants.ACTION_DOWNLOAD_LITTLE.equals(fileType) ?
                    "net.tar.gz" : "a.mp4");
            System.out.println("before transfer");
            FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            writeFileToSocket2(socketChannel, fileChannel, byteBuffer);
            System.out.println("file len=" + file.length());
            System.out.println("after transfer");
            socketChannel.close();
        }

        /**
         * 从文件读取数据至缓冲区，然后写入socket，然后缓冲区压缩，并继续读取写入循环
         * 当读到文件尾时，还要等待缓冲区排空后才能停止
         */
        private void writeFileToSocket2(SocketChannel socketChannel, FileChannel fileChannel,
                                        ByteBuffer byteBuffer) throws IOException {
            /* 关键之处是判断buffer的position */
            while (fileChannel.read(byteBuffer) != -1 || byteBuffer.position() != 0) {
                byteBuffer.flip();
                int write = socketChannel.write(byteBuffer);
                if (write == 0) {
                    try {
                        /* 可以不休眠 */
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                byteBuffer.compact();
            }
        }

        /**
         * 从文件读取数据至缓冲区，然后写入socket，若缓冲区数据未排完，则再次写入socket，
         * 直至排空缓冲区，才再次读取数据
         *
         * @deprecated use {@link #writeFileToSocket2(SocketChannel, FileChannel, ByteBuffer)}
         */
        private void writeFileToSocket(SocketChannel socketChannel, FileChannel fileChannel,
                                       ByteBuffer byteBuffer) throws IOException {
            int len;
            while ((len = fileChannel.read(byteBuffer)) != -1) {
                try {
                    /* 测试用 */
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byteBuffer.flip();
                int write = 0;
                int i;
                while (byteBuffer.hasRemaining()) {
                    i = socketChannel.write(byteBuffer);
                    if (write != 0) {
                        System.out.println(String.format("have written %s, retry... ", write));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    write += i;
                }
                System.out.println(String.format("read %s, write %s", len, write));
                byteBuffer.clear();
            }
        }

        private void closeKey(SelectionKey key) {
            System.out.println("cancel key " + key.attachment());
            key.cancel();
        }
    }

    /**
     * 同步IO，对接收到的每个socket开一个线程
     */
    public static class ServerRunnable implements Runnable {
        @Override
        public void run() {
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                serverSocketChannel.bind(new InetSocketAddress(8888));
                while (true) {
                    SocketChannel accept = serverSocketChannel.accept();
                    new Thread(new AcceptRunnable(accept)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @AllArgsConstructor
        class AcceptRunnable implements Runnable {
            private SocketChannel mSocketChannel;

            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                System.out.println(byteBuffer.toString());
                try {
                    int lenRead = mSocketChannel.read(byteBuffer);
                    byteBuffer.flip();
                    Charset charset = Charset.forName("utf8");
                    String action = charset.decode(byteBuffer).toString();
                    System.out.println(action);
                    switch (action) {
                        case Constants.ACTION_DOWNLOAD_LARGE:
                            File file = new File("a.mp4");
                            FileChannel.open(file.toPath(), StandardOpenOption.READ)
                                    .transferTo(0, file.length(), mSocketChannel);
                            break;
                        case Constants.ACTION_UPLOAD:
                            break;
                        case Constants.ACTION_CHAT:
                            String s;
                            do {
                                byteBuffer.clear();
                                mSocketChannel.read(byteBuffer);
                                byteBuffer.flip();
                                if (byteBuffer.limit() == 0) {
                                    System.out.println("byteBuffer limit is 0");
                                    return;
                                }
                                s = charset.decode(byteBuffer).toString();
                                System.out.println("got " + s);
                            } while (!Constants.ACTION_END.equals(s));
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Accept end!");
                }
            }
        }
    }
}
