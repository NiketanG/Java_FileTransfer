import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

import java.net.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.io.*;
import java.awt.Desktop;
import java.awt.event.*;

public class Client extends JFrame implements ActionListener {
    JTextField address;
    JButton start, open;
    JLabel label, status;
    JTextArea received_dir;
    String current_dir;
    Client() {
        label = new JLabel("Enter the Address of server :");
        address = new JTextField("127.0.0.1");
        start = new JButton("Start Client");
        status = new JLabel("Status : Idle");
        current_dir = System.getProperty("user.dir") + "/ReceivedFiles";
        JLabel received_dir_label = new JLabel("Received Files Location : ");
        received_dir = new JTextArea(current_dir);
        open = new JButton("Open Folder");

        label.setBounds(50, 50, 300, 30);
        address.setBounds(50, 80, 300, 25);
        start.setBounds(100, 125, 200, 30);
        status.setBounds(50, 200,300,30);
        received_dir_label.setBounds(50,230,300,30);
        received_dir.setBounds(50, 260,300,30);
        open.setBounds(100,300,200,30);
        
        start.addActionListener(this);
        open.addActionListener(this);

        received_dir.setLineWrap(true);
        received_dir.setWrapStyleWord(true);
        
        add(label);
        add(address);
        add(start);
        add(status);
        add(received_dir_label);
        add(open);
        add(received_dir);
        
    }

    public void receivedDir(){
        File directory = new File(current_dir);
        if (!directory.exists()) {
            directory.mkdir();
            return;
        } else {
            return;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == start) {
            status.setText("Waiting for server to send file.");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        receivedDir();
                        receiveFile();
                    } catch (UnknownHostException ex) { 
                        System.out.println("Invalid Server Address");
                        status.setText("Server Not Found. Check Address.");
                    } catch (ConnectException ex) {
                            System.out.println("Server not listening");
                            status.setText("Connection Refused by Server.");
                    } catch (Exception ex) {
                            ex.printStackTrace();
                    }
                }
            }).start();

        } else if (e.getSource() == open){
            
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                try {
                    desktop.open(new File(current_dir));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.setTitle("File Receiver");
        client.setSize(400, 500);
        client.setLayout(null);
        client.setVisible(true);
        client.setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    public void receiveFile() throws Exception {
        System.out.println("Starting Client");
        status.setText("Starting Client");
        Socket socket = null;
        //50Mb
        byte[] bytearray = new byte[1024000000];
        //1000000bytes = 1 Megabyte
        int bytesRead;
        int currentTot = 0;
        BufferedOutputStream bos;
        String server_address = address.getText();
        int port = 5000;

        socket = new Socket(server_address, port);
        System.out.println("Connected");
        status.setText("Connected to Server");

        InputStream is = socket.getInputStream();
        
        bytesRead = is.read(bytearray, 0, bytearray.length);
        currentTot = bytesRead;
        status.setText("Receiving File.");

        do {
            bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
            
            if (bytesRead >= 0) {
                currentTot += bytesRead;
            }
        } while (bytesRead > -1);
    
        int i = 0;
        int position = 0;
        byte[] fileName = new byte[1000];
        do {
            fileName[i] = bytearray[i];
            i++;
            position++;
        } while(bytearray[i] != 10);
        position++;
        
        i = 0;
        char c;
        String file_data = "";
        do {
            c = (char) fileName[i];
            file_data+=c;
            i++;
            
        } while(fileName[i] != 0);
        
        i=0;
        String data[] = new String[2];
        StringTokenizer st = new StringTokenizer(file_data, "/");
        while(st.hasMoreTokens()){
            data[i++] = st.nextToken();
        }
        FileOutputStream fos = new FileOutputStream(current_dir + "/" + data[0]);
        bos = new BufferedOutputStream(fos);
        bos.write(bytearray, (position), (currentTot-position));

        // Close connection
        try {
            bos.flush();
            is.close();
            fos.close();
            bos.close();
            socket.close();
            System.out.println("File Transfer Completed");
            status.setText("File Transfer Completed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
