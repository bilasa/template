 // -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;
import Server.RMI.*;
import Server.Common.*;

import java.util.*;

import javax.transaction.InvalidTransactionException;

import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.io.*;

/*
 * Middleware acts as intermediary server between the Client and the 3 different
 * ResourceManagers (flight, car, and room). It also communicates with an additional
 * ResourceManager server for customers.
 */
public abstract class Middleware implements IResourceManager
{
    protected String m_name = "";
    public IResourceManager flightResourceManager = null;
    public IResourceManager carResourceManager = null;
    public IResourceManager roomResourceManager = null;
    public IResourceManager customerResourceManager = null;
    
    public Middleware(String p_name)
    {
        m_name = p_name;
    }
    
    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        updateTransaction(xid, rms);
        return flightResourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
    }
    
    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        updateTransaction(xid, rms);
        return carResourceManager.addCars(xid, location, count, price);
    }
    
    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        updateTransaction(xid, rms);
        return roomResourceManager.addRooms(xid, location, count, price);
    }
    
    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        updateTransaction(xid, rms);
        return flightResourceManager.deleteFlight(xid, flightNum);
    }
    
    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CARS);
        updateTransaction(xid, rms);
        return carResourceManager.deleteCars(xid, location);
    }
    
    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        updateTransaction(xid, rms);
        return roomResourceManager.deleteRooms(xid, location);
    }
    
    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        updateTransaction(xid, rms);
        return flightResourceManager.queryFlight(xid, flightNum);
    }
    
    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        updateTransaction(xid, rms);
        return carResourceManager.queryCars(xid, location);
    }
    
    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        updateTransaction(xid, rms);
        return roomResourceManager.queryRooms(xid, location);
    }
    
    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        updateTransaction(xid, rms);
        return flightResourceManager.queryFlightPrice(xid, flightNum);
    }
    
    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        updateTransaction(xid, rms);
        return carResourceManager.queryCarsPrice(xid, location);
    }
    
    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        updateTransaction(xid, rms);
        return roomResourceManager.queryRoomsPrice(xid, location);
    }
    
    public String queryCustomerInfo(int xid, int customerID) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);
        return customerResourceManager.queryCustomerInfo(xid, customerID);
    }
    
    public int newCustomer(int xid) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);
        return customerResourceManager.newCustomer(xid);
    }
    
    public boolean newCustomer(int xid, int customerID) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);
        return customerResourceManager.newCustomer(xid, customerID);
    }
    
    public boolean deleteCustomer(int xid, int customerID) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);

        ArrayList<ReservedItem> items = customerResourceManager.deleteCustomer_CustomerRM(xid, customerID);

        for (ReservedItem item : items) 
        {
            String key = item.getKey();
            String[] parts = key.split("-");
            int count = item.getCount();

            if (parts[0].equals("flight"))
            {   
                flightResourceManager.reserveFlight_FlightRM(xid, Integer.parseInt(parts[1]), -count);
            }

            if (parts[0].equals("car"))
            {   
                carResourceManager.reserveCar_CarRM(xid, parts[1], -count);
            }

            if (parts[0].equals("room"))
            {   
                roomResourceManager.reserveRoom_RoomRM(xid, parts[1], -count);
            }
        }

        return true;
    }
    
    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);
        
        // Reserve a seat in the flight and get the price for the flight
        Integer flightPrice = flightResourceManager.reserveFlight_FlightRM(xid, flightNum, 1).intValue();
    
        if ((int) flightPrice == -1) 
        {
            return false; // flight reservation failed
        } 
        else {
            return customerResourceManager.reserveFlight_CustomerRM(xid, customerID, flightNum, flightPrice);
        }
    }
    
    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);

        // Reserve a car and get its price
        Integer carPrice = carResourceManager.reserveCar_CarRM(xid, location, 1).intValue();
        
        if ((int) carPrice == -1) 
        {
            return false; // car reservation failed
        } 
        else {
            return customerResourceManager.reserveCar_CustomerRM(xid, customerID, location, carPrice);
        }
    }
    
    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);

        // Reserve a room and get its price
        Integer roomPrice = roomResourceManager.reserveRoom_RoomRM(xid, location, 1).intValue();
        
        if ((int) roomPrice == -1) 
        {
            return false; // room reservation failed
        } 
        else {
            return customerResourceManager.reserveRoom_CustomerRM(xid, customerID, location, roomPrice);
        }
    }

    // Reserve bundle
    public boolean bundle(int xid, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        ArrayList<RESOURCE_MANAGER_TYPE> rms = new ArrayList<RESOURCE_MANAGER_TYPE>();
        rms.add(RESOURCE_MANAGER_TYPE.FLIGHT);
        rms.add(RESOURCE_MANAGER_TYPE.ROOM);
        rms.add(RESOURCE_MANAGER_TYPE.CAR);
        rms.add(RESOURCE_MANAGER_TYPE.CUSTOMER);
        updateTransaction(xid, rms);
        
        ArrayList<Integer> prices = new ArrayList<Integer>();
        int carPrice = -1;
        int roomPrice = -1;
        boolean customer = true;

        // Convert flight numbers from string format to integer format
        ArrayList<Integer> flights  = new ArrayList<Integer>();
        for (String f : flightNumbers) flights.add(Integer.parseInt(f));

        // Validate 
        prices = flightResourceManager.reserveFlights_FlightRM(xid, flights, 1);
        if (car) carPrice = carResourceManager.reserveCar_CarRM(xid, location, 1);
        if (room) roomPrice = roomResourceManager.reserveRoom_RoomRM(xid, location, 1);
        customer = !(customerResourceManager.queryCustomerInfo(xid, customerID).isEmpty());

        // Invalid cases
        if (
            (prices.size() != flightNumbers.size()) ||
            (car && carPrice == -1) || 
            (room && roomPrice == -1) || 
            (customer == false)
        ) { 
            if (prices.size() == flightNumbers.size()) flightResourceManager.reserveFlights_FlightRM(xid, flights, -1);
            if (car && carPrice != -1) carResourceManager.reserveCar_CarRM(xid, location, -1);
            if (room && roomPrice != -1) roomResourceManager.reserveRoom_RoomRM(xid, location, -1);

            return false;
        }
        
        // Reserve items for customer
        customerResourceManager.reserveFlights_CustomerRM(xid, customerID, flights, prices);
        if (car) customerResourceManager.reserveCar_CustomerRM(xid, customerID, location, carPrice);
        if (room) customerResourceManager.reserveRoom_CustomerRM(xid, customerID, location, roomPrice);

        return true; 
    }

    //====================================================================================================
    //====================================================================================================

    /**
     * THE FOLLOWING INCORPORATE THE IMPLEMENTATION OF TRANSACTION MANAGEMENT
     * - START
     * - COMMIT
     * - ABORT
     * - SHUTDOWN
     */

    private HashMap<Integer,Transaction> transactions = new HashMap<Integer,Transaction>();
    private HashMap<Integer,Timer> timers = new HashMap<Integer,Timer>();
    private long TRANSACTION_TIME_LIMIT = 10000;

    // Function to start transaction
    public int startTransaction() throws RemoteException
    {   
        synchronized(this.transactions)
        {
            int xid = (int) new Date().getTime();
            this.transactions.put(xid, new Transaction(xid));

            Timer t = new Timer(new String(xid));
            this.timers.put(xid, t);
            t.schedule(new TimerTask(){
            
                @Override
                public void run() {
                    initiateAbort(xid);
                }
            }, this.TRANSACTION_TIME_LIMIT);

            return xid;
        }
    }

    // Function to commit transaction
    public boolean commitTransaction(int xid) throws RemoteException,TransactionAbortedException,InvalidTransactionException
    {   
        synchronized(this.transactions)
        {
            if (!transactions.containsKey(xid)) 
            {
                throw new InvalidTransactionException(xid,"Cannot commit to a non-existent transaction xid");
            }

            Transaction ts = this.transactions.get(xid);
            ArrayList<Operation> ops = ts.getOperations();

            for (Operation op : ops)
            {
                ArrayList<RESOURCE_MANAGER_TYPE> rms = op.getResourceManagers();

                for (RESOURCE_MANAGER_TYPE rm : rms)
                {
                    switch (rm)
                    {
                        case FLIGHT:
                            flightResourceManager.commit(xid);
                            break;
                        case CAR:
                            carResourceManager.commit(xid);
                            break;
                        case ROOM:
                            roomResourceManager.commit(xid);
                            break;
                        case CUSTOMER:
                            customerResourceManager.commit(xid);
                            break;
                        default:
                            break;
                    }
                }
            }

            this.transactions.remove(xid);
            this.timers.remove(xid);
        }   

        return true;
    }

    // Function to abort transaction
    public boolean abortTransaction(int xid) throws RemoteException,InvalidTransactionException
    {   
        synchronized(this.transactions)
        {
            if (!transactions.containsKey(xid)) {
                throw new InvalidTransactionException(xid,"Cannot abort to a non-existent transaction xid");
            }
            
            return initiateAbort(xid);
        }

        return false;
    }

    // Function to shutdown
    public boolean shutdown() throws Exception
    {   
        synchronized(this.transactions)
        {   
            if (!this.transactions.isEmpty()) return false;

            flightResourceManager.shutdown();
            carResourceManager.shutdown();
            roomResourceManager.shutdown();
            customerResourceManager.shutdown();
        }

        return true;
    }

    // Function to initiate abort
    public boolean initiateAbort(int xid) 
    {
        Transaction ts = this.transactions.get(xid);
        ArrayList<Operation> ops = ts.getOperations();

        for (Operation op : ops)
        {
            ArrayList<RESOURCE_MANAGER_TYPE> rms = op.getResourceManagers();

            for (RESOURCE_MANAGER_TYPE rm : rms)
            {
                switch (rm)
                {
                    case FLIGHT:
                        flightResourceManager.abort(xid);
                        break;
                    case CAR:
                        carResourceManager.abort(xid);
                        break;
                    case ROOM:
                        roomResourceManager.abort(xid);
                        break;
                    case CUSTOMER:
                        customerResourceManager.abort(xid);
                        break;
                    default:
                        break;
                }
            }
        }

        this.transactions.remove(xid);
        this.timers.remove(xid);

        return true;
    }

    // Function to add operation and to update timer for a transaction
    public void updateTransaction(int xid, ArrayList<RESOURCE_MANAGER_TYPE> rms) throws InvalidTransactionException
    {   
        if (!this.transactions.containsKey(xid) || !this.timers.containsKey(xid)) 
        {
            throw new InvalidTransactionException(xid,"Cannot identify the transaction xid by transaction manager");
        }

        Transaction ts = this.transactions.get(xid);
        Timer t = this.timers.get(xid);

        ts.addOperation(new Operation(rms));
        t.schedule(new TimerTask(){
        
            @Override
            public void run() {
                initiateAbort(xid);
            }
        }, this.TRANSACTION_TIME_LIMIT);
    }

    //====================================================================================================
    //====================================================================================================

    /**
     * THE FOLLOWING ARE NOT USED IN RMI ARCHITECTURE, BUT ARE IMPLEMENTED DUE TO INHERITANCE
     */

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
        return false;
    }

    public Integer reserveFlight_FlightRM(int xid, int flightNum, int toReserve) throws RemoteException
    {
        return new Integer(-1);
    }

	// Function to reserve flights (multiple) in FlightResourceManager
    public ArrayList<Integer> reserveFlights_FlightRM(int xid, ArrayList<Integer> flightNums, int toReserve) throws RemoteException
    {
        return new ArrayList<Integer>();
    }

	// Function to reserve car in CarResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
    public Integer reserveCar_CarRM(int xid, String location, int toReserve) throws RemoteException
    {
        return new Integer(-1);
    }

	// Function to reserve room in RoomResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
    public Integer reserveRoom_RoomRM(int xid, String location, int toReserve) throws RemoteException
    {
        return new Integer(-1);
    }

	// Function to reserve flight in CustomerResourceManager (this returns an integer value as updating in the customer resource manager requires latest reserved price of item)
    public boolean reserveFlight_CustomerRM(int xid, int customerID, int flightNum, int price) throws RemoteException
    {
        return false;
    }

	// Function to reserve flights (multiple) in CustomerResourceManager 
    public boolean reserveFlights_CustomerRM(int xid, int customerID, ArrayList<Integer> flightNums, ArrayList<Integer> prices) throws RemoteException
    {
        return false;
    }

	// Function to reserve car in CustomerResourceManager
    public boolean reserveCar_CustomerRM(int xid, int customerID, String location, int price) throws RemoteException
    {
        return false;
    }

	// Function to reserve room in CustomerResourceManager
    public boolean reserveRoom_CustomerRM(int xid, int customerID, String location, int price) throws RemoteException
    {
        return false;
    }

	// Function to reserve item in CustomerResourceManager
    public boolean reserveItem_CustomerRM(int xid, int customerID, String key, String location, int price) throws RemoteException
    {
        return false;
    }

	// Function to delete customer in customer database
    public ArrayList<ReservedItem> deleteCustomer_CustomerRM(int xid, int customerID) throws RemoteException
    {
        return new ArrayList<ReservedItem>();
    }
    
    public String getName() throws RemoteException
    {
        return m_name;
    }
}

