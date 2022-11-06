package structuredESS;

import java.awt.Color;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Int2D;
import sweep.GUIStateSweep;

public class Group implements Steppable {
	//location
	public int x; //group location
	public int y; //group location
	//group info
	public Bag members;
	public int groupSize;
	public int groupID;
	public double groupCap;
	public double fissionSize; // fission if the group size is too large
	public int fusionCounts; //how many times a group fusion when group size is too small
	//group migration rate
	public double groupMigrationRate;
	//get group structure data
	public double groupHawkFrequency; //group hawk frequency
	public double groupHawkResMean; //mean hawk resources for the group 
	public double groupDoveResMean; //mean dove resources for the group
	public double groupResMean; //mean resources for whole group
	public double groupHawkResSD; //hawk resource SD
	public double groupDoveResSD; //dove resource SD
	public double groupResSD; //whole group resource SD
	
	//connect to other classes
	public Stoppable event = null;
	public GroupProbe probe;
	
	//constructor
	public Group(SimEnvironment state, int x, int y, int groupID, Bag agents) {
		super();
		this.x = x;
		this.y = y;
		this.groupID = groupID;
		members = new Bag(agents); //create a bag contains agents already in the groups
		for(int i=0; i<members.numObjs; i++) {
			Agent a = (Agent)members.objs[i];
			a.setGroup(this); //assign each agent into this group
		}
		this.groupSize = members.numObjs;
		this.probe = state.probe;
		this.fusionCounts = 0; // when group size is too small
		this.groupMigrationRate = 0.2 * state.random.nextDouble();
		
		colorGroupByStructure(state);
	}
	
	public Group(SimEnvironment state, int x, int y, int groupID) {
		super();
		this.x = x;
		this.y = y;
		this.groupID = groupID;
		members = new Bag(); //create a new bag for new members
		//do I need to cast SimEnvironment?? I don't think so
		this.probe = state.probe;
		
	}
	
	@Override
	public void step(SimState state) {
		//The group die when no one is in the group
		if(groupSize == 0) {
			die((SimEnvironment)state);
		}
		//fission-fusion dynamics
		fission((SimEnvironment)state);
		fusion((SimEnvironment)state);
		//collect the data
		getStructureData();
		getResourceData();
		
		//color group -- should do this at the end step because we need to get all required data before coloring it
		colorGroupByStructure((SimEnvironment)state);
	}
	
	
	/**
	 * Death of the group: remove the group from the space; clear the member; 
	 * and the group size should be zero
	 * @param state
	 */
	public void die(SimEnvironment state) {
		state.sparseSpace.remove(this); //remove this group
		event.stop(); // stop the event
		members.clear();//clear the bag
		groupSize = members.numObjs; //now there is no agents in the group
	}
	
	
	/*
	 * *****************************************************************************
	 *                            Fission-Fusion Dynamics
	 * *****************************************************************************
	 */
	/**
	 * Group will be broken down when group size is too large.
	 * Fission size varies with a constant of C
	 * (1) find all empty locations, (2)randomly select an empty location, (3)create a new empty group
	 * (4) shuffle the members of the original group, (5) spilt to two groups
	 * No matter fission or not, get the data
	 * @param state
	 */
	public void fission(SimEnvironment state) {
		fissionSize = state.fissionC * ((state.nHawks + state.nDoves)/state.nGroup); //fission when group is too large
		int initialGroupSize = (state.nHawks + state.nDoves)/ state.nGroup;
		groupCap = (double)state.populationCap / (double) initialGroupSize; //maximum groups in the population; cast as double
		
		if(state.sparseSpace.getAllObjects().numObjs < groupCap) { //fission occurs only when group number is less than group capacity
			if(groupSize > fissionSize) {
				//Going to fission!! get the data for these groups before they fission
				probe.setFissionCounts(1);
				probe.setFissionHF(groupHawkFrequency);
				probe.setFissionHawkResMean(groupHawkResMean);
				probe.setFissionDoveResMean(groupDoveResMean);
				probe.setFissionResMean(groupResMean);
				probe.setFissionHawkResSD(groupHawkResSD);
				probe.setFissionDoveResSD(groupDoveResSD);
				probe.setFissionResSD(groupResSD);
				//Start the fission
				ArrayList<Int2D> emptyLocation = state.sparseSpace.getAllEmptyLocations();
				Int2D newLocation = emptyLocation.get(state.random.nextInt(emptyLocation.size()));
				Int2D oldLocation = new Int2D(this.x, this.y);
				//create a new group, and then move the members from old group to new group
				state.groupID ++;
				Group newGroup = new Group(state, newLocation.x, newLocation.y, state.groupID); //create a new group
				Group oldGroup = new Group(state, this.x, this.y, this.groupID); //old group
				//remember to verify the groupID for both new and old groups
				members.shuffle(state.random); //shuffle the members before fission
				for(int i=0; i<members.numObjs; i++) {
					if(i %2 == 0) //randomly separate to two groups
						newGroup.addMembers((Agent)members.objs[i]); 
					else
						oldGroup.addMembers((Agent)members.objs[i]);
				}
				this.die(state); //This original group die and got removed after fission
				//set the new and old group
				state.sparseSpace.setObjectLocation(newGroup, newLocation);
				state.sparseSpace.setObjectLocation(oldGroup, oldLocation);
				newGroup.scheduleSelf(state);
				oldGroup.scheduleSelf(state);
			}
			else { //if not fission, also get the data
				probe.setNonFissionCounts(1);
				probe.setNonFissionHF(groupHawkFrequency);
				probe.setNonFissionHawkResMean(groupHawkResMean);
				probe.setNonFissionDoveResMean(groupDoveResMean);
				probe.setNonFissionResMean(groupResMean);
				probe.setNonFissionHawkResSD(groupHawkResSD);
				probe.setNonFissionDoveResSD(groupDoveResSD);
				probe.setNonFissionResSD(groupResSD);	
			}
		}
	}
	
