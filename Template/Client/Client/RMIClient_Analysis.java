package Client;

import Server.Interface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import java.util.*;
import java.io.*;

public class RMIClient_Analysis extends Client_Analysis
{
	private static String s_serverHost = "host";
	private static String s_serverName = "RMIMiddleware";
	private static String s_rmiPrefix = "group32";
	private static int s_serverPort = 2156;
	private static Integer its = null;

	public static void main(String args[])
	{	
		if (args.length > 0)
		{	
			System.out.println("feefe");
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{	
			System.out.println("feefe3");
			s_serverName = args[1];
		}
		if (args.length > 2)
		{	
			System.out.println("ab");
			its = Integer.parseInt(args[2]);
		}
		if (args.length > 3)
		{	
			System.out.println("feefefwffw");
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		// Set the security policy
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}
			System.out.println("here");
		// Get a reference to the RMIRegister
		try {
			RMIClient_Analysis client = new RMIClient_Analysis();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public RMIClient_Analysis()
	{
		super();
	}

	public void connectServer()
	{
			System.out.println("here2");
		connectServer(s_serverHost, s_serverPort, s_serverName);
	}

	public void connectServer(String server, int port, String name)
	{
		try {
			boolean first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(server, port);
					System.out.println(server);
					System.out.println(port);
					m_resourceManager = (IResourceManager) registry.lookup(s_rmiPrefix + name);
					System.out.println("initiate");
					client_id = UUID.randomUUID().toString();
					stamps = new HashMap<Integer,Long>();
					iterations = its;
					System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					if (first) {
						System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}