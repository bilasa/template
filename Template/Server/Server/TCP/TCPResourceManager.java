package Server.TCP;

import java.util.*;
import java.io.*; 
import java.util.*;
import java.net.*;
import Server.Interface.*;
import Server.Actions.*;
import Server.Common.*;

public class TCPResourceManager extends ResourceManager 
{
	private static String s_serverName = "Server";
	private static String s_rmiPrefix = "group32";
	private static int s_serverPort = 2876;

	public static void main(String args[])
	{
		if (args.length > 0)
		{
			s_serverName = args[0];
		}

		if (s_serverName.equals("Flights")) 
		{
			s_serverPort = 2607;
		}
		else if (s_serverName.equals("Cars")) 
		{
			s_serverPort = 2708;
		}
		else if (s_serverName.equals("Rooms")) 
		{	
			s_serverPort = 2609;
		}
		else if (s_serverName.equals("Customers")) 
		{
			s_serverPort = 2610;
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Create the RMI server entry
		try {
			// Create a new Server object
			TCPResourceManager server = new TCPResourceManager(s_serverName);

			// Initialize server socket
			ServerSocket ss = new ServerSocket(s_serverPort);

			// Listen to incoming requests
			while (true)
			{   
				// Socket and stream objects to an incoming request
				Socket s = null;

				try {
					// Receive incoming request
					s = ss.accept();
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					// Initialize thread
					Thread t = new Thread() {

						@Override
						public void run() {

							try {
								TravelAction req = (TravelAction) in.readObject();

								Boolean res = null;
								Integer res_ = null;
								ArrayList<Integer> ress_ = null;
								ArrayList<ReservedItem> res_items = null;
                                ArrayList<String> res_a = null;
								String res_s = null;

								switch (req.getSubtype()) {

									case ADD_FLIGHT:

										res = new Boolean(
											server.addFlight(
												((AddFlightAction) req).getXid(),
												((AddFlightAction) req).getFlightNumber(),
												((AddFlightAction) req).getFlightSeats(),
												((AddFlightAction) req).getFlightPrice()
											)
										);
										break;

									case ADD_CAR_LOCATION:

										res = new Boolean(
											server.addCars(
												((AddCarLocationAction) req).getXid(),
												((AddCarLocationAction) req).getLocation(),
												((AddCarLocationAction) req).getNumCars(),
												((AddCarLocationAction) req).getPrice()
												)
											);
										break;

									case ADD_ROOM_LOCATION:

										res = new Boolean(
											server.addRooms(
												((AddRoomLocationAction) req).getXid(),
												((AddRoomLocationAction) req).getLocation(),
												((AddRoomLocationAction) req).getNumRooms(),
												((AddRoomLocationAction) req).getPrice()
												)
											);
										break;

									case ADD_CUSTOMER:

										int xid = ((AddCustomerAction) req).getXid();
										int customerID = ((AddCustomerAction) req).getCustomerID();

										if (customerID == -1) {
											res_ = new Integer(
												server.newCustomer(xid)
											);
										}
										else {
											res = new Boolean(
												server.newCustomer(xid, customerID)
											);
										}
										break;

									case QUERY_FLIGHT:

										res_ = new Integer(
											server.queryFlight(
												((QueryFlightAction) req).getXid(),
												((QueryFlightAction) req).getFlightNumber()
												)
											);
										break;

									case QUERY_CAR_LOCATION:

										res_ = new Integer(
											server.queryCars(
												((QueryCarLocationAction) req).getXid(),
												((QueryCarLocationAction) req).getLocation()
											)
										);
										break;

									case QUERY_ROOM_LOCATION:

										res_ = new Integer(
											server.queryRooms(
												((QueryRoomLocationAction) req).getXid(),
												((QueryRoomLocationAction) req).getLocation()
											)
											);
										break;

									case QUERY_CUSTOMER:

										res_s = new String(
											server.queryCustomerInfo(
												((QueryCustomerAction) req).getXid(),
												((QueryCustomerAction) req).getCustomerID()
											)
										);
										break;

									case QUERY_FLIGHT_PRICE:

										res_ = new Integer(
											server.queryFlightPrice(
												((QueryFlightPriceAction) req).getXid(),
												((QueryFlightPriceAction) req).getFlightNumber()
											)
										);
										break;

									case QUERY_CAR_PRICE:

										res_ = new Integer(
											server.queryCars(
												((QueryCarPriceAction) req).getXid(),
												((QueryCarPriceAction) req).getLocation()
											)
										);
										break;

									case QUERY_ROOM_PRICE:

										res_ = new Integer(
											server.queryRoomsPrice(
												((QueryRoomPriceAction) req).getXid(),
												((QueryRoomPriceAction) req).getLocation()
											)
										);
										break;

									case DELETE_FLIGHT:

										res = new Boolean(
											server.deleteFlight(
												((DeleteFlightAction) req).getXid(),
												((DeleteFlightAction) req).getFlightNumber()
											)
										);
										break;

									case DELETE_CAR_LOCATION:

										res = new Boolean(
											server.deleteCars(
												((DeleteCarLocationAction) req).getXid(),
												((DeleteCarLocationAction) req).getLocation()
											)
										);
										break;

									case DELETE_ROOM_LOCATION:

										res = new Boolean(
											server.deleteRooms(
												((DeleteRoomLocationAction) req).getXid(),
												((DeleteRoomLocationAction) req).getLocation()
											)
										);
										break;

									case DELETE_CUSTOMER:

										res_items = new ArrayList<ReservedItem>(
											server.deleteCustomer_CustomerRM(
												((DeleteCustomerAction) req).getXid(),
												((DeleteCustomerAction) req).getCustomerID()
											)
										);
										break;

									case RESERVE_FLIGHT_RM:

										res_ = new Integer(
											server.reserveFlight_FlightRM(
												((ReserveFlightRmAction) req).getXid(),
												((ReserveFlightRmAction) req).getFlightNumber(),
												((ReserveFlightRmAction) req).getToReserve()
											)	
										);
										break;

									case RESERVE_FLIGHT_CUSTOMER_RM:

										res = new Boolean(
											server.reserveFlight_CustomerRM(
												((ReserveFlightCustomerRmAction) req).getXid(),
												((ReserveFlightCustomerRmAction) req).getCustomerID(),
												((ReserveFlightCustomerRmAction) req).getFlightNumber(),
												((ReserveFlightCustomerRmAction) req).getPrice()
											)
										);
										break;
									
									case RESERVE_FLIGHTS_RM:
										
										ress_ = new ArrayList<Integer>(
											server.reserveFlights_FlightRM(
												((ReserveFlightsRmAction) req).getXid(),
												((ReserveFlightsRmAction) req).getFlightNumbers(),
												((ReserveFlightsRmAction) req).getToReserve()
											)
										);
										break;

									case RESERVE_FLIGHTS_CUSTOMER_RM:

										res = new Boolean(
											server.reserveFlights_CustomerRM(
												((ReserveFlightsCustomerRmAction) req).getXid(),
												((ReserveFlightsCustomerRmAction) req).getCustomerID(),
												((ReserveFlightsCustomerRmAction) req).getFlightNumbers(),
												((ReserveFlightsCustomerRmAction) req).getPrices()
											)
										);
										break;

									case RESERVE_CAR_RM:
										
										res_ = new Integer(
											server.reserveCar_CarRM(
												((ReserveCarRmAction) req).getXid(),
												((ReserveCarRmAction) req).getLocation(),
												((ReserveCarRmAction) req).getToReserve()
											)	
										);
										break;

									case RESERVE_CAR_CUSTOMER_RM: 

										res = new Boolean(
											server.reserveCar_CustomerRM(
												((ReserveCarCustomerRmAction) req).getXid(),
												((ReserveCarCustomerRmAction) req).getCustomerID(),
												((ReserveCarCustomerRmAction) req).getLocation(),
												((ReserveCarCustomerRmAction) req).getPrice()
											)
										);
										break;  

									case RESERVE_ROOM_RM:
										
										res_ = new Integer(
											server.reserveRoom_RoomRM(
												((ReserveRoomRmAction) req).getXid(),
												((ReserveRoomRmAction) req).getLocation(),
												((ReserveRoomRmAction) req).getToReserve()
											)
										);
										break;

									case RESERVE_ROOM_CUSTOMER_RM:

										res = new Boolean(
											server.reserveCar_CustomerRM(
												((ReserveRoomCustomerRmAction) req).getXid(),
												((ReserveRoomCustomerRmAction) req).getCustomerID(),
												((ReserveRoomCustomerRmAction) req).getLocation(),
												((ReserveRoomCustomerRmAction) req).getPrice()
											)
										);
										break;

									case RESERVE_BUNDLE_CUSTOMER_RM:

										res = new Boolean(
											server.bundle(
												((ReserveBundleCustomerRmAction) req).getXid(),
												((ReserveBundleCustomerRmAction) req).getCustomerID(),
												((ReserveBundleCustomerRmAction) req).getFlightNumbers(),
												((ReserveBundleCustomerRmAction) req).getFlightPrices(),
												((ReserveBundleCustomerRmAction) req).getLocation(),
												((ReserveBundleCustomerRmAction) req).getCar(),
												((ReserveBundleCustomerRmAction) req).getCarPrice(),
												((ReserveBundleCustomerRmAction) req).getRoom(),
												((ReserveBundleCustomerRmAction) req).getRoomPrice()
											)	
										);
										break;
                                        
                                    case GET_SUMMARY:

 										res_a = new ArrayList<String>(
											server.getSummary(
												((GetSummaryAction) req).getXid()
											)
										);
										break;
    

									default: 
										break;
								}

								if (res != null) {
									out.writeObject(res); 
								}
								else if (res_ != null) {
									out.writeObject(res_);
								}
								else if (res_s != null) {
									out.writeObject(res_s);
								}
								else if (ress_ != null) {
									out.writeObject(ress_);
								}
								else if (res_items != null) {
									out.writeObject(res_items);
								} 
								else if (res_a != null) {
									out.writeObject(res_a);
								} 
								else {
									out.writeObject(new String("NULL"));
								}

								out.flush(); 
							} 
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (ClassNotFoundException e) {
								e.printStackTrace();
							}

							try { 
								in.close();
								out.close();   
								//s.close();
							}
							catch(IOException e) { 
								e.printStackTrace(); 
							}
						}
					};

					t.start();
				}
				catch (Exception e) {
					s.close(); 
					e.printStackTrace(); 
				}
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPResourceManager(String name)
	{
		super(name);
	}
}
