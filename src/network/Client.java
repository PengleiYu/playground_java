package network;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.Random;

/**
 * Created by yupenglei on 17/6/8.
 */
public class Client {
    public static void main(String[] args) {
//        new Thread(new ClientRunnable(Constants.ACTION_CHAT, true)).start();
//        new Thread(new ClientRunnable(Constants.ACTION_DOWNLOAD_LITTLE, false)).start();
        new Thread(new ClientRunnable(Constants.ACTION_CHAT, true)).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new ClientRunnable(Constants.ACTION_DOWNLOAD_LARGE, false)).start();
    }

    @AllArgsConstructor
    public static class ClientRunnable implements Runnable {
        private String mAction;
        private boolean mRunForever;

        @Override
        public void run() {
            try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost",
                    8888))) {
                /*
                写字符串
                 */
                socketChannel.write(ByteBuffer.wrap(mAction.getBytes()));
                Constants.printWrite(mAction);

                switch (mAction) {
                    case Constants.ACTION_DOWNLOAD_LARGE:
                        File file = new File("client_a.mp4");
                        receiveFile(socketChannel, file);
                        break;
                    case Constants.ACTION_DOWNLOAD_LITTLE:
                        File smallFile = new File("client_hah.gz");
                        receiveFile(socketChannel, smallFile);
                        break;
                    case Constants.ACTION_UPLOAD:

                        break;
                    case Constants.ACTION_CHAT:
                        Random random = new Random();
                        int num = 0;
                        while (mRunForever) {
                            int sleep = random.nextInt(1000);
                            System.out.println(String.format("start sleep %ss...", sleep / 1000f));
                            try {
                                Thread.sleep(sleep);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                socketChannel.write(ByteBuffer.wrap(Constants.ACTION_END.getBytes
                                        ()));
                            }

                            boolean readable = random.nextInt(100) % 2 == 0;
                            String strWrite = readable ? Constants.MSG_CHAT_NEED_RESPOND :
                                    Constants.MSG_CHAT_NO_RESPOND;
                            strWrite = strWrite + "*" + num++;
                            Constants.printWrite(strWrite);
                            long timeWritten = System.currentTimeMillis();
                            socketChannel.write(ByteBuffer.wrap(strWrite.getBytes()));

                            if (readable) {
                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                socketChannel.read(byteBuffer);
                                byteBuffer.flip();
                                String msgRead = Charset.forName("utf8").decode(byteBuffer)
                                        .toString();
                                Constants.printRead(msgRead);
                                System.out.println(String.format("write to read, time=%s",
                                        System.currentTimeMillis() - timeWritten));
                            }
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client end!");
            }
        }

        private void receiveFile(SocketChannel socketChannel, File file) throws IOException {
            if (file.exists()) {
                file.delete();
            }
            FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption
                    .CREATE, StandardOpenOption.WRITE);
//            fileChannel.transferFrom(socketChannel, 0, 100 * 1024 * 1024);

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            int len;
            while ((len = socketChannel.read(byteBuffer)) != -1) {
                byteBuffer.flip();
                int write = fileChannel.write(byteBuffer);
                System.out.println(String.format("receive %s, write file %s", len, write));
                byteBuffer.clear();
            }
            System.out.println("final **** len=" + file.length());
        }
    }


}
