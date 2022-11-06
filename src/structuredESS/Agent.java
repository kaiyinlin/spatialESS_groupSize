package structuredESS;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
import sweep.SimStateSweep;

public class Agent implements Steppable {
	//life history related
	public String idString; //each agent has an ID, in String format
	public int age; //current age
	public double lifeSpan; //the lifeSpan for each agent
	public int averageLifeSpan; //lifespan = averageLifeSpan + random * lifeSPanSD
	public int lifeSpanSD; //the SD of lifespan, usually 10% of averageLifeSpan
	public boolean inheritance;
	public double inheritPortion;
	
	//game related
	public int strategy; //game strategy for agents
	public int rounds; //competition rounds
	public long lastPlayed = -1; //update the steps after competition
	public long currentStep = -1; //get the current step
	public double mutation; //strategy mutation rate when reproducing
	public double payoff = 0; //payoff for each competition
	public double resources = 0; //accumulated payoff for each agent
	//selfishness is a probability betwenn 0-1; 0 represents a selfless person while 1 represents a selfish person
	public double selfishness_H; //hawks set up the portion of payoff for sharing; winner makes the decision
	public double selfishness_D; //doves set up the portion of payoff for sharing; make the decision randomly
	
	//reproduction related
	public double reproductionThreshold; //agents need to reach a threshold for reproduction
	public double aveReproductionThreshold; //average reproduction threshold for population
	public double reproductionThresholdSD; //each agent have slight different threshold for reproduction
	public int reproductionAge; //the age of sexual maturity (APPLY FOR REPRODUCTION PROJECT)
	public int gestation; //gestation; (REPRODUCTION PROJECT)
	public int gestationCounts; //reproduce only at gestation; (REPRODUCTION PROJECT)
	public int delaySteps; //record how long one agent reach the reproduction threshold
	public Asexual reproductionMethod;
	public int reproductionNum = 0; //how many times a parent reproduced
	public int offspringNum = 0; //baby counts for parents
	public String offspringNumString; //transfer the baby number to String format
	public String offspringIDString; //assign an ID to offspring
	
	//get delayed reproduction data
	ArrayList<String> hawkDelayList = new ArrayList<String>();
	ArrayList<String> doveDelayList = new ArrayList<String>();
	ArrayList<String> hawkResList = new ArrayList<String>();
	ArrayList<String> doveResList = new ArrayList<String>();
	
	
	//connect to other class
	public Stoppable event;
	public Group group;
	public GroupProbe probe;
	
	//test
//	ArrayList<String> resList = new ArrayList<String>();
//	ArrayList<String> payoffList = new ArrayList<String>();
	
	//constructor
	public Agent(SimStateSweep state, String idString, int age, int strategy) {
		super();
		SimEnvironment sim = (SimEnvironment)state;
		this.probe = sim.probe;
		this.idString = idString;
		this.age = age;
		//lifeSpan
		this.averageLifeSpan = sim.averageLifeSpan;
		this.lifeSpanSD = sim.lifeSpanSD;
		lifeSpan = (int)(averageLifeSpan + state.random.nextGaussian()*lifeSpanSD);
		//reproduction
		this.inheritance = sim.inheritance;
		this.inheritPortion = sim.inheritPortion;
		this.aveReproductionThreshold = sim.reproductionCost + sim.difference;
//		this.reproductionThresholdSD = sim.reproductionThresholdSD;
		reproductionThreshold = aveReproductionThreshold/* + state.random.nextGaussian() * reproductionThresholdSD*/;
		reproductionAge = sim.reproductionAge;
		gestation = sim.gestation;
		gestationCounts = 1;
		delaySteps = 1;
		reproductionMethod = new Asexual();
		offspringNum = 0;
		reproductionNum = 0;
		lastPlayed = -1;
		this.mutation = sim.mutation;
		//game related
		this.strategy = strategy;
		this.resources = sim.startEnergy;
		if(this.strategy == SimEnvironment.HAWK) selfishness_H = sim.selfishness_H;
		if(this.strategy == SimEnvironment.DOVE) selfishness_D = sim.selfishness_D;
//		this.resList = new ArrayList<String>();
//		this.payoffList = new ArrayList<String>();
		
	}

