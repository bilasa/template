// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;
import Server.LockManager.*;

import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class ResourceManager extends LockManager implements IResourceManager
{
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();
    
    // Hashmap indexed by xid to store the local histories of each transaction
    protected Map<Integer, RMHashMap> local = new HashMap<Integer, RMHashMap>();

	public ResourceManager(String p_name)
	{
		m_name = p_name;
	}

	// Reads a data item
	protected RMItem readData(int xid, String key) throws DeadlockException
	{
        try {
            Lock(xid, key, TransactionLockObject.LockType.LOCK_READ);
        }
        catch (DeadlockException deadlock) {
            throw deadlock;
        }
        
        // Get the local history for the transaction
        synchronized(local) {
            RMHashMap local_data = local.get(xid);
            if (local_data == null)
            {
                // if the local history has not been created yet, read from main memory
                local_data = new RMHashMap();
                
                RMItem item;
                synchronized(m_data) {
                    item = m_data.get(key);
                }
                if (item != null)
                {
                    // add item to local store
                    local_data.put(key, item);
                    
                    // add the local history to the hashmap of local histories
                    local.put(xid, local_data);
                    
                    return (RMItem)item.clone();
                }
                return null;
            }
            else
            {
                // Check if local history already contains the item
                RMItem local_item = local_data.get(key);
                if (local_item != null)
                {
                    // Return the item found in the local history
                    return (RMItem)local_item.clone();
                }
                
                else
                {
                    // otherwise, check the main memory
                    RMItem item;
                    synchronized(m_data) {
                        item = m_data.get(key);
                    }
                    if (item != null)
                    {
                        // add item to local history
                        local_data.put(key, item);
                        
                        // update the hashmap of local histories
                        local.put(xid, local_data);
                        
                        return (RMItem)item.clone();
                    }
                    return null;
                }
            }
        }
	}

	// Writes a data item
	protected void writeData(int xid, String key, RMItem value) throws DeadlockException
	{
        try {
            Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
        }
        catch (DeadlockException deadlock) {
            throw deadlock;
        }
        
        // Get the local history for the transaction
        synchronized(local) {
            RMHashMap local_data = local.get(xid);
            if (local_data == null)
            {
                // if the local history has not been created yet, create it
                local_data = new RMHashMap();
            }
            
            local_data.put(key, value);
            
            // update the hashmap of local histories
            local.put(xid, local_data);
        }
	}
    
    // Commits a transaction
    protected boolean commit(int xid) throws RemoteException
    {
        synchronized(local) {
            RMHashMap local_data = local.get(xid);
            if (local_data == null)
            {
                Trace.warn("RM::commit(" + xid + ") failed--the local history does not exist");
                return false;
            }
            else
            {
                synchronized(m_data) {
                    // Put all items in local history into main memory
                    for (String key : local_data.keySet())
                    {
                        RMItem item = local_data.get(key);
                        m_data.put(key, item);
                    }
                }
                
                // Unlock all locks owned by transaction
                UnlockAll(xid);
                Trace.info("RM::commit(" + xid + ") succeeded");
                return true;
            }
        }
    }
    
    // Aborts a transaction
    protected void abort(int xid) throws RemoteException
    {
        if (local.get(xid) != null)
        {
            // Remove the local history
            local.remove(xid);
        }
    }
    
    // Exits the server
    public boolean shutdown() throws RemoteException
    {
        
    }

	// Remove the item out of storage
	protected void removeData(int xid, String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key)
	{
		Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		// Check if there is such an item in the storage
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(xid, curObj.getKey());
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(int xid, String key)
	{
		Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0;  
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
		return value;
	}    

	// Query the price of an item
	protected int queryPrice(int xid, String key)
	{
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0; 
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
		return value;        
	}

	// Reserve an item
	protected boolean reserveItem(int xid, int customerID, String key, String location)
	{
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );        
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		} 

		// Check if the item is available
		ReservableItem item = (ReservableItem)readData(xid, key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{            
			customer.reserve(key, location, item.getPrice());        
			writeData(xid, customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(xid, item.getKey(), item);

			Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}        
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
		Flight curObj = (Flight)readData(xid, Flight.getKey(flightNum));
		if (curObj == null)
		{
			// Doesn't exist yet, add it
			Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
		}
		else
		{
			// Add seats to existing flight and update the price if greater than zero
			curObj.setCount(curObj.getCount() + flightSeats);
			if (flightPrice > 0)
			{
				curObj.setPrice(flightPrice);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice);
		}
		return true;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		Car curObj = (Car)readData(xid, Car.getKey(location));
		if (curObj == null)
		{
			// Car location doesn't exist yet, add it
			Car newObj = new Car(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$" + price);
		}
		else
		{
			// Add count to existing car location and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0)
			{
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		Room curObj = (Room)readData(xid, Room.getKey(location));
		if (curObj == null)
		{
			// Room location doesn't exist yet, add it
			Room newObj = new Room(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addRooms(" + xid + ") created new room location " + location + ", count=" + count + ", price=$" + price);
		} else {
			// Add count to existing object and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0)
			{
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addRooms(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) throws RemoteException
	{
		return deleteItem(xid, Flight.getKey(flightNum));
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws RemoteException
	{
		return deleteItem(xid, Car.getKey(location));
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws RemoteException
	{
		return deleteItem(xid, Room.getKey(location));
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) throws RemoteException
	{
		return queryNum(xid, Flight.getKey(flightNum));
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws RemoteException
	{
		return queryNum(xid, Car.getKey(location));
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws RemoteException
	{
		return queryNum(xid, Room.getKey(location));
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) throws RemoteException
	{
		return queryPrice(xid, Flight.getKey(flightNum));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws RemoteException
	{
		return queryPrice(xid, Car.getKey(location));
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws RemoteException
	{
		return queryPrice(xid, Room.getKey(location));
	}

	public String queryCustomerInfo(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			// NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			return "";
		}
		else
		{
			Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
			System.out.println(customer.getBill());
			return customer.getBill();
		}
	}

	public int newCustomer(int xid) throws RemoteException
	{
        Trace.info("RM::newCustomer(" + xid + ") called");
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt(String.valueOf(xid) +
			String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
			String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer customer = new Customer(cid);
		writeData(xid, customer.getKey(), customer);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
		return cid;
	}

	public boolean newCustomer(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			customer = new Customer(customerID);
			writeData(xid, customer.getKey(), customer);
			Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
			return true;
		}
		else
		{
			Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
			return false;
		}
	}

	public boolean deleteCustomer(int xid, int customerID) throws RemoteException
	{
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			return false;
		}
		else
		{            
			// Increase the reserved numbers of all reservable items which the customer reserved. 
 			RMHashMap reservations = customer.getReservations();
			for (String reservedKey : reservations.keySet())
			{        
				ReservedItem reserveditem = customer.getReservedItem(reservedKey);
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
				ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
				item.setReserved(item.getReserved() - reserveditem.getCount());
				item.setCount(item.getCount() + reserveditem.getCount());
				writeData(xid, item.getKey(), item);
			}

			// Remove the customer from the storage
			removeData(xid, customer.getKey());
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
			return true;
		}
	}

	// Adds flight reservation to this customer
	public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
	{
		return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
	{
		return reserveItem(xid, customerID, Car.getKey(location), location);
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
	{
		return reserveItem(xid, customerID, Room.getKey(location), location);
	}

	/* NOTE: The following functions are to support the Client-Middleware-RMs design */
	
	// Function to reserve flight in FlightResourceManager
	public Integer reserveFlight_FlightRM(int xid, int flightNum, int toReserve) throws RemoteException
	{	
		Trace.info("RM::updateFlight(" + xid + ", " + flightNum + ") called");
		
		// Retrieve flight
		Flight curObj = (Flight) readData(xid, Flight.getKey(flightNum));

		if (curObj == null) return new Integer(-1);

		// Count and reservations
		int nCount = curObj.getCount() - toReserve;
		int nReserved = curObj.getReserved() + toReserve;
	
		if (nCount < 0 || nReserved < 0) return new Integer(-1);
		// Update 
		curObj.setCount(nCount);
		curObj.setReserved(nReserved);
		writeData(xid, curObj.getKey(), curObj);

		return new Integer(curObj.getPrice());
	}

	// Function to reserve flights (multiple) in FlightResourceManager
	public ArrayList<Integer> reserveFlights_FlightRM(int xid, ArrayList<Integer> flightNums, int toReserve) 
	{
		ArrayList<Integer> prices = new ArrayList<Integer>();

		for (int i = 0; i < flightNums.size(); i++)
		{
			Flight curObj = (Flight) readData(xid, Flight.getKey(flightNums.get(i)));

			if (curObj == null) return new ArrayList<Integer>();

			int nCount = curObj.getCount() - toReserve;
			int nReserved = curObj.getReserved() + toReserve;

			if (nCount < 0 || nReserved < 0) return new ArrayList<Integer>();
		}

		for (int i = 0; i < flightNums.size(); i++)
		{
			Flight curObj = (Flight) readData(xid, Flight.getKey(flightNums.get(i)));

			int nCount = curObj.getCount() - toReserve;
			int nReserved = curObj.getReserved() + toReserve;
			
			curObj.setCount(nCount);
			curObj.setReserved(nReserved);
			writeData(xid, curObj.getKey(), curObj);

			int price = curObj.getPrice();
			prices.add(price);
		}

		return prices;
	}

	// Function to reserve car in CarResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
	public Integer reserveCar_CarRM(int xid, String location, int toReserve)
	{	
		Trace.info("RM::updateCars(" + xid + ", " + location + ") called");

		// Retrieve car
		Car curObj = (Car) readData(xid, Car.getKey(location));

		if (curObj == null) return new Integer(-1);

		// Count and reservations
		int nCount = curObj.getCount() - toReserve;
		int nReserved = curObj.getReserved() + toReserve;

		if (nCount < 0 || nReserved < 0) return new Integer(-1);

		// Update 
		curObj.setCount(nCount);
		curObj.setReserved(nReserved);
		writeData(xid, curObj.getKey(), curObj);

		return new Integer(curObj.getPrice());
	}

	// Function to reserve room in RoomResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
	public Integer reserveRoom_RoomRM(int xid, String location, int toReserve)
	{
		Trace.info("RM::updateRooms(" + xid + ", " + location + ") called");

		// Reserve room
		Room curObj = (Room) readData(xid, Room.getKey(location));

		if (curObj == null) return new Integer(-1);

		// Count and reservations
		int nCount = curObj.getCount() - toReserve;
		int nReserved = curObj.getReserved() + toReserve;

		if (nCount < 0 || nReserved < 0) return new Integer(-1);

		// Update 
		curObj.setCount(nCount);
		curObj.setReserved(nReserved);
		writeData(xid, curObj.getKey(), curObj);

		return new Integer(curObj.getPrice());
	}

	// Function to reserve flight in CustomerResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
	public boolean reserveFlight_CustomerRM(int xid, int customerID, int flightNum, int price) throws RemoteException
	{
		return reserveItem_CustomerRM(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum), price);
	}

	// Function to reserve flights (multiple) in CustomerResourceManager 
	public boolean reserveFlights_CustomerRM(int xid, int customerID, ArrayList<Integer> flightNums, ArrayList<Integer> prices) 
	{	
		boolean success = true;

		for (int i = 0; i < flightNums.size(); i++) {

			success = reserveItem_CustomerRM(xid, customerID, Flight.getKey(flightNums.get(i)), String.valueOf(flightNums.get(i)), prices.get(i));
			if (!success) return false;
		}

		return success;
	}

	// Function to reserve car in CustomerResourceManager
	public boolean reserveCar_CustomerRM(int xid, int customerID, String location, int price) throws RemoteException
	{
		return reserveItem_CustomerRM(xid, customerID, Car.getKey(location), location, price);
	}

	// Function to reserve room in CustomerResourceManager
	public boolean reserveRoom_CustomerRM(int xid, int customerID, String location, int price) throws RemoteException
	{
		return reserveItem_CustomerRM(xid, customerID, Room.getKey(location), location, price);
	}

	// Function to reserve item in CustomerResourceManager
	public boolean reserveItem_CustomerRM(int xid, int customerID, String key, String location, int price)
	{
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );   
		// Retrieve customer
		Customer customer = (Customer) readData(xid, Customer.getKey(customerID));

		if (customer == null)
		{	
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		} 

		// Update customer
		customer.reserve(key, location, price);        
		writeData(xid, customer.getKey(), customer);
		Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
		
		return true;
	}

	// Function to delete customer in customer database
	public ArrayList<ReservedItem> deleteCustomer_CustomerRM(int xid, int customerID) throws RemoteException 
	{	
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");

		// Retrieve customer 
		Customer customer = (Customer) readData(xid, Customer.getKey(customerID));

		if (customer == null)
		{
			Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			return new ArrayList<ReservedItem>();
		}

		ArrayList<ReservedItem> res = new ArrayList<ReservedItem>();
		RMHashMap reservations = customer.getReservations();
		
		for (String reservedKey : reservations.keySet()) {
			ReservedItem item = customer.getReservedItem(reservedKey);
			res.add(item);
		}

		// Remove customer from storage
		removeData(xid, customer.getKey());
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");

		return res;
	}

	// Function to bundle (Not used)
	public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{	
		return false;
	} 

	// Function to reserve bundle (TCP)
	public boolean bundle(
		int xid, 
		int customerID, 
		Vector<String> flightNumbers, 
		ArrayList<Integer> flightPrices, 
		String location, boolean car, 
		Integer carPrice, 
		boolean room, 
		Integer roomPrice) throws RemoteException
	{	
		Customer customer = (Customer) readData(xid, Customer.getKey(customerID));

		if (customer == null)
		{	
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ")  failed--customer doesn't exist");
			return false;
		} 

		// Reserve flights
		for (int i = 0; i < flightNumbers.size() ; i++) {
			reserveFlight_CustomerRM(xid, customerID, Integer.parseInt(flightNumbers.get(i)), flightPrices.get(i));
		}

		// Reserve car
		if (car) 
		{	
			reserveCar_CustomerRM(xid, customerID, location, carPrice);
		}

		// Reserve room 
		if (room) 
		{	
			reserveRoom_CustomerRM(xid, customerID, location, roomPrice);
		}

		return true;
	}
    
    // Function to get a summary of all customers' item purchases
    public ArrayList<String> getSummary(int xid) {
    
    	Trace.info("RM::getSummary(" + xid + ") called");
	
        // List to store bills in
        ArrayList<String> bills = new ArrayList<String>();
        
        // Add bill for each customer in hashmap
        for (RMItem item : m_data.values()) {
            Customer customer = (Customer) item;
            bills.add(customer.getBill());
        }
        
        return bills;
    }

	// Function to get resource manager's name
	public String getName() throws RemoteException
	{
		return m_name;
	}
}
 
