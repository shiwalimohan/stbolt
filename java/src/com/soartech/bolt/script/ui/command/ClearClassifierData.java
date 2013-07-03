package com.soartech.bolt.script.ui.command;

import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;
import edu.umich.sbolt.SBolt;

public class ClearClassifierData implements UiCommand {

	@Override
	public void execute() {
		robot_command_t cmd = new robot_command_t();
		cmd.utime = TimeUtil.utime();
		cmd.dest = new double[6];
		cmd.updateDest = false;
		cmd.action = "CLEAR";
		SBolt.broadcastRobotCommand(cmd);
	}
}
