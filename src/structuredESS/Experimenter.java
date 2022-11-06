package structuredESS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import observer.Observer;
import sim.engine.SimState;
import sim.util.Bag;
import sweep.ParameterSweeper;
import sweep.SimStateSweep;

public class Experimenter extends Observer {
	public GroupProbe probe = null;
	public Group group;
	
	public double popHawks;
	public double popDoves;
	
	//output csv
	public DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm"); //define a data format
	public Date date = new Date();
	public String fileName_H = "HAWK" + dateFormat.format(date); //use the date as a file name
	public String fileName_D = "DOVE" + dateFormat.format(date);
	public File hawkCsvFile = new File("/Users/kaiyinlin/Desktop/" + fileName_H + ".csv");
	public File doveCsvFile = new File("/USers/kaiyinlin/Desktop/" + fileName_D + ".csv");
	public boolean headerWritten_Hawk = false;
	public boolean headerWritten_Dove = false;
	public String[] header = {"Time", "Steps", "Res"};

	/*
	 * Constructor
	 */
	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
		probe = ((SimEnvironment)state).probe;
		
	}

	/*
	 * ***************************************************************************
	 *                               STEP
	 * ***************************************************************************
	 */
	public void step(SimState state) {
		super.step(state);
		if(getdata) {
			 probe.ANOVA();
			 System.out.println("hawkProportion = " + probe.getPopHawkFrequency()+"   hawkRes = " + probe.getPopHawkResMean() + "    doveRes= " + probe.getPopDoveResMean());
			 nextInterval();
//			 printDataInConsole();
			 probe.reSet();
			 probe.reSet_fissionNonFission(); //Fission/ Non-fission data is only collected in a period of time because there are not many fission-fusion dynamics each step
		}
		else {
//			probe.ANOVA(); 
			System.out.println("hawkProportion = " + probe.getPopHawkFrequency()+"   hawkRes = " + probe.getPopHawkResMean() + "    doveRess= " + probe.getPopDoveResMean());
//			printDataInConsole();
//			try {
//				if(probe.getHawkDelaySteps() != null && state.schedule.getSteps() <= 200000)
//					outputCSV_Hawk();
//				if(probe.getDoveDelaySteps() != null && state.schedule.getSteps() <= 200000)
//					outputCSV_Dove();
//			}
//			catch(IOException e){
//				e.printStackTrace();
//			}
			probe.reSet();
		}
	}
	
	/*
	 * ***************************************************************************
	 *                              Data Collection
	 * ***************************************************************************
	 */
	/**
	 * Collect population data
	 * @return
	 */
	public boolean nextInterval() {
		data.add(agents.numObjs);
		data.add(probe.getPopHawks());
		data.add(probe.getPopDoves());
		data.add(probe.getPopHawkFrequency());
		data.add(probe.getPopHawkOffsprings());
		data.add(probe.getPopDoveOffsprings());
		data.add(probe.getPopHawkResMean());
		data.add(probe.getPopDoveResMean());
		data.add(probe.getPopResMean());
		data.add(probe.getPopHawkResSD());
		data.add(probe.getPopDoveResSD());
		data.add(probe.getPopResSD());
		
		data.add(probe.getFissionCounts());
		data.add(probe.getFissionHF());
		data.add(probe.getFissionHawkResMean());
		data.add(probe.getFissionDoveResMean());
		data.add(probe.getFissionResMean());
		data.add(probe.getFissionHawkResSD());
		data.add(probe.getFissionDoveResSD());
		data.add(probe.getFissionResSD());

		data.add(probe.getNonFissionCounts());
		data.add(probe.getNonFissionHF());
		data.add(probe.getNonFissionHawkResMean());
		data.add(probe.getNonFissionDoveResMean());
		data.add(probe.getNonFissionResMean());
		data.add(probe.getNonFissionHawkResSD());
		data.add(probe.getNonFissionDoveResSD());
		data.add(probe.getNonFissionResSD());
		
		data.add(probe.msBetween);
		data.add(probe.msWithin);
		data.add(probe.fRatio);
		return true;
	}
	
	/**
	 * Print the data in console
	 */
	public void printDataInConsole() {
		if(state.schedule.getSteps() % 1000 == 0) {
			System.out.println("Step     Groups     Hawks     Doves    HawkFrequency     resMean     resSD     resCV   popHawkRes     popDoveRes   hawkResSD   doveResSD   fissionN   fissionHP   fissionHawkSD   fissionDoveSD");
			System.out.println(state.schedule.getSteps() + "   " + agents.numObjs + "   " + 
					probe.getPopHawks() + "   " + probe.getPopDoves() + "   " + probe.getPopHawkFrequency() + "   " + 
					probe.getPopResMean() + "   " + probe.getPopResSD() + "   " + (probe.getPopResSD()/probe.getPopResMean()) + "   " +
					probe.getPopHawkResMean() + "   " + probe.getPopDoveResMean() + 
					probe.getPopHawkResSD() + "   " + probe.getPopDoveResSD() + 
					probe.getFissionCounts() + "   " + probe.getFissionHF() + "   " + probe.getFissionHawkResSD() + "   " + probe.getFissionDoveResSD());
		}
		
	}
	
	/*
	 * ***************************************************************************
	 *                              CSV Files
	 * ***************************************************************************
	 */
	public void outputCSV_Hawk() throws IOException{
		if(!hawkCsvFile.exists()){
			System.out.println("Let's create a new csv file for reproduction delays!");
			hawkCsvFile.createNewFile();
		}
		FileWriter writer = new FileWriter(hawkCsvFile.getAbsoluteFile(), true);
		if(headerWritten_Hawk == false){
			for(int i=0; i < header.length; i++){
				if(i != 0)
					writer.append(",");
				writer.append(header[i]);
			}
			writer.append('\n');
		}
		headerWritten_Hawk = true;
		
		ArrayList<String> delaySteps = probe.getHawkDelaySteps();
		ArrayList<String> delayRes = probe.getHawkDelayRes();
		for(int i=0; i< delaySteps.size(); i++) {
			writer.append(Double.toString(state.schedule.getTime()));
			writer.append(",");
			writer.append(delaySteps.get(i));
			writer.append(",");
			writer.append(delayRes.get(i));
			writer.append('\n');
		}
		writer.flush();
		writer.close();
	}
	
	public void outputCSV_Dove() throws IOException{
		if(!doveCsvFile.exists()){
			System.out.println("Let's create a new csv file for reproduction delays!");
			doveCsvFile.createNewFile();
		}
		FileWriter writer = new FileWriter(doveCsvFile.getAbsoluteFile(), true);
		if(headerWritten_Dove == false){
			for(int i=0; i < header.length; i++){
				if(i != 0)
					writer.append(",");
				    writer.append(header[i]);
			}
			writer.append('\n');
		}
		
		headerWritten_Dove = true;
		ArrayList<String> delaySteps = probe.getDoveDelaySteps();
		ArrayList<String> delayRes = probe.getDoveDelayRes();
		for(int i=0; i< delaySteps.size(); i++) {
			writer.append(Double.toString(state.schedule.getTime()));
			writer.append(",");
			writer.append(delaySteps.get(i));
			writer.append(",");
			writer.append(delayRes.get(i));
			writer.append('\n');
		}
		writer.flush();
		writer.close();
	}
	

	/*
	 * ***************************************************************************
	 *                  Set the Experimenter in the Environment
	 * ***************************************************************************
	 */
	public GroupProbe getProbe() {
		return probe;
	}

	public void setProbe(GroupProbe probe) {
		this.probe = probe;
	}

	

}
