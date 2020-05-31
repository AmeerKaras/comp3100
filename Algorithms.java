import java.util.ArrayList;

import javax.print.attribute.standard.JobSheets;

public class Algorithms {

    private ArrayList<Server> servers = new ArrayList<Server>();
	private Server[] systemServers;

	Algorithms(ArrayList<Server> servers, Server[] systemServers) {
		this.servers = servers;
		this.systemServers = systemServers;
	}


	// STATES: 0=inactive, 1=booting, 2=idle, 3=active, 4=unavailable
	

	public Server bFitMem (Job job) {
		Server bestS = null;
		//Sets exist to false
		Boolean exists = false;
		//Instantiate variables to be used for fitness calculation
		int bF = Integer.MAX_VALUE;//Best Fit
		int mA = Integer.MAX_VALUE;// Minimum available


		for (Server s : servers) {
			if ((s.coreCount >= job.cpuCores && s.disk >= job.disk && s.memory >= job.memory)) {
				int fitVal = s.memory - job.memory;  //fitness value of a job is amount of cores in server
				// subtracted by the cores required for the job
				if ((fitVal < bF) || (fitVal == bF && s.availableTime < mA)) {
					bF = fitVal;
					mA = s.availableTime;
					if (s.state == 0 || s.state == 1 || s.state == 2 || s.state == 3) {
						exists = true;
						bestS = s;
					}
				}
			}
		}
		if (exists) {
			return bestS;
		} else {//when the server is running nothing

			int otherBF = Integer.MAX_VALUE;
			Server sAlt = null;
			for (Server s : systemServers) {
				int otherFitVal = s.coreCount - job.cpuCores;
				if (otherFitVal >= 0 && otherFitVal < otherBF && s.disk > job.disk && s.memory > job.memory) {
					otherBF = otherFitVal;//Return the best fit active server based on initial resource
					sAlt = s;
				}
			}
			sAlt.id = 0;
			return sAlt;
		}
	}

	public Server wFitDisk(Job job) {
		//Set worstFit and altFit to a very small number
		int worstFit = Integer.MIN_VALUE;
		int altFit = Integer.MIN_VALUE;
		Server worst = null;
		Server next = null;
		Boolean worstFound = false;
		Boolean nextFound = false;

		//For each server
		for (Server s : servers) {
			if (s.coreCount >= job.cpuCores && s.disk >= job.disk && s.memory >= job.memory && (s.state == 0 || s.state == 2 || s.state == 3)) {
				//calculate the fitness value
				int fitValue = s.disk - job.disk;
				//if fitness > worstFit is available then set worstFit
				if (fitValue > worstFit && (s.availableTime == -1 || s.availableTime == job.submitTime)) {
					worstFit = fitValue;
					worstFound = true;
					worst = s;
					//otherwise set altFit
				} else if (fitValue > altFit && s.availableTime >= 0) {
					altFit = fitValue;
					nextFound = true;
					next = s;
				}
			}
		}
		// if worstFit, return it
		if (worstFound) {
			return worst;
			//otherwise, if altFit, return it
		} else if (nextFound) {
			return next;
		}

		//Return the worst-fit active server based on initial resource capcity
		int lowest = Integer.MIN_VALUE;
		Server curServer = null;
		for (Server s : systemServers) {
			int fit = s.coreCount - job.cpuCores;
			if (fit > lowest && s.disk >= job.disk && s.memory >= job.memory) {
				lowest = fit;
				curServer = s;
			}
		}
		curServer.id = 0; //The server doesn't think it exists unless its 0.
		return curServer;
	}

	public Server bFitDisk (Job job) {
		Server bestS = null;
		//Sets exist to false
		Boolean exists = false;
		//Instantiate variables to be used for fitness calculation
		int bF = Integer.MAX_VALUE;//Best Fit
		int mA = Integer.MAX_VALUE;// Minimum available


		for (Server s : servers) {
			if ((s.coreCount >= job.cpuCores && s.disk >= job.disk && s.memory >= job.memory)) {
				int fitVal = s.disk - job.disk;  //fitness value of a job is amount of cores in server
				// subtracted by the cores required for the job
				if ((fitVal < bF) || (fitVal == bF && s.availableTime < mA)) {
					bF = fitVal;
					mA = s.availableTime;
					if (s.state == 0 || s.state == 1 || s.state == 2 || s.state == 3) {
						exists = true;
						bestS = s;
					}
				}
			}
		}
		if (exists) {
			return bestS;
		} else {//when the server is running nothing

			int otherBF = Integer.MAX_VALUE;
			Server sAlt = null;
			for (Server s : systemServers) {
				int otherFitVal = s.coreCount - job.cpuCores;
				if (otherFitVal >= 0 && otherFitVal < otherBF && s.disk > job.disk && s.memory > job.memory) {
					otherBF = otherFitVal;//Return the best fit active server based on initial resource
					sAlt = s;
				}
			}
			sAlt.id = 0;
			return sAlt;
		}
	}

