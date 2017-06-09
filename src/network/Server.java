package network;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * Created by yupenglei on 17/6/7.
 */
public class Server {

    public static void main(String[] args) {
//        new Thread(new ServerRunnable()).start();
        new Thread(new SelectorRunnable()).start();
    }

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
                    int count = selector.select();
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
                    System.out.println("read len =-1, closing...");
                    closeKey(key);
                    return;
                }
                byteBuffer.flip();
                String msgRead = Charset.forName("utf8").decode(byteBuffer).toString();
                Constants.read(msgRead);
                if (Constants.ACTION_CHAT_NEED_RESPOND.equals(msgRead)) {
                    String msgWrite = "Hello, This is a response";
                    socketChannel.write(ByteBuffer.wrap(msgWrite.getBytes()));
                    Constants.write(msgWrite);
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
//            FileChannel.open(file.toPath(), StandardOpenOption.READ).transferTo
//                    (0, file.length(), socketChannel);
            FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            int len;
            while ((len = fileChannel.read(byteBuffer)) != -1) {
                System.out.println("read len=" + len);
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            }
            System.out.println("file len=" + file.length());
            System.out.println("after transfer");
            socketChannel.close();
        }

        private void closeKey(SelectionKey key) {
            System.out.println("cancel key " + key.attachment());
            key.cancel();
        }
    }

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
