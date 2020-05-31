//COMP3100, DISTRIBUTED SYSTEMS ASSIGNMENT, STAGE 1.
//GROUP: 23.
//STUDENTS: 45228353 JOSHUA VAZZOLER, 44908903 HANNES VENTER, 44948956 AMEER KARAS.


//Packages for server:
import java.net.*;
import java.io.*;

//Packages for XML Parser, Week 3:
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import java.util.ArrayList;

//Class Client
public class Client {
    private Socket socket  = null;
    private BufferedReader inputStream = null;
    private DataOutputStream outputStream = null;
    
    private ArrayList<Server> serverList = new ArrayList<Server>();
    private Server[] servers = new Server[1];
    private int largest = 0;
    private String state;
    private Boolean completed = false;
    private String algType = "ff";


    public Client (String address, int port) {

        try {
            socket = new Socket(address, port);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch(UnknownHostException z) {
            System.out.println("SERVERERROR, ERRORCODE:" + z);
        }
        catch(IOException x) {
            System.out.println("SERVERERROR, ERRORCODE:" + x);
        }
    }

    public void XMLParse() {
        try {
			File systemXML = new File("system.xml");

            //Retrieve Document Factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            //Build Document
			Document document = builder.parse(systemXML);

            //Normalise the XML Structure
            document.getDocumentElement().normalize();
            
			NodeList serverList = document.getElementsByTagName("server");
            servers = new Server[serverList.getLength()];
            

			for (int i = 0; i < serverList.getLength(); i++) {
                //These are local variables for the parsing of the XML.
				Element server = (Element) serverList.item(i);
				String tipe = server.getAttribute("type");
				int lim = Integer.parseInt(server.getAttribute("limit"));
				int boot = Integer.parseInt(server.getAttribute("bootupTime"));
				float rate = Float.parseFloat(server.getAttribute("rate"));
				int core = Integer.parseInt(server.getAttribute("coreCount"));
				int mem = Integer.parseInt(server.getAttribute("memory"));
				int disk = Integer.parseInt(server.getAttribute("disk"));
				Server temp = new Server(i, tipe, lim, boot, rate, core, mem, disk);
				servers[i] = temp;
			}
			largest = setLargest();
		} catch (Exception i) {
            System.out.println("SERVERERROR, ERRORCODE:" + i);
            i.printStackTrace();
		}

    }

    public void clientIntialRun() {
		sendMessage("HELO");
		state = receiveMessage();
		sendMessage("AUTH " + System.getProperty("user.name"));
		state = receiveMessage();
		XMLParse();
		sendMessage("REDY");
		state = receiveMessage();

		if (state.equals("NONE")) {
			serverQuit();
        } 
        else {
			while (!completed) {
				if (state.equals("OK")) {
					sendMessage("REDY");
					state = receiveMessage();
                }
                
				if (state.equals("NONE")) {
					completed = true;
					break;
                }
                
                // Parse job information received here.
				String[] jobInfo = state.split("\\s+"); // Break the job information up so we can create obj.
				Job job = new Job(Integer.parseInt(jobInfo[1]), Integer.parseInt(jobInfo[2]),
						Integer.parseInt(jobInfo[3]), Integer.parseInt(jobInfo[4]), Integer.parseInt(jobInfo[5]),
						Integer.parseInt(jobInfo[6]));

				sendMessage("RESC All"); // Get all server information.
                state = receiveMessage();
				sendMessage("OK");

                state = receiveMessage();
				serverList = new ArrayList<Server>();
				while (!state.equals(".")) {
					// We know the server has stopped sending information when we get ".".
					// Therefore, we'll keeping reading information in and adding array until then.

					String[] serverInfo = state.split("\\s+");
					// Adding Server information to ArrayList for later use.
                    serverList.add(
							new Server(serverInfo[0], Integer.parseInt(serverInfo[1]), Integer.parseInt(serverInfo[2]),
									Integer.parseInt(serverInfo[3]), Integer.parseInt(serverInfo[4]),
									Integer.parseInt(serverInfo[5]), Integer.parseInt(serverInfo[6])));
					sendMessage("OK");
                    state = receiveMessage();
				}

				Algorithms algs = new Algorithms(serverList, servers);

				Server toBeSent = null;
				if (algType.equals("bf")) {
                    toBeSent = algs.bestFit(job);
                    sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);

                } else if (algType.equals("bfm")) {
                    toBeSent = algs.bFitMem(job);
                    sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);
                } else if (algType.equals("bfd")) {
                    toBeSent = algs.bFitDisk(job);
                    sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);
                } else if (algType.equals("wfd")) {
                    toBeSent = algs.wFitDisk(job);
                    sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);
				} else if (algType.equals("ff")) {
                    toBeSent = algs.firstFit(job);
					sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);
				} else if (algType.equals("wf")) {
                    toBeSent = algs.worstFit(job);
					sendMessage("SCHD " + job.id + " " + toBeSent.type + " " + toBeSent.id);
				} else {
					// FROM STAGE 1
					String[] jobData = state.split("\\s+");
					int cnt = Integer.parseInt(jobData[2]);
					sendMessage("SCHD " + cnt + " " + servers[largest].type + " " + "0");
				}

                state = receiveMessage();
			}
		}
		serverQuit();
	}

    public void sendMessage(String msg) {
        
        //Week 2 code:
        try {
            outputStream.write(msg.getBytes());
            outputStream.flush();

        } catch (IOException i) {
            System.out.println("SERVERERROR, ERRORCODE:" + i);
        }

    }

    public String receiveMessage(){ 
		String msg = "";
		try {
			while (!inputStream.ready()) {
			}
			while (inputStream.ready()) {
				msg += (char) inputStream.read();
            }
            
			state = msg;
		} catch (IOException i) {
            System.out.println("SERVERERROR, ERRORCODE:" + i);
        }
		return msg;
    }

    public void serverQuit(){
        try {
            sendMessage("QUIT");
            state = receiveMessage();
            if (state == "QUIT") {
                inputStream.close();
                outputStream.close();
                socket.close();
            }
        } catch (IOException i) {
            System.out.println("SERVERERROR, ERRORCODE:" + i);
        }
    }

    public int setLargest() {
        int largest = servers[0].id;

		for (int i = 0; i < servers.length; i++) {
			if (servers[i].coreCount > servers[largest].coreCount) {
				largest = servers[i].id;
			}
		}
		return largest;
    }

    public static void main(String args[]) {
        Client client = new Client ("127.0.0.1", 50000);
        if (args.length == 2) {
            if (args[0].equals("-a")) {
                if (args[1].equals("bf")) {
                    client.algType = "bf";
                } else if (args[1].equals("bfd")) {
                    client.algType = "bfd";
                } else if (args[1].equals("bfm")) {
                    client.algType = "bfm";
                } else if (args[1].equals("wfd")) {
                    client.algType = "wfd";
                } else if (args[1].equals("wf")) {
                    client.algType = "wf";
                } else if (args[1].equals("ff")) {
                    client.algType = "ff";
                }
            }
        }
        client.clientIntialRun();    
    }

}