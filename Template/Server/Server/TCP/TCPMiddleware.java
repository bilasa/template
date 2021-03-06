package Server.TCP;

import java.util.*;
import java.io.*;
import java.net.*;
import Server.Interface.*;
import Server.Common.*;
import Server.Actions.*;

public class TCPMiddleware {

	private static ServerSocket ss = null;

	// TCP Middleware name and port
	private static String s_serverName = "TCPMiddleware";  
	private static int s_middleware_host = 3334;
	// RMs (host names and ports)
	private static String s_flight_host = "Flights";
	private static String s_car_host = "Cars";
	private static String s_room_host = "Rooms";
	private static String s_customer_host = "Customers";
	private static int s_serverPort_flight = 2607;
	private static int s_serverPort_car = 2708;
	private static int s_serverPort_room = 2609;
	private static int s_serverPort_customer = 2610;
	// Prefix
	private static String  s_rmiPrefix = "group32";

	public static void main(String[] args) {

		// Retrieve RM names
		if (args.length > 0)
		{
			s_flight_host = args[0];
		}
		if (args.length > 1)
		{
			s_car_host = args[1];
		}
		if (args.length > 2)
		{
			s_room_host = args[2];
		}
		if (args.length > 3)
		{
			s_customer_host = args[3];
		}
		if (args.length > 4)
		{
			System.err.println((char)27 + "[31;1mTCPMiddleware exception: " + (char)27 + "[0mUsage: java tcp.TCPMiddleware [server_hostname [server_tcpobject]]");
			System.exit(1);
		}

		// Security policy 
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
		}
		
