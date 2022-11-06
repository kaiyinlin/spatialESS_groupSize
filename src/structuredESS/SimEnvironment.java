package structuredESS;

import sim.util.Bag;
import sweep.SimStateSweep;

public class SimEnvironment extends SimStateSweep {
	//agent setting
	public int averageLifeSpan = 150; //average life span of population
	public int lifeSpanSD = 15; //SD of life span
	public double selfishness_H = 0; //0-1; 0 means that hawks equally share resources
	public double selfishness_D = 0; //0-1; 0 means that doves equally share resources
	public int startEnergy = 0; //newborn's start energy
	public boolean inheritance = false; //whether parents can pass resources to offspring
	public double inheritPortion = 0.2; //how much resources pass to offspring
	public boolean preference = false; //preference on choosing opponents
	public double preferProb_H = 0;//preference on choosing another hawks;
	public double preferProb_D = 0.5; //preference on choosing another doves;
	public int id; //each agent acquires an ID when making agents
	public String idString; // transfer id to String
	
	//game setting
	public final static int HAWK = 1; //hawk-dove
	public final static int DOVE = 0; //hawk-dove
	public double benefit = 6; //hawk-dove
	public double cost = 8; //hawk-dove
	public double T = 6; //prisoner's dilemma
	public double R = 4; //prisoner's dilemma
	public double P = 2; //prisoner's dilemma
	public double S = 0; //prisoner's dilemma
	public double stagStag = 6; //stag hunt
	public double hareStag = 4; //stag hunt
	public double hareHare = 4; //stag hunt
	public double stagHare = 0; //stag hunt
	public static int HD = 0; //hawk-dove
	public static int PD = 1; //prisoner's dilemma
	public static int SH = 2; //stag hunt
	public static int SD = 3; //snowdrift
	public int gameType = 0; //default game type is Hawk-Dove
	
	//reproduction setting
//	public double aveReproductionThreshold = 10; //reproduction threshold 
//	public double reproductionThresholdSD = 0.5; //SD for reproduction threshold 
	public boolean puberty = false; //reproduce after puberty
	public int reproductionAge = 30; //first reproduction age after puberty (REPRODUCTION PROJECT)
	public int gestation = 1; //gestation (REPRODUCTION PROJECT)
	public double reproductionCost = 30; //a cost for reproduction
	public double mutation = 0.01; //strategy mutation occurs when reproduction occurs
	public boolean dispersal = false; //offspring dispersal
	public double dispersalRate = 0.1; //probability of dispersal for each baby
	public double difference = 0;
	
	
	//population setting
	public int populationCap = 1000; //maximum population capacity
	public int nHawks = 120; //initial number of hawks
	public int nDoves = 120; //initial number of doves
	
	//group setting
	public int nGroup = 24; //initial number of groups
	public int groupID = 0; //set up an ID for each group
	public int fissionC = 2; // a constant to control the group size cap
	public int fusionSize = 3; //fusion size
	public boolean graphic = true;
	
	//probe
	public GroupProbe probe = null;
	
