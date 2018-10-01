package Client;

import Server.Actions.AddCarLocationAction;
import Server.Interface.*;
import Client.Common.*;

import java.io.*; 
import java.text.*; 
import java.util.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.lang.*;

public class TCPClient extends Client
{
	private static String s_serverHost = "localhost";
	private static String s_serverName = "Server";
	private static String s_rmiPrefix = "group32";
	private static int s_serverPort = 1099;

	private Socket s = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	
	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			s_serverName = args[1];
		}
		if (args.length > 2)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		// Set the security policy
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Get a reference to the RMIRegister
		try {
			TCPClient client = new TCPClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPClient()
	{
		super();
	}

	public void connectServer()
	{
		connectServer(s_serverHost, s_serverPort, s_serverName);
	}
	
	public void connectServer(String server, int port, String name)
	{
		try {

			boolean first = true;

			while (true) {

				try {
					this.out = new ObjectOutputStream(s.getOutputStream());
					this.in = new ObjectInputStream(s.getInputStream());
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

	public void execute(Command cmd, Vector<String> arguments) throws RemoteException, NumberFormatException
	{
		switch (cmd)
		{
			case Help:
			{
				if (arguments.size() == 1) {
					System.out.println(Command.description());
				} else if (arguments.size() == 2) {
					Command l_cmd = Command.fromString((String)arguments.elementAt(1));
					System.out.println(l_cmd.toString());
				} else {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
				}
				break;
			}
			case AddFlight: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding a new flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				System.out.println("-Flight Seats: " + arguments.elementAt(3));
				System.out.println("-Flight Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));
				int flightSeats = toInt(arguments.elementAt(3));
				int flightPrice = toInt(arguments.elementAt(4));

				// Send request
				TravelAction req = new AddFlightAction(id, flightNum, flightSeats, flightPrice);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Flight added");
				} else {
					System.out.println("Flight could not be added");
				} 
				break;
			}
			case AddCars: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new cars [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				System.out.println("-Number of Cars: " + arguments.elementAt(3));
				System.out.println("-Car Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				int numCars = toInt(arguments.elementAt(3));
				int price = toInt(arguments.elementAt(4));

				// Send request
				TravelAction req = new AddCarLocationAction(id, location, numCars, price);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Cars added");
				} else {
					System.out.println("Cars could not be added");
				}
				break;
			}
			case AddRooms: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new rooms [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				System.out.println("-Number of Rooms: " + arguments.elementAt(3));
				System.out.println("-Room Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				int numRooms = toInt(arguments.elementAt(3));
				int price = toInt(arguments.elementAt(4));

				// Send request
				TravelAction req = new AddRoomLocationAction(id, location, numRooms, price);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Rooms added");
				} else {
					System.out.println("Rooms could not be added");
				}
				break;
			}
			case AddCustomer: {
				checkArgumentsCount(2, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");

				int id = toInt(arguments.elementAt(1));

				TravelAction req = new AddCustomerAction(id);
				this.out.writeObject(req);
				this.out.flush();

				int customer = (int) this.in.readObject();
				System.out.println("Add customer ID: " + customer);
				break;
			}
			case AddCustomerID: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new AddCustomerAction(id, customerID);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Add customer ID: " + customerID);
				} else {
					System.out.println("Customer could not be added");
				}
				break;
			}
			case DeleteFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new DeleteFlightAction(id, flightNum);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Flight Deleted");
				} else {
					System.out.println("Flight could not be deleted");
				}
				break;
			}
			case DeleteCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all cars at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				
				// Send request
				TravelAction req = new DeleteCarLocationAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Cars Deleted");
				} else {
					System.out.println("Cars could not be deleted");
				}
				break;
			}
			case DeleteRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all rooms at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				// Send request
				TravelAction req = new DeleteRoomLocationAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Rooms Deleted");
				} else {
					System.out.println("Rooms could not be deleted");
				}
				break;
			}
			case DeleteCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a customer from the database [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new DeleteCustomerAction(id, customerID);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();
				
				if (res.booleanValue()) {
					System.out.println("Customer Deleted");
				} else {
					System.out.println("Customer could not be deleted");
				}
				break;
			}
			case QueryFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new QueryFlightAction(id, flightNumber);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int res = (int) this.in.readObject();
				System.out.println("Number of seats available: " + res);
				break;
			}
			case QueryCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				// Send request
				TravelAction req = new QueryCarLocationAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int numCars = (int) this.in.readObject();
				System.out.println("Number of cars at this location: " + numCars);
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				// Send request
				TravelAction req = new QueryRoomLocationAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int numRoom = (int) this.in.readObject();
				System.out.println("Number of rooms at this location: " + numRoom);
				break;
			}
			case QueryCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new QueryCustomerAction(id, customerID);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				String bill = (String) this.in.readObject();
				System.out.print(bill);
				break;               
			}
			case QueryFlightPrice: {
				checkArgumentsCount(3, arguments.size());
				
				System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));

				// Send request
				TravelAction req = new QueryFlightPriceAction(id, flightNum);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int price = (int) this.in.readObject();
				System.out.println("Price of a seat: " + price);
				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				// Send request
				TravelAction req = new QueryCarPriceAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int price = (int) this.in.readObject();
				System.out.println("Price of cars at this location: " + price);
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				// Send request
				TravelAction req = new QueryRoomPriceAction(id, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				int price = (int) this.in.readObject();
				System.out.println("Price of rooms at this location: " + price);
				break;
			}
			case ReserveFlight: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving seat in a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Flight Number: " + arguments.elementAt(3));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				int flightNum = toInt(arguments.elementAt(3));

				// Send request
				TravelAction req = new ReserveFlightAction(id, customerID, flightNum);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();

				if (res.booleanValue()) {
					System.out.println("Flight Reserved");
				} else {
					System.out.println("Flight could not be reserved");
				}
				break;
			}
			case ReserveCar: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a car at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Car Location: " + arguments.elementAt(3));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				String location = arguments.elementAt(3);

				// Send request
				TravelAction req = new ReserveCarAction(id, customerID, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();

				if (res.booleanValue()) {
					System.out.println("Car Reserved");
				} else {
					System.out.println("Car could not be reserved");
				}
				break;
			}
			case ReserveRoom: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a room at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Room Location: " + arguments.elementAt(3));
				
				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				String location = arguments.elementAt(3);

				// Send request
				TravelAction req = new ReserveRoomAction(id, customerID, location);
				this.out.writeObject(req);
				this.out.flush();

				// Await response
				Boolean res = (Boolean) this.in.readObject();

				if (res.booleanValue()) {
					System.out.println("Room Reserved");
				} else {
					System.out.println("Room could not be reserved");
				}
				break;
			}
			case Bundle: {
				if (arguments.size() < 7) {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 7 arguments. Location \"help\" or \"help,<CommandName>\"");
					break;
				}

				System.out.println("Reserving an bundle [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				for (int i = 0; i < arguments.size() - 6; ++i)
				{
					System.out.println("-Flight Number: " + arguments.elementAt(3+i));
				}
				System.out.println("-Car Location: " + arguments.elementAt(arguments.size()-2));
				System.out.println("-Room Location: " + arguments.elementAt(arguments.size()-1));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				Vector<String> flightNumbers = new Vector<String>();
				for (int i = 0; i < arguments.size() - 6; ++i)
				{
					flightNumbers.addElement(arguments.elementAt(3+i));
				}
				String location = arguments.elementAt(arguments.size()-3);
				boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
				boolean room = toBoolean(arguments.elementAt(arguments.size()-1));

				// Send request
				TravelAction req = new ReserveBundleAction(id, customerID, flightNumbers, location, car, room);
				this.out.writeObject(req);
				this.out.flush();
				
				// Await response
				Boolean res = (Boolean) this.in.readObject();

				if (res.booleanValue()) {
					System.out.println("Bundle Reserved");
				} else {
					System.out.println("Bundle could not be reserved");
				}
				break;
			}
			case Quit:
				checkArgumentsCount(1, arguments.size());

				this.s.close();
				this.in.close();
				this.out.close();

				System.out.println("Quitting client");
				System.exit(0);
		}
	}
}