	/**
	 * Small groups that contains too few agents will merge to other groups.
	 * @param state
	 */
	public void fusion(SimEnvironment state) { //fusion when group size is too small
		Bag currentGroups = state.sparseSpace.getAllObjects();
		Bag tempBag = new Bag();
		if(groupSize < state.fusionSize) { //if the group size is too small
			if(currentGroups.numObjs > 1) { //if there is another group
				Group other = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)]; //find another group
				while(this.equals(other)) { //keep seeking another group until find one
					other = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)];
				}
				for(int i=0; i<groupSize; i++) {
					tempBag.add((Agent)members.objs[i]); //put the small group into a temp bag because this group is going to die
				}
				this.die(state);
				for(int i=0; i< tempBag.numObjs; i++) { //add the members to the other group
					other.addMembers((Agent)tempBag.objs[i]);
				}
				other.fusionCounts ++ ; //get fusion counts
			}
		}
	}

	/*
	 * ****************************************************************************
	 *                             Migration
	 * ****************************************************************************
	 */
	/**
	 * add members to the group and then set the agent into this group
	 * @param agent
	 * @return
	 */
	public boolean addMembers(Agent agent) {
		final boolean results = members.add(agent); //add a member to the group
		if(results == true) { //if adding a member
			groupSize = members.numObjs; //already have the member added
			agent.setGroup(this); //set this agent into this group
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * remove members from the group
	 * @param agent
	 * @return
	 */
	public boolean removeMembers(Agent agent) {
		final boolean results = members.remove(agent); //remove a member off the group
		if(results) {
			groupSize = members.numObjs; // already remove that member
			return true;
		}
		else {
			return false;
		}
	}
	
	/*
	 * ********************************************************************
	 *                             Scheduling
	 * ********************************************************************
	 */
	/**
	 * schedule the groups in the simulation environment
	 * @param state
	 */
	public void scheduleSelf(SimEnvironment state) {
		event = state.schedule.scheduleRepeating(state.schedule.getSteps()+1, 1, this); //set the order
	}
	
	/*
	 * **********************************************************************
	 *                        Data Collecting
	 * **********************************************************************
	 */
	
	public void getStructureData() {
		double hawks = 0;
		double doves = 0;
		double hawkOffsprings = 0;
		double doveOffsprings = 0;
		double hawkCounts = 0;
		double doveCounts = 0;
		groupHawkFrequency = 0;
		if(members.numObjs != 0) { //if there is any members in the group
			for(int i=0; i<members.numObjs; i++) {
				Agent a = (Agent)members.objs[i];
				if(a.strategy == SimEnvironment.HAWK) { //get hawk data
					hawks ++;
					probe.setGroupHawks(1);
					if(a.age + 1 > a.lifeSpan) {
						hawkOffsprings += a.reproductionNum;
						hawkCounts ++;
					}
				}
				else if(a.strategy == SimEnvironment.DOVE) { //get dove data
					doves ++;
					probe.setGroupDoves(1);
					if(a.age + 1 > a.lifeSpan) {
						doveOffsprings += a.reproductionNum;
						doveCounts ++;
					}
				}
			}
			//group data
			groupHawkFrequency = hawks/ (hawks+ doves);
			//population data
			probe.setPopHawks((double)hawks);
			probe.setPopDoves((double)doves);
			probe.setPopSize((hawks + doves)); //sum up group size for population counts
			probe.setPopHawkOffsprings(hawkOffsprings, hawkCounts);
			probe.setPopDoveOffsprings(doveOffsprings, doveCounts);
			probe.setGroupSize(groupSize);//it is an integer
			//egalitarianism
			if(groupHawkFrequency >=0 && groupHawkFrequency < 0.2) {
				probe.setGrade1(1);
			}
			else if(groupHawkFrequency >=0.2 && groupHawkFrequency < 0.4) {
				probe.setGrade2(1);
			}
			else if(groupHawkFrequency >=0.4 && groupHawkFrequency <0.6) {
				probe.setGrade3(1);
			}
			else if(groupHawkFrequency >=0.6 && groupHawkFrequency < 0.8) {
				probe.setGrade4(1);
			}
			else if(groupHawkFrequency >= 0.8 && groupHawkFrequency <=1) {
				probe.setGrade5(1);
			}
		}
	}
	
	public void getResourceData() {
		double hawks = 0; //number of hawks in the group
		double doves = 0; //number of doves in the group
		double totalN = 0; //number of total agents in the group
		double sumGroupHawkRes = 0; //sum of hawk resources
		double sum2GroupHawkRes = 0; //sum 2 of hawk resources
		double sumGroupDoveRes = 0; //sum of dove resources
		double sum2GroupDoveRes = 0; //sum 2 of hawk resources
		double sumGroupRes = 0; //sum of group resource
		double sum2GroupRes = 0; //sum 2 of group resource
		for(int i=0; i<members.numObjs; i++) {
			Agent a = (Agent)members.objs[i];
			if(a.strategy == SimEnvironment.HAWK) {
				hawks ++;
				sumGroupHawkRes += a.resources;
				sum2GroupHawkRes += a.resources * a.resources;
			}
			else if(a.strategy == SimEnvironment.DOVE) {
				doves ++;
				sumGroupDoveRes += a.resources;
				sum2GroupDoveRes += a.resources * a.resources;
			}
		}//end of for
		//group resources
		sumGroupRes = sumGroupHawkRes + sumGroupDoveRes;
		sum2GroupRes = sum2GroupHawkRes + sum2GroupDoveRes;
		totalN = hawks + doves; //total number of agents in the group
		groupHawkResMean = probe.mean(sumGroupHawkRes, hawks);
		groupDoveResMean = probe.mean(sumGroupDoveRes, doves);
		groupResMean = probe.mean (sumGroupRes, totalN);
		groupHawkResSD = probe.sampleSD(sumGroupHawkRes, sum2GroupHawkRes, hawks);
		groupDoveResSD = probe.sampleSD(sumGroupDoveRes, sum2GroupDoveRes, doves);
		groupResSD = probe.sampleSD(sumGroupRes, sum2GroupRes, totalN);
		
		//probe
		probe.getGroupSS(sumGroupRes, sum2GroupRes, totalN);
		probe.setPopResMean(sumGroupRes, sum2GroupRes);
		probe.setPopHawkResMean(sumGroupHawkRes, sum2GroupHawkRes);
		probe.setPopDoveResMean(sumGroupDoveRes, sum2GroupDoveRes);
		//Grade resources probe
		if(groupHawkFrequency >=0 && groupHawkFrequency < 0.2) {
			probe.setGrade1Res(groupHawkResMean);
		}
		else if(groupHawkFrequency >=0.2 && groupHawkFrequency < 0.4) {
			probe.setGrade2Res(groupHawkResMean);
		}
		else if(groupHawkFrequency >=0.4 && groupHawkFrequency <0.6) {
			probe.setGrade3Res(groupHawkResMean);
		}
		else if(groupHawkFrequency >=0.6 && groupHawkFrequency < 0.8) {
			probe.setGrade4Res(groupHawkResMean);
		}
		else if(groupHawkFrequency >= 0.8 && groupHawkFrequency <=1) {
			probe.setGrade5Res(groupHawkResMean);
		}
	}
	

	
	/*
	 * *****************************************************************************
	 *                             Coloring the Groups
	 * *****************************************************************************
	 */
	public void colorGroupByStructure(SimEnvironment state) {
		if(!state.graphic) return;
		final double groupSizeCap = state.fissionC * ((state.nHawks + state.nDoves)/ state.nGroup);
		float groupDensity = (float)((double)members.numObjs/ groupSizeCap);
		//make sure the groupDensity is between 0-1
		if(groupDensity >1) groupDensity = 1;
		if(groupDensity <0) groupDensity = 0;
		//set up the color based on the hawk frequency and dove frequency
		float hawkFreq = (float) groupHawkFrequency; //return groupHawkFrequency
		float doveFreq = (float) (1- hawkFreq); //return groupDoveFrequency
		//set the color
		setColor(state, hawkFreq, (float)0, doveFreq, groupDensity);
	}
	/**
	 * set the color for the group
	 * @param state
	 * @param red
	 * @param green
	 * @param blue
	 * @param opacity
	 */
	public void setColor(SimEnvironment state, float red, float green, float blue, float opacity) {
		Color c = new Color(red, green, blue, opacity);
		OvalPortrayal2D o = new OvalPortrayal2D(c);
		GUIStateSweep guiState = (GUIStateSweep)state.gui; //I don't quite understand here
		if(state.sparseSpace != null) { //if the groups are in a sparseGrid2D space
			guiState.agentsPortrayalSparseGrid.setPortrayalForObject(this,o); //the second argument is color???
		}
	}
}
