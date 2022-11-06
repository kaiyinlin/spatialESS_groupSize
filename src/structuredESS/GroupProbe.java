package structuredESS;

import java.util.ArrayList;

import observer.Probe;
import sweep.SimStateSweep;

public class GroupProbe extends Probe {
	//population probe
	public double popHawks;
	public double popDoves;
	public double popSize; 
	public double popHawkFrequency;
	public double popHawkOffsprings;
	public double popDoveOffsprings;
	public double hawkCounts; //head counts for counting offsprings of hawks
	public double doveCounts; //head counts for counting offsprings of doves
	public double sumPopRes;//overall resource
	public double sum2PopRes;
	public double popResMean;
	public double popHawkResMean; //hawk resource mean
	public double sumPopHawkRes;
	public double sum2PopHawkRes;
	public double popDoveResMean;//dove resource mean
	public double sumPopDoveRes;
	public double sum2PopDoveRes;
	public double popHawkResSD; //resource SD
	public double popDoveResSD;
	public double popResSD;
	
	public ArrayList<String> hawkDelaySteps;
	public ArrayList<String> doveDelaySteps;
	public ArrayList<String> hawkDelayRes;
	public ArrayList<String> doveDelayRes;

	public ArrayList<String> getHawkDelayRes() {
		return hawkDelayRes;
	}

	public void setHawkDelayRes(ArrayList<String> hawkDelayRes) {
		this.hawkDelayRes = hawkDelayRes;
	}

	public ArrayList<String> getDoveDelayRes() {
		return doveDelayRes;
	}

	public void setDoveDelayRes(ArrayList<String> doveDelayRes) {
		this.doveDelayRes = doveDelayRes;
	}

	public ArrayList<String> getHawkDelaySteps() {
		return hawkDelaySteps;
	}

	public void setHawkDelaySteps(ArrayList<String> hawkDelaySteps) {
		this.hawkDelaySteps = hawkDelaySteps;
	}

	public ArrayList<String> getDoveDelaySteps() {
		return doveDelaySteps;
	}

	public void setDoveDelaySteps(ArrayList<String> doveDelaySteps) {
		this.doveDelaySteps = doveDelaySteps;
	}

	//group probe
	public double groupHawks;
	public double groupDoves;
	public double groupHawkFrequency;
	public int groupSize;
	public int grade1;
	public int grade2;
	public int grade3;
	public int grade4;
	public int grade5;
	public double grade1Res;
	public double grade2Res;
	public double grade3Res;
	public double grade4Res;
	public double grade5Res;
	
	//ANOVA related
	public ArrayList<Double> groupSS;
	public ArrayList<Double> groupN;
	public double ssTotal = 0;
	public double ssBetween = 0;
	public double ssWithin = 0;
	public double msBetween = 0;
	public double msWithin = 0;
	public double fRatio = 0;
	
	//fission group probe
	public double fissionHF;
	public double fissionHawkResMean;
	public double fissionDoveResMean;
	public double fissionResMean;
	public double fissionHawkResSD;
	public double fissionDoveResSD;
	public double fissionResSD;
	public double fissionCounts;
	
	//non-fission group probe
	public double nonFissionHF;
	public double nonFissionHawkResMean;
	public double nonFissionDoveResMean;
	public double nonFissionResMean;
	public double nonFissionHawkResSD;
	public double nonFissionDoveResSD;
	public double nonFissionResSD;
	public double nonFissionCounts;

	/*
	 * Constructor
	 */
	public GroupProbe(SimStateSweep state, int burnIn, double min, double max, double interval) {
		super(state, burnIn, min, max, interval);
		groupSS = new ArrayList<Double>();
		groupN = new ArrayList<Double>();
	}
	
	/*
	 * ***********************************************************************
	 *                         Population Probes
	 * ***********************************************************************
	 */
	public double getPopHawks() {
		return popHawks;
	}

	public void setPopHawks(double groupHawks) {
		this.popHawks += groupHawks;
	}

	public double getPopDoves() {
		return popDoves;
	}

