package Objects;

import java.io.Serializable;
import java.net.InetAddress;

public class ServerSettings implements Serializable {
    InetAddress ip;
    int port;
    String password;

    public ServerSettings() {
    }

    public ServerSettings(InetAddress ip, int port, String password) {
        this.ip = ip;
        this.port = port;
        this.password = password;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ServerSettings{" +
                "ip=" + ip +
                ", port=" + port +
                ", password='" + password + '\'' +
                '}';
    }
}
