/**
 * RmiHelper.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;


import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.Arrays;
import java.util.List;


/**
 * Helper class which provides methods for handling the RMI registry.
 *
 * @author Michael Cox
 * @version 1.0
 */
public final class RmiHelper {

    /** The initial capacity of the string builder used to build URLs. */
    private static final int URL_INITIAL_CAPACITY = 50;

    /**
     * Constructs an instance.
     * <p/>
     * Should never be called as this is a helper class of static methods only.
     */
    private RmiHelper() {
    }

    /**
     * Indicates if the local RMI registry is running on the specified port.
     *
     * @param port the port the RMI registry can be found on
     *
     * @return <code>true</code> if the registry is running on that port
     */
    private static boolean isLocalRegistryRunning(final int port) {
        try {
            final Registry reg = LocateRegistry.getRegistry(port);
            final String[] names = reg.list();
            final List<String> namesStr = Arrays.asList(names);
            return true;
        } catch (AccessException ignore) {
            return false;
        } catch (RemoteException ignore) {
            return false;
        }
    }

    /**
     * Starts up the local RMI registry on the specified port.
     * <p/>
     * No action is taken if the registry is already running.
     *
     * @param port the port the RMI registry should listen on
     *
     * @throws java.rmi.RemoteException if the registry cannot be started
     */
    public static void startupLocalRmiRegistry(final int port)
            throws RemoteException {
        if (!isLocalRegistryRunning(port)) {
//            System.err.println("STARTING NEW REG");
            final RmiSocketFactory clientFactory = new RmiSocketFactory();
            final RMISocketFactory serverFactory = RMISocketFactory
                    .getDefaultSocketFactory();
            final Registry reg = LocateRegistry
                    .createRegistry(port, clientFactory, serverFactory);

            /* Call it to make sure it works */
            reg.list();
        } else {
//            System.err.println("NOT STARTING NEW REG");
        }
    }

    /**
     * Rebinds the specified name to the specified remote object in the
     * specified RMI registry.
     *
     * @param host   host name of host where the RMI registry is running
     * @param port   network port on which the RMI registry is listening
     * @param name   the name the remote object will be listed under in the
     *               registry
     * @param remote the object with the remote interface
     *
     * @throws java.net.MalformedURLException if a valid URL for the object cannot be
     *                               constructed
     * @throws java.rmi.RemoteException       if registry could not be contacted
     */
    public static String rebind(final String host, final int port,
                              final String name, final Remote remote)
            throws MalformedURLException, RemoteException {
        final String url = buildURL(host, port, name);
        Naming.rebind(url, remote);
        return url;
    }

    /**
     * Unbinds the specified name from its remote object in the specified RMI
     * registry.
     *
     * @param host host name of host where the RMI registry is running
     * @param port network port on which the RMI registry is listening
     * @param name the name the remote object is listed under in the registry
     *
     * @throws java.rmi.NotBoundException     if name is not currently bound
     * @throws java.net.MalformedURLException if a valid URL for the object cannot be
     *                               constructed
     * @throws java.rmi.RemoteException       if registry could not be contacted
     */
    public static void unbind(final String host, final int port,
                              final String name)
            throws NotBoundException, MalformedURLException, RemoteException {
        final String url = buildURL(host, port, name);
        Naming.unbind(url);
    }

    /**
     * Looks up the specifed name in the specified RMI registry and returns the
     * associated remote object.
     *
     * @param host host name of host where the RMI registry is running
     * @param port network port on which the RMI registry is listening
     * @param name the name the remote object will be listed under in the
     *             registry
     *
     * @return the remote object listed against the given name
     *
     * @throws java.rmi.NotBoundException     if name is not currently bound
     * @throws java.rmi.RemoteException       if registry could not be contacted
     * @throws java.net.MalformedURLException if a valid URL for the object cannot be
     *                               constructed
     */
    public static Remote lookup(final String host, final int port,
                                final String name)
            throws NotBoundException, RemoteException, MalformedURLException {
        final String url = buildURL(host, port, name);
        return Naming.lookup(url);
    }

    /**
     * Builds a valid URL for the named object at a specified RMI registry
     * location.
     *
     * @param host host name of host where the RMI registry is running
     * @param port network port on which the RMI registry is listening
     * @param name the name the remote object will be listed under in the
     *             registry
     *
     * @return a well-formed URL for the object
     */
    public static String buildURL(final String host, final int port,
                                   final String name) {
        final StringBuilder url = new StringBuilder(URL_INITIAL_CAPACITY);
        url.append("//");
        url.append(host);
        url.append(':');
        url.append(port);
        url.append('/');
        url.append(name);
        return url.toString();
    }


    static class RmiSocketFactory implements RMIClientSocketFactory, Serializable {

        /**
         * Serial version UID to prevent serialization errors where different
         * compilers are used.
         */
        private static final long serialVersionUID = -9999L;
        /**
         * The timeout in milliseconds after which the client assumes the connection
         * dead.
         */
        private static final int SOCKET_TIMEOUT = 30000;

        /**
         * Creates a socket for use by a client of the database with a timeout.
         *
         * @param host the database host
         * @param port the port through which RMI calls are routed
         *
         * @return the client socket
         *
         * @throws java.io.IOException if the socket cannot be created
         */
        public Socket createSocket(final String host, final int port)
                throws IOException {
            final RMISocketFactory factory = RMISocketFactory
                    .getDefaultSocketFactory();
            final Socket socket = factory.createSocket(host, port);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            return socket;
        }
    }

}
