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
        new Thread(new ClientRunnable(Constants.ACTION_DOWNLOAD_LARGE, false)).start();
        new Thread(new ClientRunnable(Constants.ACTION_DOWNLOAD_LITTLE, false)).start();
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
                Constants.write(mAction);

                switch (mAction) {
                    case Constants.ACTION_DOWNLOAD_LARGE:
                        File file = new File("client_a.mp4");
                        recieveFile(socketChannel, file);
                        break;
                    case Constants.ACTION_DOWNLOAD_LITTLE:
                        File smallFile = new File("client_hah.gz");
                        recieveFile(socketChannel, smallFile);
                        break;
                    case Constants.ACTION_UPLOAD:

                        break;
                    case Constants.ACTION_CHAT:
                        Random random = new Random();
                        while (mRunForever) {
                            int sleep = random.nextInt(10000);
                            System.out.println(String.format("start sleep %ss...", sleep / 1000f));
                            try {
                                Thread.sleep(sleep);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                socketChannel.write(ByteBuffer.wrap(Constants.ACTION_END.getBytes
                                        ()));
                            }

                            boolean readable = random.nextInt(100) % 2 == 0;
                            String strWrite = readable ? Constants.ACTION_CHAT_NEED_RESPOND :
                                    Constants.ACTION_CHAT_NO_RESPOND;
                            Constants.write(strWrite);
                            socketChannel.write(ByteBuffer.wrap(strWrite.getBytes()));

                            if (readable) {
                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                socketChannel.read(byteBuffer);
                                byteBuffer.flip();
                                String msgRead = Charset.forName("utf8").decode(byteBuffer)
                                        .toString();
                                Constants.read(msgRead);
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

        private void recieveFile(SocketChannel socketChannel, File file) throws IOException {
            if (file.exists()) {
                file.delete();
            }
            FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption
                    .CREATE, StandardOpenOption.WRITE);
//            fileChannel.transferFrom(socketChannel, 0, 100 * 1024 * 1024);

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
            int len;
            long size = 0;
            while ((len = socketChannel.read(byteBuffer)) != -1) {
                System.out.println("receive " + (size += len));
                byteBuffer.flip();
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
            }
            System.out.println("final **** len=" + file.length());
        }
    }


}
