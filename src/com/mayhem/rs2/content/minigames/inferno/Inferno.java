package com.mayhem.rs2.content.minigames.inferno;

import com.mayhem.core.task.Task;
import com.mayhem.core.task.TaskQueue;
import com.mayhem.core.task.Task.BreakType;
import com.mayhem.core.task.Task.StackType;
import com.mayhem.core.task.impl.TaskIdentifier;
import com.mayhem.core.util.Utility;
import com.mayhem.rs2.content.minigames.fightcave.TzharrController;
import com.mayhem.rs2.content.minigames.fightcave.TzharrData;
import com.mayhem.rs2.content.minigames.fightcave.TzharrData.NPCS_DETAILS;
import com.mayhem.rs2.entity.Location;
import com.mayhem.rs2.entity.mob.Mob;
import com.mayhem.rs2.entity.player.Player;
import com.mayhem.rs2.entity.player.controllers.ControllerManager;
import com.mayhem.rs2.entity.player.net.out.impl.SendMessage;
import com.mayhem.rs2.entity.player.net.out.impl.SendRemoveInterfaces;

public class Inferno  {

	public static final InfernoController CONTROLLER = new InfernoController();
	private int killsRemaining;
	//change
	public static final Location LEAVE = new Location(2438, 5168, 0);
	
	public int getHeight(Player player) {
		return player.getIndex()*4;
	}

	public void spawn(Player player) {
		TaskQueue.queue(new Task(player, 20, false, StackType.NEVER_STACK, BreakType.NEVER, TaskIdentifier.TZHAAR) {
			@Override
			public void execute() {
					if(player.infernoWaveId == 68) {
						player.send(new SendMessage("Relog if the boss instance does not start within the next 10 seconds."));
						//initiateTzkalzuk();
					}
				//int index = Utility.random(InfernoWave.SPAWN_DATA.length - 1);
				
				//int random = Utility.random(10);
				
				int startX = 2271 + Utility.random(5); //InfernoWave.SPAWN_DATA[index][0]
				int startY = 5342 + Utility.random(5); // InfernoWave.SPAWN_DATA[index][1]
				
				for (short i : InfernoWave.InfernoData.values()[player.getInfernoDetails().getStage()].getNpcs()) {
					Mob mob = new Mob(player, i, true, false, true, new Location(startX,startY,getHeight(player)));
					mob.getFollowing().setIgnoreDistance(true);
					mob.getCombat().setAttack(player);
					player.getInfernoDetails().addNpc(mob);
				}
				player.getClient().queueOutgoingPacket(new SendMessage("@dre@Wave: " + (player.getInfernoDetails().getStage() + 1)));
				stop();

			}
			@Override
			public void onStop() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public static final void checkForFightCave(Player p, Mob mob) {
		if (p.getController().equals(CONTROLLER)) {

			p.getInfernoDetails().removeNpc(mob);
			
			
			if (p.getInfernoDetails().getKillAmount() == 0) {
				if (p.getInfernoDetails().getStage() == 68) {
					p.getInferno().stop(p);
					return;
				}
				p.getInfernoDetails().increaseStage();
				p.getInferno().spawn(p);
			}
		}
	}

	public void leaveGame(Player player) {
		if (System.currentTimeMillis() - player.infernoLeaveTimer < 15000) {
			player.send(new SendMessage("You cannot leave yet, wait a couple of seconds and try again."));
			return;
		}
		killAllSpawns(player);
		player.send(new SendMessage("You have left the Inferno minigame."));
		player.teleport(new Location(2495, 5110, 0));
		player.infernoWaveType = 0;
		player.infernoWaveId = 0;
	}

	public void create(Player player,int type) {
		player.setController(CONTROLLER);
		player.send(new SendRemoveInterfaces());
		player.teleport(new Location(2271, 5329, getHeight(player)));
		player.infernoWaveType = type;
		player.send(new SendMessage("Welcome to the Inferno. Your first wave will start soon."));
		player.infernoWaveId = 0;
		player.infernoLeaveTimer = System.currentTimeMillis();
		spawn(player);
	}

	public void stop(Player player) {
		reward(player);
		player.teleport(new Location(2495, 5110, 0));
		player.sendMessage("@red@Congratulations on completing the Inferno! Enjoy your brand new cape!");
		player.waveInfo[player.infernoWaveType - 1] += 1;
		player.infernoWaveType = 0;
		player.infernoWaveId = 0;
		killAllSpawns(player);
		player.zukDead = false;
	}

	public void handleDeath(Player player) {
		player.teleport(new Location(2495, 5110, 0));
		player.send(new SendMessage("@red@Unfortunately you died on wave " + player.infernoWaveId + ". Better luck next time."));
		player.infernoWaveType = 0;
		player.infernoWaveId = 0;
		killAllSpawns(player);
	}

	public void killAllSpawns(Player player) {
		for (Mob i : player.getJadDetails().getMobs()) {
			if (i != null) {
				i.remove();
			}
		}

		player.getJadDetails().getMobs().clear();

		player.setController(ControllerManager.DEFAULT_CONTROLLER);
	}
	
	public void gamble(Player player) {
		if (!player.getInventory().hasItemId(INFERNAL_CAPE)) {
			player.send(new SendMessage("You do not have a infernal cape."));
			return;
		}
		player.getInventory().remove(INFERNAL_CAPE, 1);
		
		if (Utility.random(200) == 0) {
			 if (player.getItems().getItemCount(13225, true) == 0 && player.summonId != 13225) {
				 PlayerHandler.executeGlobalMessage("[@red@PET@bla@] @cr20@<col=255> " + player.playerName + "</col> received a pet from <col=255>TzTok-Jad</col>.");
				 player.getInventory().addOrCreateGroundItem(13225, 1, true);
				 player.getDH().sendDialogues(74, 2180);
			 }
		} else {
			player.getDH().sendDialogues(73, 2180);
			return;
		}
	}

	private static final int[] REWARD_ITEMS = { 6571, 6528, 11128, 6523, 6524, 6525, 6526, 6527, 6568, 6523, 6524, 6525, 6526, 6527, 6568 };

	public static final int FIRE_CAPE = 6570;
	public static final int INFERNAL_CAPE = 21295;
	public static final int ENRAGED_CAPE = 21296;
	public static final int ENSOULED_CAPE = 21297;

	public static final int TOKKUL = 6529;

	public void reward(Player player) {
		//Achievements.increase(player, AchievementType.FIGHT_CAVES_ROUNDS, 1);
		switch (player.infernoWaveType) {
		case 1:
			player.getInventory().addOrCreateGroundItem(INFERNAL_CAPE, 1, true);
			break;
		case 2:
			player.getInventory().addOrCreateGroundItem(ENSOULED_CAPE, 1, true);
			break;
		case 3:
			player.getInventory().addOrCreateGroundItem(ENRAGED_CAPE, 1, true);
			break;
		}
		player.getInventory().addOrCreateGroundItem(TOKKUL, (10000 * player.infernoWaveType) + Utility.random(5000), true);
	}

	public int getKillsRemaining() {
		return killsRemaining;
	}

	public void setKillsRemaining(int remaining) {
		this.killsRemaining = remaining;
	}

}