	/*
	 * *******************************************************************
	 *                         Constructor
	 * *******************************************************************                        
	 */
	public SimEnvironment(long seed, Class observer) {
		super(seed, observer);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * ***************************************************************************
	 *                                Start a Simulation
	 * ***************************************************************************
	 */
	/**
	 * Start a simulation: create a sparse space for groups that contains agents in the groups
	 * 
	 */
	public void start() {
		super.start();
		space = make2DSpace(spaces, 1, gridWidth, gridHeight); //making a space for groups
		probe = new GroupProbe(this, burnIn, 0, 10000, 2500);
		makeGroups();
		//set a observer to get data, set probes for the observer
		if(observer != null) {
			observer.initialize(space, spaces);
			((Experimenter)observer).setProbe(probe);
		}			
	}
	
	/*
	 * ***************************************************************************
	 *                           Making Groups and Agents
	 * ***************************************************************************
	 */
	/**
	 * Making groups and set the groups in a grid space
	 */
	public void makeGroups() {
		for(int i=0; i< nGroup; i++) {
			if(nGroup <= gridWidth * gridHeight) {
				int x = random.nextInt(gridWidth);
				int y = random.nextInt(gridHeight);
				do {
					x = random.nextInt(gridWidth);
					y = random.nextInt(gridHeight);
				}while(sparseSpace.getObjectsAtLocation(x, y) != null);
				groupID ++;
				fillAgentsInGroup(x, y, groupID);
			}
			else {
				System.out.println("Too many groups!");
			}
		}
	}
	
	/**
	 * fill agents into the group
	 * @param x
	 * @param y
	 * @param groupID
	 */
	public void fillAgentsInGroup(int x, int y, int groupID) { //add agents into one group, and create the group
		//make hawks and doves in each group
		Bag members = new Bag();
		for(int i=0; i< nHawks/ nGroup; i++) { //fill some hawks in the group
			Agent a = makeAgent(HAWK);
			members.add(a);
			a.event = schedule.scheduleRepeating(1, 0, a);
		}
		for(int i=0; i< nDoves/ nGroup; i++) { //fill some doves in the group
			Agent a = makeAgent(DOVE);
			members.add(a);
			a.event = schedule.scheduleRepeating(1, 0, a);
		}
		Group g = new Group(this, x, y, groupID, members);
		g.scheduleSelf(this);
		sparseSpace.setObjectLocation(g, x, y);
	}
	/**
	 * making some agents to fill into the groups
	 * @param strategy
	 * @return
	 */
	public Agent makeAgent(int strategy) { //make agents
		int age = random.nextInt(averageLifeSpan);
		id++;
		idString = Integer.toString(id);
		Agent a = new Agent(this, idString, age, strategy);
//		a.resources = random.nextInt(10);
		a.resources = 0;
		return a;
	}

	/*
	 * ***************************************************************************
	 *                               Getters and Setters
	 * ***************************************************************************
	 */
	//Agent setting
	public int getAverageLifeSpan() {
		return averageLifeSpan;
	}

	public void setAverageLifeSpan(int averageLifeSpan) {
		this.averageLifeSpan = averageLifeSpan;
	}

	public int getLifeSpanSD() {
		return lifeSpanSD;
	}

	public void setLifeSpanSD(int lifeSpanSD) {
		this.lifeSpanSD = lifeSpanSD;
	}

	public double getSelfishness_H() {
		return selfishness_H;
	}

	public void setSelfishness_H(double selfishness_H) {
		this.selfishness_H = selfishness_H;
	}

	public double getSelfishness_D() {
		return selfishness_D;
	}

	public void setSelfishness_D(double selfishness_D) {
		this.selfishness_D = selfishness_D;
	}

	public int getStartEnergy() {
		return startEnergy;
	}

	public void setStartEnergy(int startEnergy) {
		this.startEnergy = startEnergy;
	}

	public boolean isInheritance() {
		return inheritance;
	}

	public void setInheritance(boolean inheritance) {
		this.inheritance = inheritance;
	}

	public double getInheritPortion() {
		return inheritPortion;
	}

	public void setInheritPortion(double inheritPortion) {
		this.inheritPortion = inheritPortion;
	}

	public boolean isPreference() {
		return preference;
	}

	public void setPreference(boolean preference) {
		this.preference = preference;
	}

	public double getPreferProb_H() {
		return preferProb_H;
	}

	public void setPreferProb_H(double preferProb_H) {
		this.preferProb_H = preferProb_H;
	}

	public double getPreferProb_D() {
		return preferProb_D;
	}

	public void setPreferProb_D(double preferProb_D) {
		this.preferProb_D = preferProb_D;
	}

	//game setting
	public double getBenefit() {
		return benefit;
	}

	public void setBenefit(double benefit) {
		this.benefit = benefit;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getT() {
		return T;
	}

	public void setT(double t) {
		T = t;
	}

	public double getR() {
		return R;
	}

	public void setR(double r) {
		R = r;
	}

	public double getP() {
		return P;
	}

	public void setP(double p) {
		P = p;
	}

	public double getS() {
		return S;
	}

	public void setS(double s) {
		S = s;
	}

	public double getStagStag() {
		return stagStag;
	}

	public void setStagStag(double stagStag) {
		this.stagStag = stagStag;
	}

	public double getHareStag() {
		return hareStag;
	}

	public void setHareStag(double hareStag) {
		this.hareStag = hareStag;
	}

	public double getHareHare() {
		return hareHare;
	}

	public void setHareHare(double hareHare) {
		this.hareHare = hareHare;
	}

	public double getStagHare() {
		return stagHare;
	}

	public void setStagHare(double stagHare) {
		this.stagHare = stagHare;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int t) {
		switch(t) {
		case 0: gameType = HD; break;
		case 1: gameType = PD; break;
		case 2: gameType = SH; break;
		case 3: gameType = SD; break;
		default: gameType = HD;
		}
	}
	
	public Object domGameType() {
		return new String[] {
			"Hawk-Dove",
			"Prisoner's Dilemma",
			"Stag Hunt",
			"Snowdrift"
		};
	}

	//reproduction settings
//	public double getAveReproductionThreshold() {
//		return aveReproductionThreshold;
//	}
//
//	public void setAveReproductionThreshold(double aveReproductionThreshold) {
//		this.aveReproductionThreshold = aveReproductionThreshold;
//	}

//	public double getReproductionThresholdSD() {
//		return reproductionThresholdSD;
//	}
//
//	public void setReproductionThresholdSD(double reproductionThresholdSD) {
//		this.reproductionThresholdSD = reproductionThresholdSD;
//	}

	public boolean isPuberty() {
		return puberty;
	}

	public void setPuberty(boolean puberty) {
		this.puberty = puberty;
	}

	public int getReproductionAge() {
		return reproductionAge;
	}

	public void setReproductionAge(int reproductionAge) {
		this.reproductionAge = reproductionAge;
	}

	public int getGestation() {
		return gestation;
	}

	public void setGestation(int gestation) {
		this.gestation = gestation;
	}

	public double getReproductionCost() {
		return reproductionCost;
	}

	public void setReproductionCost(double reproductionCost) {
		this.reproductionCost = reproductionCost;
	}

	public double getMutation() {
		return mutation;
	}

	public void setMutation(double mutation) {
		this.mutation = mutation;
	}

	public boolean isDispersal() {
		return dispersal;
	}

	public void setDispersal(boolean dispersal) {
		this.dispersal = dispersal;
	}

	public double getDispersalRate() {
		return dispersalRate;
	}

	public void setDispersalRate(double dispersalRate) {
		this.dispersalRate = dispersalRate;
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}

	//population settings
	public int getPopulationCap() {
		return populationCap;
	}

	public void setPopulationCap(int populationCap) {
		this.populationCap = populationCap;
	}

	public int getnHawks() {
		return nHawks;
	}

	public void setnHawks(int nHawks) {
		this.nHawks = nHawks;
	}

	public int getnDoves() {
		return nDoves;
	}

	public void setnDoves(int nDoves) {
		this.nDoves = nDoves;
	}

	//group settings
	public int getnGroup() {
		return nGroup;
	}

	public void setnGroup(int nGroup) {
		this.nGroup = nGroup;
	}

	public int getFissionC() {
		return fissionC;
	}

	public void setFissionC(int fissionC) {
		this.fissionC = fissionC;
	}

	public int getFusionSize() {
		return fusionSize;
	}

	public void setFusionSize(int fusionSize) {
		this.fusionSize = fusionSize;
	}

	public boolean isGraphic() {
		return graphic;
	}

	public void setGraphic(boolean graphic) {
		this.graphic = graphic;
	}
	

	

	
	
}