	/*
	 * ***************************************************************************
	 *                               STEP
	 * ***************************************************************************
	 */
	@Override
	public void step(SimState state) {
		SimEnvironment sim = (SimEnvironment)state;
		currentStep = state.schedule.getSteps();
		//Agent die only when reaching lifespan
		
//		System.out.println("step1: check lifespan");
		if(age > lifeSpan) {
			die();
			return;
		}
//		System.out.println("step2: migration");
		//migration
//		individualMigrate((SimEnvironment)state);
		
//		System.out.println("step3: play games");
		//play the game
		switch(sim.gameType) {
		case 0:
			hawkDove((SimEnvironment)state);
			break;
		case 1:
			prisonerDilemma((SimEnvironment)state);
			break;
		case 2:
			stagHunt((SimEnvironment)state);
			break;
		case 3:
			snowdrift((SimEnvironment)state);	
		}
//		System.out.println("step4: aged");
		//age calculator
		age ++;
		
//		System.out.println("step5: reproduction");
		//reproduction: agents can reproduce when having enough resources
		if(sim.puberty) { //reproduce after puberty
			if(age >= reproductionAge) {
				reproduce((SimEnvironment)state, reproductionMethod);
			}
		}
		else { //reproduce after birth
			reproduce((SimEnvironment)state, reproductionMethod);
		}
//		System.out.println("id = " + idString + "   delaySteps = " + delaySteps + "   resourceList = " + resList);
//		System.out.println("payoffList = " + payoffList);
//		System.out.println("   ");
	}// end of step
	
	/*
	 * ***************************************************************************
	 *                             Biological Properties
	 * ***************************************************************************
	 */
	
	/**
	 * Death: Remove agents from the simulation, simulated death
	 * and release the agent from the group
	 */
	public void die() {
		event.stop();
		group.removeMembers(this);
		group = null; //release this agent from the group
	}
	
	/*
	 * Group setting: identify an agent into a group
	 */
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	/*
	 * ***************************************************************************
	 *                              Reproduction
	 * ***************************************************************************
	 */
	
	public void reproduce(SimStateSweep state, Reproducible reproductionMethod) {
		if(canReproduce(state)) {
			SimEnvironment sim = (SimEnvironment)state;
			//if an agent can reproduce, reproduce with a reproduction cost no matter the newborn survive or not
			this.resources -= sim.reproductionCost;
			reproductionNum ++;
//			this.resources -= 0.1 * resources;
			//check if there is space for newborn; if not, reproduction failure
			Bag currentAgents = getAllAgents((SimEnvironment)state);
			if(currentAgents.numObjs > sim.populationCap) {
				return;
			}
			//If there is space, reproduce a new born!
			Agent offspring = reproductionMethod.replicate(state);
			if(inheritance) {
				offspring.resources += inheritPortion * this.resources;
			}	
			if(offspring != null) {
				offspring.event = state.schedule.scheduleRepeating(offspring);
			}
		}
	}
	
	public interface Reproducible{
		public Agent replicate(SimStateSweep state);
	}
	
