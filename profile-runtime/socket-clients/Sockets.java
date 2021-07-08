import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This test starts two similar clients.
 * The code of the clients is the same,
 * except that BusyClient constantly receives new data,
 * while IdleClient does almost nothing but waiting for incoming data.
 * <p>
 * Most sampling profilers will not make difference between BusyClient and IdleClient,
 * because JVM does not know whether a native method consumes CPU or just waits inside a blocking call.
 *
 * @see <a href="https://github.com/apangin/codeone2019-java-profiling/blob/master/src/demo2/SocketTest.java">
 *     Based on Andrei Pangin's example
 *     </a>
 */
public class Sockets
{
    public static final String HOST = System.getProperty("host");
    public static final int PORT = 1234;

    public static void main(String[] args) throws Exception
    {
        ServerSocket s = new ServerSocket(PORT);

        new IdleClient().start();
        Socket idleClient = s.accept();

        new BusyClient().start();
        Socket busyClient = s.accept();

        byte[] buf = new byte[4096];
        ThreadLocalRandom.current().nextBytes(buf);

        for (int i = 0; ; i++)
        {
            if ((i % 10_000_000) == 0)
            {
                idleClient.getOutputStream().write(buf, 0, 1);
            }
            else
            {
                busyClient.getOutputStream().write(buf);
            }
        }
    }

    static class IdleClient extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                byte[] buf = new byte[4096];

                Socket s = new Socket(Sockets.HOST, Sockets.PORT);

                InputStream in = s.getInputStream();
                while (in.read(buf) >= 0)
                {
                    // keep reading
                }
                System.out.println(Thread.currentThread().getName() + " stopped");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    static class BusyClient extends Thread
    {

        @Override
        public void run()
        {
            try
            {
                byte[] buf = new byte[4096];

                Socket s = new Socket(Sockets.HOST, Sockets.PORT);

                InputStream in = s.getInputStream();
                while (in.read(buf) >= 0)
                {
                    // keep reading
                }
                System.out.println(Thread.currentThread().getName() + " stopped");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
