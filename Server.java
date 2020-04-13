import javax.swing.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.*;
import java.io.*;

public class Server extends JFrame implements ActionListener {
    JLabel label, status;
    JTextField current_address;    
    JButton browse, send;
    String path, address;

    Socket socket;
    ServerSocket server;

    Server(){
        try {
            address = getIPAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        label = new JLabel("File : None");
        browse = new JButton("Browse");
        send = new JButton("Start File Transfer");
        status = new JLabel("Status : Idle");
        current_address = new JTextField("Server Address : " + address);

        label.setBounds(50,50,300,30);
        browse.setBounds(100,125,200,30);
        send.setBounds(100,175,200,30);
        status.setBounds(50, 220,300,30);
        current_address.setBounds(50, 260,300,30);
        current_address.setEditable(false);
        current_address.setBorder(null);
        
        browse.addActionListener(this);
        send.addActionListener(this);
        add(label);
        add(browse);
        add(send);
        add(status);
        add(current_address);

    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browse) {
            JFileChooser fc = new JFileChooser();
            int i = fc.showOpenDialog(this);

            if (i == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                path = f.getPath();
                label.setText("File : " + path);
                status.setText("Ready for Transfer");
            }
        } else if (e.getSource() == send) {
            new Thread(new Runnable() {
                public void run() {
                    sendFile();
                }
            }).start();
        }
    }

    public String getIPAddress() throws Exception {
        InetAddress lhost_address = InetAddress.getLocalHost();
        String address = lhost_address.getHostAddress();
        return address;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.setSize(400,500);
        server.setTitle("File Sender");
        server.setLayout(null);
        server.setVisible(true);
        server.setDefaultCloseOperation(EXIT_ON_CLOSE);  
    }

    private void startServer(){
        status.setText("Starting Server");
        int port = 5000;

        try {
            server = new ServerSocket(port);
            System.out.println("Server Started");
            System.out.println("Waiting for client");
            status.setText("Waiting for Client to Connect");
            socket = server.accept();
            System.out.println("Client connected: " + socket);
            status.setText("Client Connected");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void sendFile() {
        startServer();
        status.setText("Sending File");
        System.out.println("Sending File");
        String fileName;
        Long fileSize;
        String data;
        BufferedReader br;
        PrintWriter pw;
        try {
            File transferFile;
            if (path != null) {
                transferFile = new File(path);    
                fileName = transferFile.getName();
                fileSize = transferFile.length();
                data = fileName + "/" + fileSize.toString();
                System.out.println(data);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pw = new PrintWriter(socket.getOutputStream());
                pw.println(data);
                pw.flush();

            } else {
                status.setText("No File Selected.");
                return;
            }
            byte[] bytearray = new byte [(int)transferFile.length()];
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);

            bin.read(bytearray, 0, bytearray.length);

            OutputStream os = socket.getOutputStream();
            os.write(bytearray, 0, bytearray.length);

            os.flush();
            
            socket.close();
            server.close();
            br.close();
            pw.close();
            os.close();
            bin.close();
            fin.close();
            System.out.println("File transfer Complete");
            status.setText("File transfer Complete.");
            System.out.println("Connection Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}