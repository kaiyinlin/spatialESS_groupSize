package structuredESS;

import java.awt.Color;

import sweep.GUIStateSweep;
import sweep.SimStateSweep;

public class GroupGUI extends GUIStateSweep {

	public GroupGUI(SimStateSweep state, int gridWidth, int gridHeight, Color backdrop, Color agentDefaultColor,
			boolean agentPortrayal) {
		super(state, gridWidth, gridHeight, backdrop, agentDefaultColor, agentPortrayal);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		initialize(SimEnvironment.class, Experimenter.class, GroupGUI.class, 400, 400, Color.white, Color.blue, false, spaces.SPARSE);

	}

}