	public class Asexual implements Reproducible {
		public Agent replicate(SimStateSweep state) {
			SimEnvironment sim = (SimEnvironment)state;
			offspringNum ++; //record how many offspring the agents have
			offspringNumString = Integer.toString(offspringNum); //set the name (number) for offspring
			offspringIDString = idString + "." + offspringNumString;
			Bag currentGroups = state.sparseSpace.getAllObjects();
			Agent offspring = new Agent(state, offspringIDString, 0,strategy); //newborn!
			if(sim.dispersal && state.random.nextBoolean(sim.dispersalRate)) { //dispersal can be applied with a certain probability of dispersal
				Group dispersalGroup = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)]; //choose another group to migrate
				while(group.equals(dispersalGroup)) { //keep looking for the other group for migration 
					dispersalGroup = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)];
				}
				dispersalGroup.addMembers(offspring); //move to the new group
			}
			else { //if there is no dispersal applied, add this offspring to parent's group
				group.addMembers(offspring);
			}
			if(state.random.nextBoolean(mutation)) { //offspring has a certain rate for strategy mutation
				offspring.strategy = flipStrategy(); //strategy mutation occurs when reproduction occurs
			}
			return offspring;
		}
	}
	
	/**
	 * Check if the agent has enough resources for reproduction
	 * The only criteria is the potential parents need to reach a certain reproduction threshold
	 * @param state
	 * @return
	 */
	public boolean canReproduce(SimStateSweep state) {
		if(resources > reproductionThreshold && gestationCounts == gestation) {		
			delayRecord(state);
			delaySteps = 1;
			gestationCounts = 1;
			return true;
		}
		else {
			if(gestationCounts >= gestation) { //resources doesn't meet the requirement but meet the gestation requirement
				gestationCounts = 1;
			} 	
			else { //both don't meet the requirements or only gestation deoesn't meet the criteria
				gestationCounts ++;
			}  	
			delaySteps ++;
			return false;
		}
	}
	
	public void delayRecord(SimStateSweep state) {
		if(strategy == SimEnvironment.HAWK) {
			if(state.schedule.getSteps() > 50000 && state.schedule.getSteps() <= 200000) {
				hawkDelayList.add(Integer.toString(delaySteps));
				hawkResList.add(Double.toString(resources));
				probe.setHawkDelaySteps(hawkDelayList);
				probe.setHawkDelayRes(hawkResList);					
			}	
		}
		else if(strategy == SimEnvironment.DOVE) {
			if(state.schedule.getSteps() > 50000 && state.schedule.getSteps() <= 200000) {
				doveDelayList.add(Integer.toString(delaySteps));
				doveResList.add(Double.toString(resources));
				probe.setDoveDelaySteps(doveDelayList);
				probe.setDoveDelayRes(doveResList);
			}
		}
	}
	
	/**
	 * Strategy mutation occurs when reproduction occurs
	 * @return
	 */
	public int flipStrategy() {
		if(this.strategy == SimEnvironment.HAWK) {
			this.strategy = SimEnvironment.DOVE;
		}
		else {
			this.strategy = SimEnvironment.HAWK;
		}
		return strategy;
	}
	
	/**
	 * Get all agents in the population
	 * @param state
	 * @return
	 */
	public Bag getAllAgents(SimEnvironment state) { //checking the agents before reproduction
		Bag groups = state.sparseSpace.getAllObjects();
		Bag allAgents = new Bag();
		for(int i=0; i<groups.numObjs; i++) {
			Group g = (Group)groups.objs[i];
			for(int j=0; j<g.members.numObjs; j++) {
				allAgents.add(g.members.objs[j]);
			}
		}
		return allAgents;
	}
	
	/*
	 * ***************************************************************************
	 *                              Migration(Adult)
	 * ***************************************************************************
	 */
	public void individualMigrate(SimEnvironment state) {
		double individualMigrate = 1- Math.exp(Math.log(1-group.groupMigrationRate)/this.averageLifeSpan);
		System.out.println("individualMigrate = " + individualMigrate);
		Bag currentGroups = state.sparseSpace.getAllObjects();
		if(state.random.nextBoolean(individualMigrate)) {
			if(currentGroups.numObjs >1) {
				Group otherGroup = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)];
				while(group.equals(otherGroup)) {
					otherGroup = (Group)currentGroups.objs[state.random.nextInt(currentGroups.numObjs)];
				}
				group.removeMembers(this);
				if(otherGroup.groupSize < otherGroup.fissionSize) { //migrate to new group
					otherGroup.addMembers(this);
				}	
			}
		}
	}
	
	/*
	 * ***************************************************************************
	 *                              Game Related
	 * ***************************************************************************
	 */
	/**
	 * Agents play a hawk-dove game and accumulated the payoffs as resources
	 * @param state
	 */
	public void hawkDove(SimEnvironment state) {
		Agent opponent = findOpponent(state);
		if(opponent != null && this.lastPlayed != currentStep /*&& this.age > 0 && opponent.age > 0*/) { //start the game, agent should be larger than age 0
			//two hawks meet
			if(strategy == SimEnvironment.HAWK && opponent.strategy == SimEnvironment.HAWK) {
				if(state.random.nextBoolean()) { //this agent wins
					this.payoff = this.payoffPortion()* state.benefit - (state.cost/2);
					opponent.payoff = (1- this.payoffPortion())*state.benefit -(state.cost/2);
				}
				else { //or this agent loses
					this.payoff = (1- opponent.payoffPortion())*state.benefit -(state.cost/2);
					opponent.payoff = opponent.payoffPortion()*state.benefit - (state.cost/2);
				}
			}
			//a hawk meets a dove
			else if(strategy == SimEnvironment.HAWK && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.benefit;
				opponent.payoff = 0;
			}
			//a dove meets a hawk
			else if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.HAWK) {
				this.payoff = 0;
				opponent.payoff = state.benefit;
			}
			//two doves meet
			else { //this agent decide the proportion for sharing (random agent)
				if(state.random.nextBoolean()) {
					this.payoff = this.payoffPortion() * state.benefit;
					opponent.payoff = (1- this.payoffPortion()) * state.benefit;	
				}
				else { //opponent decides the proportion
					this.payoff = opponent.payoffPortion() * state.benefit;
					opponent.payoff = (1- opponent.payoffPortion()) * state.benefit;
				}
			}
			//update agent's resources
			resources += payoff;
			opponent.resources += opponent.payoff;
			
//			this.resList.add(Double.toString(resources)+"|"+currentStep+"|this");
//			opponent.resList.add(Double.toString(opponent.resources)+"|"+currentStep+"|opp");
//			System.out.println("this.resList = " + this.resList);
//			System.out.println("this.resources = " + this.resources);
//			this.payoffList.add(Double.toString(payoff));
//			opponent.payoffList.add(Double.toString(opponent.payoff));
//			System.out.println("this.id = " + idString + "    this.strategy = " + strategy + "   opponent.id = " + opponent.idString);
			//count as played
			lastPlayed = currentStep;
			opponent.lastPlayed = currentStep;
		}
	}
	
	/**
	 * prisoner's dilemma: Suppose the cooperators keep silent and the defectors betray their partners.
	 * If both players cooperate, they both receive the reward R for cooperating. 
	 * If both players defect, they both receive the punishment payoff P. 
	 * If one agent defects while the other cooperates, then the defector receives the temptation payoff T, while the cooperator receives the "sucker's" payoff, S. 
	 * Similarly, if the cooperator cooperates while the defector defects, then cooperator receives the sucker's payoff S, while the defector receives the temptation payoff T.
	 * The generalized form is T>R>P>S
	 * @param state
	 */
	public void prisonerDilemma(SimEnvironment state) {
		Agent opponent = findOpponent(state);
		if(opponent != null && this.lastPlayed != currentStep && this.age > 0 && opponent.age > 0) { //start the game, agent should be larger than age 0
			//two cooperators meet, both receive the reward R
			if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.R;
				opponent.payoff = state.R;
			}
			//a defector meets a cooperator, the defector receives T, the cooperator receives S
			else if(strategy == SimEnvironment.HAWK && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.T;
				opponent.payoff = state.S;
			}
			//a cooperator meets a defector, the cooperator receives S, the defector receives T
			else if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.HAWK) {
				this.payoff = state.S;
				opponent.payoff = state.T;
			}
			//two defectors meet, both receive the punishment payoff P
			else {
				this.payoff = state.P;
				opponent.payoff = state.P;
			}
			//update agent's resources
			resources += payoff;
			opponent.resources += opponent.payoff;
			//count as played
			lastPlayed = currentStep;
			opponent.lastPlayed = currentStep;
		}
	}
	
	/**
	 * A generic stag hunt illustrate a payoff matrix a > b >= d > c
	 * @param state
	 */
	public void stagHunt(SimEnvironment state) {
		Agent opponent = findOpponent(state);
		if(opponent != null && this.lastPlayed != currentStep && this.age > 0 && opponent.age > 0) { //start the game, agent should be larger than age 0
			//two cooperators meet to hunt a stag, both get payoff stagStag
			if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.stagStag;
				opponent.payoff = state.stagStag;
			}
			//a defector meets a cooperator; defector get payoff hareStag and cooperator gets payoff stagHare
			else if(strategy == SimEnvironment.HAWK && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.hareStag;
				opponent.payoff = state.stagHare;
			}
			//a cooperator meets a defector; cooperator gets payoff stagHare and defector gets payoff hareStag
			else if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.HAWK) {
				this.payoff = state.stagHare;
				opponent.payoff = state.hareStag;
			}
			//two defectors meet; both gain the payoff hareHare
			else {
				this.payoff = state.hareHare;
				opponent.payoff = state.hareHare;
			}
			//update agent's resources
			resources += payoff;
			opponent.resources += opponent.payoff;
			//count as played
			lastPlayed = currentStep;
			opponent.lastPlayed = currentStep;
		}
	}
	
	/**
	 * More intuitively, snowdrift game assume that the benefit of getting home is b 
	 * while the total labour costs to remove the snowdrift are c, with b > c > 0.
	 * For high costs, 2 b > c > b > 0, these payoffs recover the Prisoner's Dilemma.
	 * @param state
	 */
	public void snowdrift(SimEnvironment state) { //in this game, benefit should be larger than cost
		Agent opponent = findOpponent(state);
		if(opponent != null && this.lastPlayed != currentStep && this.age > 0 && opponent.age > 0) { //start the game, agent should be larger than age 0
			//two cooperators meet, both share the cost
			if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.benefit - (state.cost/2); //two people remove the snowdrift together, share the load
				opponent.payoff = state.benefit - (state.cost/2);
			}
			//a defector meets a cooperator; the defector pay the cost and both gain the benefit
			else if(strategy == SimEnvironment.HAWK && opponent.strategy == SimEnvironment.DOVE) {
				this.payoff = state.benefit; //defector get benefit without effort
				opponent.payoff = state.benefit - state.cost; //the cooperator provide the effort only.
			}
			//a cooperator meets a defector; the cooperator pay the cost and both gain the benefit
			else if(strategy == SimEnvironment.DOVE && opponent.strategy == SimEnvironment.HAWK) {
				this.payoff = state.benefit - state.cost;
				opponent.payoff = state.benefit; //free rider!!
			}
			//two defectors meet; no one can get home
			else {
				this.payoff = 0; //no one get the benefit
				opponent.payoff = 0;
			}
			//update agent's resources
			resources += payoff;
			opponent.resources += opponent.payoff;
			//count as played
			lastPlayed = currentStep;
			opponent.lastPlayed = currentStep;
		}
	}

		
	/**
	 * Agents find an opponent before playing the game.
	 * Doves seek another seek when preference is performed. However, hawks have no preference on partner choosing
	 * @param state
	 * @return
	 */
	public Agent findOpponent(SimEnvironment state) {
		Bag notPlayed = new Bag();
		Bag notPlayedDoves = new Bag();
		if(group == null) {
			return null;
		}
		if(group.members.numObjs > 0) { //find the opponent only when there are agents in the group
			for(int i=0; i<group.members.numObjs; i++) { //find the agents haven't played
				Agent p = (Agent)group.members.objs[i];
				if(p.lastPlayed != currentStep && !(this.equals(p))) {
					notPlayed.add(p);
				}
			}	
		}
		if(notPlayed.numObjs > 0) {
			if(state.preference && state.random.nextBoolean(state.preferProb_D)) {
				if(this.strategy == SimEnvironment.DOVE) { //doves seek another doves
					for(int i=0; i< notPlayed.numObjs; i++) {
						Agent q = (Agent)notPlayed.objs[i];
						if(q.strategy == SimEnvironment.DOVE) {
							notPlayedDoves.add(q);
						}
					}
					if(notPlayedDoves.numObjs > 0) {
						Agent d = (Agent)notPlayedDoves.objs[state.random.nextInt(notPlayedDoves.numObjs)];
						return d;
					}
					else {
						return null;
					}
				}
				else { //hawks have no preference, randomly choose a partner
					Agent a = (Agent)notPlayed.objs[state.random.nextInt(notPlayed.numObjs)];
					return a;
				}
			} //end of if (preference)
			else { //no preference
				Agent a = (Agent)notPlayed.objs[state.random.nextInt(notPlayed.numObjs)];
				return a;
			}	
		}
		else {
			return null;
		}
	}
	
	/**
	 * Some Agents share resources equally while some don't. Payoff portion was applied to see 
	 * how the variance of payoff can affect social structure.
	 * @return
	 */
	public double payoffPortion() {
		double rangeMin = 0.5; //the minimum portion for opponents is 50% (share equally)
		double rangeMax = 1.0; //The maximum portion for opponents is 100% (winner get all)
		double payoffPortion = 0;
		if(this.strategy == SimEnvironment.HAWK) {
			payoffPortion = rangeMin + (rangeMax - rangeMin) * selfishness_H;
		}
		else {
			payoffPortion = rangeMin + (rangeMax - rangeMin) * selfishness_D;
		}
		
		return payoffPortion;
	}
	
	

	
	
}