	/**
	 * Joshua Vazzoler
	 * This first sorts the ArrayList of servers using bubble sort. It then
	 * iterates over the sorted servers and compares the requirements of the 
	 * job the capacity of the servers. As soon as there is a fit, the job will
	 * be assigned to that server. It will iterate until an active server with 
	 * a fit is found, regardless of well it fits.
	 */
    public Server firstFit (Job Job) {
		Server[] serverSort = serverBubbleSort(systemServers); //Calls Bubble sort function, that arranges by server ID.
			
		for (Server s : serverSort) {
			for (Server serverArr : servers) {
				if ((s.type).equals(serverArr.type)) { //Checks if it is the samee
					if (serverArr.coreCount >= Job.cpuCores && serverArr.disk >= Job.disk && serverArr.memory >= Job.memory && serverArr.state != 4) { //Checks if it meets the reqs and is not unavaliable
						return serverArr;
					}
				}
			}
		}
			
		for (Server s : systemServers) { //If none of the avaliable servers aren't a fit for the job will check all the servers whether if avaliable or unavliable.  
			Server tempServ = null;
			if (s.coreCount >= Job.cpuCores && s.disk >= Job.disk && s.memory >= Job.disk && s.state != 4) {
				tempServ = s;
				tempServ.id = 0; 
				return tempServ;
			}
		}
		return null;
	}

	public Server[] serverBubbleSort(Server[] serverSort) {
		int n = serverSort.length;
		for (int i = 0; i < n - 1; i++) {
			for (int j = 0; j < n - i - 1; j++) {
				if (serverSort[j].coreCount > serverSort[j + 1].coreCount) {
					Server tempServ = serverSort[j];
					serverSort[j] = serverSort[j + 1];
					serverSort[j + 1] = tempServ;
				}
			}
		}
		return serverSort;
	}

	/**
	 * Ameer Karas
	 * It iterates over the global ArrayList and uses the fitnessValue (fitValue)
	 * to select the server which will yield the best fit. This is determined by 
	 * finding the smallest fitness value while still ensuring the server has enough 
	 * resources to be able to handle the job.
	 */

	public Server bestFitSort (Job job) {
		Server bestS = null;
		//Sets exist to false
		Boolean exists = false;
		//Instantiate variables to be used for fitness calculation
		int bF = Integer.MAX_VALUE;//Best Fit
		int mA = Integer.MAX_VALUE;// Minimum available

        for (Server s : servers) {
			if ((s.coreCount >= job.cpuCores && s.disk >= job.disk && s.memory >= job.memory)) {
				int fitVal = s.coreCount - job.cpuCores;  //fitness value of a job is amount of cores in server
				// subtracted by the cores required for the job
				if ((fitVal < bF) || (fitVal == bF && s.availableTime < mA)) {
					bF = fitVal;
					mA = s.availableTime;
					if (s.state == 0 || s.state == 1 || s.state == 2 || s.state == 3) {
						exists = true;
						bestS = s;
					}
				}
			}
		}
    if (exists) {
			return bestS;
		} else {//when the server is running nothing

			int otherBF = Integer.MAX_VALUE;
			Server sAlt = null;
			for (Server s : systemServers) {
				int otherFitVal = s.coreCount - job.cpuCores;
				if (otherFitVal >= 0 && otherFitVal < otherBF && s.disk > job.disk && s.memory > job.memory) {
                    otherBF = otherFitVal;//Return the best fit active server based on initial resource
					sAlt = s;
				}
			}
			sAlt.id = 0;
			return sAlt;
		}
	}


	/**
	 * Hannes Venter
	 * It iterates over the global ArrayList and uses the fitnessValue (fitValue)
	 * to select the server which will yield the worst fit. This is the fitValue
	 * with the biggest different and will thus result in the highest cost. 
	 */
    public Server worstFit(Job job) {
		//Set worstFit and altFit to a very small number
		int worstFit = Integer.MIN_VALUE;
		int altFit = Integer.MIN_VALUE;
		Server worst = null;
		Server next = null;
		Boolean worstFound = false;
		Boolean nextFound = false;

		//For each server
		for (Server s : servers) {
			if (s.coreCount >= job.cpuCores && s.disk >= job.disk && s.memory >= job.memory && (s.state == 0 || s.state == 2 || s.state == 3)) {
				//calculate the fitness value
				int fitValue = s.coreCount - job.cpuCores;
				//if fitness > worstFit is available then set worstFit
				if (fitValue > worstFit && (s.availableTime == -1 || s.availableTime == job.submitTime)) {
					worstFit = fitValue;
					worstFound = true;
					worst = s;
				//otherwise set altFit
				} else if (fitValue > altFit && s.availableTime >= 0) {
					altFit = fitValue;
					nextFound = true;
					next = s;
				}
			}
		}
		// if worstFit, return it
		if (worstFound) {
			return worst;
		//otherwise, if altFit, return it
		} else if (nextFound) {
			return next;
		}

		//Return the worst-fit active server based on initial resource capcity
		int lowest = Integer.MIN_VALUE;
		Server curServer = null;
		for (Server s : systemServers) {
			int fit = s.coreCount - job.cpuCores;
			if (fit > lowest && s.disk >= job.disk && s.memory >= job.memory) {
				lowest = fit;
				curServer = s;
			}
		}
		curServer.id = 0; //The server doesn't think it exists unless its 0.
		return curServer;
	}

}