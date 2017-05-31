package de.jetwick.snacktory.utils;


import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * In Java 8, SSLSocketFactory has some weird behaviour when the HostNameVerification is
 * bypassed it also skip the SNI header.
 * <p>
 * To overcome the default behaviour this class helps to include SNI in the SSL requests.
 * <p>
 * Ref: http://javabreaks.blogspot.in/2015/12/java-ssl-handshake-with-server-name.html
 */
public class SSLConnectionSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory sslSocketFactory;
    private final SSLParameters sslParameters;

    public SSLConnectionSocketFactory(SSLSocketFactory sslSocketFactory, SSLParameters sslParameters) {
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, host, port, autoClose);
        setParameters(sslSocket);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        setParameters(socket);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port, localHost, localPort);
        setParameters(socket);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        setParameters(socket);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(address, port, localAddress, localPort);
        setParameters(socket);
        return socket;
    }

    private void setParameters(SSLSocket socket) {
        socket.setSSLParameters(sslParameters);
    }
}