	public void setPopDoves(double groupDoves) {
		this.popDoves += groupDoves;
	}

	public double getPopSize() {
		return popSize;
	}

	public void setPopSize(double groupSize) {
		this.popSize += groupSize;
	}

	public double getPopHawkFrequency() {
		if(popHawks + popDoves > 0) {
			popHawkFrequency = popHawks/ (popHawks + popDoves);
		}
		return popHawkFrequency;
	}
	
	public double getPopHawkOffsprings() {
		return mean(popHawkOffsprings, hawkCounts);
	}

	public void setPopHawkOffsprings(double popHawkOffsprings, double hawkCounts) {
		this.popHawkOffsprings += popHawkOffsprings;
		this.hawkCounts += hawkCounts;
	}

	public double getPopDoveOffsprings() {
		return mean(popDoveOffsprings, doveCounts);
	}

	public void setPopDoveOffsprings(double popDoveOffsprings, double doveCounts) {
		this.popDoveOffsprings += popDoveOffsprings;
		this.doveCounts += doveCounts;
	}

	public double getPopResMean() {
		this.popResMean = mean(sumPopRes, popSize);
		return popResMean;
	}

	public void setPopResMean(double sumGroupRes, double sum2GroupRes) {
		sumPopRes += sumGroupRes;
		sum2PopRes += sum2GroupRes;
	}

	public double getPopHawkResMean() {
		this.popHawkResMean = mean(sumPopHawkRes, popHawks);
		return popHawkResMean;
	}

	public void setPopHawkResMean(double sumGroupHawkRes, double sum2GroupHawkRes) {
		sumPopHawkRes += sumGroupHawkRes;
		sum2PopHawkRes += sum2GroupHawkRes;
	}

	public double getPopDoveResMean() {
		this.popDoveResMean = mean(sumPopDoveRes, popDoves);
		return popDoveResMean;
	}

	public void setPopDoveResMean(double sumGroupDoveRes, double sum2GroupDoveRes) {
		sumPopDoveRes += sumGroupDoveRes;
		sum2PopDoveRes += sum2GroupDoveRes;
	}

	public double getPopHawkResSD() {
		this.popHawkResSD = sampleSD(sumPopHawkRes, sum2PopHawkRes, popHawks);
		return popHawkResSD;
	}

	public double getPopDoveResSD() {
		this.popDoveResSD = sampleSD(sumPopDoveRes, sum2PopDoveRes, popDoves);
		return popDoveResSD;
	}

	public double getPopResSD() {
		this.popResSD = sampleSD(sumPopRes, sum2PopRes, popSize);
		return popResSD;
	}

	/*
	 * ***********************************************************************
	 *                            Group Probes
	 * ***********************************************************************
	 */
	public double getGroupHawks() {
		return groupHawks;
	}

	public void setGroupHawks(double groupHawks) {
		this.groupHawks += groupHawks;
	}

	public double getGroupDoves() {
		return groupDoves;
	}

	public void setGroupDoves(double groupDoves) {
		this.groupDoves += groupDoves;
	}