		try {
			ss = new ServerSocket(s_middleware_host);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// Incoming 
		while (true)
		{	
			Socket s = null;
			
			try {
				// Accept client
				s = ss.accept();
				ObjectInputStream in_client = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream out_client = new ObjectOutputStream(s.getOutputStream());
				
				// Thread intinitalization
				Thread t = new Thread() {
					
					@Override
					public void run() {
						
						// Handle client request
						try {
							TravelAction req = (TravelAction) in_client.readObject();
	
							switch (req.getType()) 
							{
								case FLIGHT_ACTION:

									Socket s_f = new Socket(s_flight_host, s_serverPort_flight);
									ObjectOutputStream out_f = new ObjectOutputStream(s_f.getOutputStream());
									ObjectInputStream in_f = new ObjectInputStream(s_f.getInputStream());

									out_f.writeObject(req);
									out_f.flush();
									
									out_client.writeObject(in_f.readObject());
									out_client.flush();

									in_f.close();
									out_f.close();	
									s_f.close();
									
									break;

								case CAR_ACTION:

									Socket s_c = new Socket(s_car_host, s_serverPort_car);
									ObjectOutputStream out_c = new ObjectOutputStream(s_c.getOutputStream());
									out_c.flush();
									ObjectInputStream in_c = new ObjectInputStream(s_c.getInputStream());

									out_c.writeObject(req);
									out_c.flush();

									out_client.writeObject(in_c.readObject());
									out_client.flush();

									in_c.close();
									out_c.close();	
									s_c.close();

									break;

								case ROOM_ACTION:

									Socket s_r = new Socket(s_room_host, s_serverPort_room);
									ObjectOutputStream out_r = new ObjectOutputStream(s_r.getOutputStream());
									out_r.flush();
									ObjectInputStream in_r = new ObjectInputStream(s_r.getInputStream());

									out_r.writeObject(req);
									out_r.flush();

									out_client.writeObject(in_r.readObject());
									out_client.flush();

									in_r.close();
									out_r.close();	
									s_r.close();

									break;

								case CUSTOMER_ACTION:

									Socket s_cust = new Socket(s_customer_host, s_serverPort_customer);
									ObjectOutputStream out_cust = new ObjectOutputStream(s_cust.getOutputStream());
									ObjectInputStream in_cust = new ObjectInputStream(s_cust.getInputStream());
									Integer price = null;

									// Handle reservations
									switch (req.getSubtype()) 
									{
										
										case RESERVE_FLIGHT_CUSTOMER_RM:

											Socket s_flightRm = new Socket(s_flight_host, s_serverPort_flight);
											ObjectOutputStream out_flightRm = new ObjectOutputStream(s_flightRm.getOutputStream());
											out_flightRm.flush();
											ObjectInputStream in_flightRm = new ObjectInputStream(s_flightRm.getInputStream());

											out_flightRm.writeObject(
												new ReserveFlightRmAction(
													((ReserveFlightCustomerRmAction) req).getXid(),
													((ReserveFlightCustomerRmAction) req).getFlightNumber(),
													1    
												)
											);
											out_flightRm.flush();

											price = (Integer) in_flightRm.readObject();

											if (!price.equals(new Integer(-1))) {

												out_cust.writeObject(
													new ReserveFlightCustomerRmAction(
														((ReserveFlightCustomerRmAction) req).getXid(),
														((ReserveFlightCustomerRmAction) req).getCustomerID(),
														((ReserveFlightCustomerRmAction) req).getFlightNumber(),
														(int) price
													)
												);

												out_client.writeObject(in_cust.readObject());
											}
											else {
												out_client.writeObject(new Boolean(false));
											}

											in_flightRm.close();
											out_flightRm.close();
											s_flightRm.close();

											break;

										case RESERVE_CAR_CUSTOMER_RM:
											
											Socket s_carRm = new Socket(s_car_host, s_serverPort_car);
											ObjectOutputStream out_carRm = new ObjectOutputStream(s_carRm.getOutputStream());
											out_carRm.flush();
											ObjectInputStream in_carRm = new ObjectInputStream(s_carRm.getInputStream());
											
											out_carRm.writeObject(
												new ReserveCarRmAction(
													((ReserveCarCustomerRmAction) req).getXid(),
													((ReserveCarCustomerRmAction) req).getLocation(),
													1    
												)
											);
											out_carRm.flush();
						
											price = (Integer) in_carRm.readObject();
						
											if (!price.equals(new Integer(-1))) {
						
												out_cust.writeObject(
													new ReserveCarCustomerRmAction(
														((ReserveCarCustomerRmAction) req).getXid(),
														((ReserveCarCustomerRmAction) req).getCustomerID(),
														((ReserveCarCustomerRmAction) req).getLocation(),
														(int) price
													)
												);
						
												out_client.writeObject(in_cust.readObject());
											}
											else {
												out_client.writeObject(new Boolean(false));
											}
						
											out_cust.flush();
											out_client.flush();
						
											in_carRm.close();
											out_carRm.close();
											s_carRm.close();

											break;

										case RESERVE_ROOM_CUSTOMER_RM:

											Socket s_roomRm = new Socket(s_room_host, s_serverPort_room);
											ObjectOutputStream out_roomRm = new ObjectOutputStream(s_roomRm.getOutputStream());
											out_roomRm.flush();
											ObjectInputStream in_roomRm = new ObjectInputStream(s_roomRm.getInputStream());
											
											out_roomRm.writeObject(
												new ReserveRoomRmAction(
													((ReserveRoomCustomerRmAction) req).getXid(),
													((ReserveRoomCustomerRmAction) req).getLocation(),
													1    
												)
											);
											out_roomRm.flush();
						
											price = (Integer) in_roomRm.readObject();
						
											if (!price.equals(new Integer(-1))) {
						
												out_cust.writeObject(
													new ReserveRoomCustomerRmAction(
														((ReserveRoomCustomerRmAction) req).getXid(),
														((ReserveRoomCustomerRmAction) req).getCustomerID(),
														((ReserveRoomCustomerRmAction) req).getLocation(),
														(int) price
													)
												);
						
												out_client.writeObject(in_cust.readObject());
											}
											else {
												out_client.writeObject(new Boolean(false));
											}
						
											out_cust.flush();
											out_client.flush();
						
											in_roomRm.close();
											out_roomRm.close();
											s_roomRm.close();

											break;

										case RESERVE_BUNDLE_CUSTOMER_RM:

											int xid = ((ReserveBundleCustomerRmAction) req).getXid();
											int customer = ((ReserveBundleCustomerRmAction) req).getCustomerID();
											Vector<String> flights = ((ReserveBundleCustomerRmAction) req).getFlightNumbers();
											String loc = ((ReserveBundleCustomerRmAction) req).getLocation();
											boolean car = ((ReserveBundleCustomerRmAction) req).getCar();
											boolean room = ((ReserveBundleCustomerRmAction) req).getRoom();
											
											ArrayList<Integer> flights_ = new ArrayList<Integer>();
											for (String s : flights) flights_.add(Integer.parseInt(s));
										
											ArrayList<Integer> prices = new ArrayList<Integer>();
											Integer carPrice = null;
											Integer roomPrice = null;
											boolean customerExists = true;
											int len = flights.size();
						
											if (len > 0)
											{   
												Socket s_fb = new Socket(s_flight_host, s_serverPort_flight);
												ObjectOutputStream out_fb = new ObjectOutputStream(s_fb.getOutputStream());
												ObjectInputStream in_fb = new ObjectInputStream(s_fb.getInputStream());
												
												out_fb.writeObject(
													new ReserveFlightsRmAction(
														xid, 
														flights_, 
														1
													) 
												);
												out_fb.flush();
						
												prices = (ArrayList<Integer>) in_fb.readObject();
						
												in_fb.close();
												out_fb.close();
												s_fb.close();
											}
						
											if (car) 
											{
												Socket s_cb = new Socket(s_car_host, s_serverPort_car);
												ObjectOutputStream out_cb = new ObjectOutputStream(s_cb.getOutputStream());
												ObjectInputStream in_cb = new ObjectInputStream(s_cb.getInputStream());
												
												out_cb.writeObject(
													new ReserveCarRmAction(
														xid,
														loc, 
														1
													)
												);
												out_cb.flush();
						
												carPrice = (Integer) in_cb.readObject();
						
												in_cb.close();
												out_cb.close();
												s_cb.close();
											}
						
											if (room)
											{
												Socket s_rb = new Socket(s_room_host, s_serverPort_room);
												ObjectOutputStream out_rb = new ObjectOutputStream(s_rb.getOutputStream());
												ObjectInputStream in_rb = new ObjectInputStream(s_rb.getInputStream());
						
												out_rb.writeObject(
													new ReserveRoomRmAction(
														xid,
														loc,
														1
													)
												);
												out_rb.flush();
						
												roomPrice = (Integer) in_rb.readObject();
						
												in_rb.close();
												out_rb.close();
												s_rb.close();
											}
						
											// Check if customer exists
											out_cust.writeObject(
												new QueryCustomerAction(xid, customer)
											);
											out_cust.flush();
											customerExists = !(((String) in_cust.readObject()).isEmpty());
						
											if (
												(prices.size() != flights_.size()) || 
												(car && carPrice.equals(new Integer(-1))) || 
												(room && roomPrice.equals(new Integer(-1))) ||
												customerExists == false
											) {	
												// Invalid bundle
												out_client.writeObject(new Boolean(false));
												out_client.flush();
						
												// Reset
												if (prices.size() == flights_.size()) 
												{
													Socket s_fb = new Socket(s_flight_host, s_serverPort_flight);
													ObjectOutputStream out_fb = new ObjectOutputStream(s_fb.getOutputStream());
													out_fb.flush();
													ObjectInputStream in_fb = new ObjectInputStream(s_fb.getInputStream());
						
													out_fb.writeObject(
														new ReserveFlightsRmAction(
															xid, 
															flights_, 
															-1
														) 
													);
													out_fb.flush();
						
													prices = (ArrayList<Integer>) in_fb.readObject();
						
													in_fb.close();
													out_fb.close();
													s_fb.close();
												}
						
												if (car && !carPrice.equals(new Integer(-1)))
												{
													Socket s_cb = new Socket(s_car_host, s_serverPort_car);
													ObjectOutputStream out_cb = new ObjectOutputStream(s_cb.getOutputStream());
													out_cb.flush();
													ObjectInputStream in_cb = new ObjectInputStream(s_cb.getInputStream());
													
													out_cb.writeObject(
														new ReserveCarRmAction(
															xid,
															loc, 
															-1
														)
													);
													out_cb.flush();
						
													carPrice = (Integer) in_cb.readObject();
						
													in_cb.close();
													out_cb.close();
													s_cb.close();
												}
						
												if (room && !roomPrice.equals(new Integer(-1)))
												{
													Socket s_rb = new Socket(s_room_host, s_serverPort_room);
													ObjectOutputStream out_rb = new ObjectOutputStream(s_rb.getOutputStream());
													out_rb.flush();
													ObjectInputStream in_rb = new ObjectInputStream(s_rb.getInputStream());
												
													out_rb.writeObject(
														new ReserveRoomRmAction(
															xid,
															loc,
															-1
														)
													);
													out_rb.flush();
						
													roomPrice = (Integer) in_rb.readObject();
						
													in_rb.close();
													out_rb.close();
													s_rb.close();
												}

												out_client.writeObject(new Boolean(false));
												out_client.flush();
											}
											else {
												Socket inv = new Socket(s_customer_host, s_serverPort_customer);
												ObjectOutputStream out_cust2 = new ObjectOutputStream(inv.getOutputStream());
												ObjectInputStream in_cust2 = new ObjectInputStream(inv.getInputStream());

												out_cust2.writeObject(
													new ReserveBundleCustomerRmAction(
														xid,
														customer,
														flights,
														prices,
														loc,
														car,
														(car? carPrice : new Integer(-1)),
														room,
														(room? roomPrice : new Integer(-1))
													)
												);
												out_cust2.flush();

												Boolean bundled = (Boolean) in_cust2.readObject();

												/*
												// Update customer flights
												out_cust2.writeObject(
													new ReserveFlightsCustomerRmAction(
														xid,
														customer,
														flights_,
														prices
													)
												);
												out_cust2.flush();
												Boolean r1 = (Boolean) in_cust2.readObject();
												// Update customer car

												if (car)
												{
													out_cust2.writeObject(
														new ReserveCarCustomerRmAction(
															xid, 
															customer, 
															loc,
															(int) carPrice
														)
													);
													out_cust2.flush();
													r1 = (Boolean) in_cust2.readObject();
												}
												
												
												// Update customer room
												if (room)
												{
													out_cust2.writeObject(
														new ReserveRoomCustomerRmAction(
															xid, 
															customer, 
															loc, 
															roomPrice
														)
													);
													out_cust2.flush();
													r1 = (Boolean) in_cust2.readObject();
												}
												*/
												in_cust2.close();
												out_cust2.close();
												inv.close();
							
												out_client.writeObject(bundled);
												out_client.flush();
											}

										case DELETE_CUSTOMER:
											
											// Flight RM
											Socket sf = new Socket(s_flight_host, s_serverPort_flight);
											ObjectOutputStream outf = new ObjectOutputStream(sf.getOutputStream());
											ObjectInputStream inf = new ObjectInputStream(sf.getInputStream());
						
											// Car RM
											Socket sc = new Socket(s_car_host, s_serverPort_car);
											ObjectOutputStream outc = new ObjectOutputStream(sc.getOutputStream());
											ObjectInputStream inc = new ObjectInputStream(sc.getInputStream());
						
											// Room RM
											Socket sr = new Socket(s_room_host, s_serverPort_room);
											ObjectOutputStream outr = new ObjectOutputStream(sr.getOutputStream());
											ObjectInputStream inr = new ObjectInputStream(sr.getInputStream());
											
											ArrayList<ReservedItem> items = new ArrayList<ReservedItem>();
											
											out_cust.writeObject(
												new DeleteCustomerAction( 
													((DeleteCustomerAction) req).getXid(),
													((DeleteCustomerAction) req).getCustomerID()
												)
											);
											out_cust.flush();
						
											items = (ArrayList<ReservedItem>) in_cust.readObject();
						
											// Update items in respective RMs
											for (ReservedItem item : items) {
												
												String key = item.getKey().toLowerCase();
												int count = item.getCount();
												String[] parts = key.split("-");
												Boolean deleted = null;
						
												// Flight item
												if (parts[0].equals("flight")) 
												{
													outf.writeObject(
														new ReserveFlightRmAction(
															req.getXid(),
															Integer.parseInt(parts[1]),
															-count
														)    
													);
													outf.flush();
						
													deleted = (Boolean) inf.readObject();
												}
						
												// Car item
												if (parts[0].equals("car"))
												{
													outc.writeObject(
														new ReserveCarRmAction(
															req.getXid(),
															parts[1],
															-count
														)
													);
													outc.flush();
						
													deleted = (Boolean) inc.readObject();
												}
						
												// Room item
												if (parts[0].equals("room"))
												{
													outr.writeObject(
														new ReserveRoomRmAction(
															req.getXid(),
															parts[1],
															-count
														)
													);
												}
											}

											inf.close();
											outf.close();
											sf.close();
											inc.close();
											outc.close();
											sc.close();
											inr.close();
											outr.close();
											sr.close();
						
											out_client.writeObject(new Boolean(true));

											break;

										default: // only interact with customer rm

											out_cust.writeObject(req);
											out_cust.flush();

											out_client.writeObject(in_cust.readObject());
											out_client.flush();

											break;
									}

									in_cust.close();
									out_cust.close();
									s_cust.close();

									break;

								default:
									System.out.println("Error: Action type not recognized.");
									break;
							}
						}
						catch (IOException e) {
							e.printStackTrace();
						}
						catch (ClassNotFoundException e) {
							e.printStackTrace();
						}

						try {
							in_client.close();
							out_client.close();
						}
						catch (IOException e) {
							e.printStackTrace();	
						}
					}
				};

				t.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