	public double getGroupHawkFrequency() {
		if(groupHawks + groupDoves > 0) {
			groupHawkFrequency = groupHawks / (groupHawks + groupDoves);
		}
		return groupHawkFrequency;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	public int getGrade1() {
		return grade1;
	}

	public void setGrade1(int grade1) {
		this.grade1 += grade1;
	}

	public int getGrade2() {
		return grade2;
	}

	public void setGrade2(int grade2) {
		this.grade2 += grade2;
	}

	public int getGrade3() {
		return grade3;
	}

	public void setGrade3(int grade3) {
		this.grade3 += grade3;
	}

	public int getGrade4() {
		return grade4;
	}

	public void setGrade4(int grade4) {
		this.grade4 += grade4;
	}

	public int getGrade5() {
		return grade5;
	}

	public void setGrade5(int grade5) {
		this.grade5 += grade5;
	}

	public double getGrade1Res() {
		return mean(grade1Res, grade1);
	}

	public void setGrade1Res(double grade1Res) {
		this.grade1Res += grade1Res;
	}

	public double getGrade2Res() {
		return mean(grade2Res, grade2);
	}

	public void setGrade2Res(double grade2Res) {
		this.grade2Res += grade2Res;
	}

	public double getGrade3Res() {
		return mean(grade3Res,grade3);
	}

	public void setGrade3Res(double grade3Res) {
		this.grade3Res += grade3Res;
	}

	public double getGrade4Res() {
		return mean(grade4Res, grade4);
	}

	public void setGrade4Res(double grade4Res) {
		this.grade4Res += grade4Res;
	}

	public double getGrade5Res() {
		return mean(grade5Res, grade5);
	}

	public void setGrade5Res(double grade5Res) {
		this.grade5Res += grade5Res;
	}

	/*
	 * ***********************************************************************
	 *                          Fission Group Probes
	 * ***********************************************************************
	 */
	public double getFissionCounts() {
		return fissionCounts;
	}

	public void setFissionCounts(double fissionCounts) {
		this.fissionCounts += fissionCounts;
	}

	public double getFissionHF() {
		return mean(fissionHF, fissionCounts);
	}

	public void setFissionHF(double fissionHF) {
		this.fissionHF += fissionHF;
		
	}

	public double getFissionHawkResMean() {
		return mean(fissionHawkResMean, fissionCounts);
	}

	public void setFissionHawkResMean(double fissionHawkResMean) {
		this.fissionHawkResMean += fissionHawkResMean;
	}

	public double getFissionDoveResMean() {
		return mean(fissionDoveResMean, fissionCounts);
	}

	public void setFissionDoveResMean(double fissionDoveResMean) {
		this.fissionDoveResMean += fissionDoveResMean;
	}

	public double getFissionResMean() {
		return mean(fissionResMean, fissionCounts);
	}

	public void setFissionResMean(double fissionResMean) {
		this.fissionResMean += fissionResMean;
	}

	public double getFissionHawkResSD() {
		return mean(fissionHawkResSD, fissionCounts);
	}

	public void setFissionHawkResSD(double fissionHawkResSD) {
		this.fissionHawkResSD += fissionHawkResSD;
	}

	public double getFissionDoveResSD() {
		return mean(fissionDoveResSD, fissionCounts);
	}

	public void setFissionDoveResSD(double fissionDoveResSD) {
		this.fissionDoveResSD += fissionDoveResSD;
	}
	
	public double getFissionResSD() {
		return mean(fissionResSD, fissionCounts);
	}

	public void setFissionResSD(double fissionResSD) {
		this.fissionResSD += fissionResSD;
	}
	
	/*
	 * ***********************************************************************
	 *                          Fission Group Probes
	 * ***********************************************************************
	 */
	public double getNonFissionHF() {
		return mean(nonFissionHF, nonFissionCounts);
	}

	public void setNonFissionHF(double nonFissionHF) {
		this.nonFissionHF += nonFissionHF;
	}

	public double getNonFissionHawkResMean() {
		return mean(nonFissionHawkResMean, nonFissionCounts);
	}

	public void setNonFissionHawkResMean(double nonFissionHawkResMean) {
		this.nonFissionHawkResMean += nonFissionHawkResMean;
	}

	public double getNonFissionDoveResMean() {
		return mean(nonFissionDoveResMean, nonFissionCounts);
	}

	public void setNonFissionDoveResMean(double nonFissionDoveResMean) {
		this.nonFissionDoveResMean += nonFissionDoveResMean;
	}

	public double getNonFissionResMean() {
		return mean(nonFissionResMean, nonFissionCounts);
	}

	public void setNonFissionResMean(double nonFissionResMean) {
		this.nonFissionResMean += nonFissionResMean;
	}

	public double getNonFissionHawkResSD() {
		return mean(nonFissionHawkResSD, nonFissionCounts);
	}

	public void setNonFissionHawkResSD(double nonFissionHawkResSD) {
		this.nonFissionHawkResSD += nonFissionHawkResSD;
	}

	public double getNonFissionDoveResSD() {
		return mean(nonFissionDoveResSD, nonFissionCounts);
	}

	public void setNonFissionDoveResSD(double nonFissionDoveResSD) {
		this.nonFissionDoveResSD += nonFissionDoveResSD;
	}

	public double getNonFissionResSD() {
		return mean(nonFissionResSD, nonFissionCounts);
	}

	public void setNonFissionResSD(double nonFissionResSD) {
		this.nonFissionResSD += nonFissionResSD;
	}

	public double getNonFissionCounts() {
		return nonFissionCounts;
	}

	public void setNonFissionCounts(double nonFissionCounts) {
		this.nonFissionCounts += nonFissionCounts;
	}
	
	/*
	 * ***********************************************************************
	 *                          ANOVA Related
	 * ***********************************************************************
	 */
	public void ANOVA() {
		double n = (double)getPopHawks()+ (double)getPopDoves();
		if(n>1) {
			ssTotal = ss(sumPopRes, sum2PopRes, popSize); //SS_total
			for(int i=0; i<groupSS.size(); i++) {
				ssWithin += groupSS.get(i); //SS_within
			}
			ssBetween = ssTotal - ssWithin;
			//MSb: Mean Square_between
			double dfWithin = n - groupSS.size(); //dfWithin = n-K
			if(dfWithin > 0)
				msWithin = ssWithin / dfWithin ;
			else
				msWithin = 0;
			double dfBetween = groupSS.size()-1;
			if(dfBetween >0) //dfBetween = K-1
				msBetween = ssBetween / dfBetween;
			else
				msBetween = 0;
			if(msWithin != 0)
				fRatio = msBetween / msWithin;
			else
				System.out.println("not enough agents");
		}	
	}
	
	public void getGroupSS(double sumGroupRes, double sum2GroupRes, double N) {
		//get GroupSS, N refers to the groupSize
		if(N == 0)
			return;//if there is no one in the group, nothing to do
		if(N == 1){
			groupSS.add((double)0);
			return; //if there is only one agent in the group, no variance
		}
		final double ss = ss(sumGroupRes, sum2GroupRes, N);
		groupSS.add(ss);
		groupN.add(N);
	}
	
	/*
	 * ***********************************************************************
	 *                               Reset
	 * ***********************************************************************
	 */
	public void reSet() {
		//reset group data
		groupHawks = 0; 
		groupDoves = 0;
		groupHawkFrequency = 0;
		//reset population data
		popHawks = 0;
		popDoves = 0;
		popSize = 0;
		popHawkFrequency = 0;
		popHawkOffsprings = 0;
		popDoveOffsprings = 0;
		hawkCounts = 0;
		doveCounts = 0;
		popResMean = 0;
		popResSD = 0;
		sumPopRes = 0;
		sum2PopRes = 0;
		popHawkResMean = 0;
		popDoveResMean = 0;
		sumPopHawkRes = 0;
		sum2PopHawkRes = 0;
		sumPopDoveRes = 0;
		sum2PopDoveRes = 0;
		//reset ANOVA related
		ssTotal = 0;
		ssBetween = 0;
		ssWithin = 0;
		msBetween = 0;
		msWithin = 0;
		fRatio = 0;
		//clear ArrayList
		groupSS.clear();
		groupN.clear();		
		}
	
	public void reSet_fissionNonFission() {
		//reset fission data
		fissionCounts = 0;
		fissionHF = 0;
		fissionHawkResSD = 0;
		fissionDoveResSD = 0;
		fissionResSD = 0;
		fissionHawkResMean = 0;
		fissionDoveResMean = 0;
		fissionResMean = 0;
		//reset non-fission data
		nonFissionCounts = 0;
		nonFissionHF = 0;
		nonFissionHawkResSD = 0;
		nonFissionDoveResSD = 0;
		nonFissionResSD = 0;
		nonFissionHawkResMean = 0;
		nonFissionDoveResMean = 0;
		nonFissionResMean = 0;
		
	}
	
}
