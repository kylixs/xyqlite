package com.androidxyq.battle;

import com.androidxyq.sprite.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 战斗指令控制器
 * @author dewitt
 * @date 2009-11-11 create
 */
public class CommandController {
	private BattleScreen canvas;
	private List cmdlist;
	private List playerlist;
	private CommandInterpreter interpretor;
	private BattleCalculator battleCalculator;
    private Random random = new Random();

    public CommandController(BattleScreen canvas) {
		this.canvas = canvas;
		this.interpretor = new CommandInterpreter(canvas);
		this.cmdlist = new ArrayList();
		this.battleCalculator = new BattleCalculator();
	}
	

	/**
	 * 回合战斗
	 */
	synchronized public void turnBattle() {
        System.err.println("thread:"+Thread.currentThread().getId()+" Command Controller turnBattle begin");
		prepareNpcCommands();
		// 计算战斗结果
		Map params = new HashMap();//影响因素
		List<Player> t1 = canvas.getAdversaryTeam();
		List<Player> t2 = canvas.getOwnsideTeam();
		List<Command> results = battleCalculator.calc(cmdlist,t2,t1,params);
		//依次执行指令
		for (int i = 0; i < results.size(); i++) {
			Command cmd = results.get(i);
			try {
				System.out.println("执行："+cmd);
				this.interpretor.exec(cmd);
			}catch(Exception e) {
				System.out.println("战斗指令执行失败！"+cmd);
				e.printStackTrace();
			}
		}
        //清除命令列表
        cmdlist.clear();

        //检查双方的状态
		int result = checkTeamStatus();
        if(result == 1){
            System.err.println("战斗结束，我方获得胜利。");
            canvas.handleBattleEvent(new BattleEvent(canvas,BattleEvent.BATTLE_WIN));
        }else if(result == 2){
            //战斗失败
            System.err.println("战斗结束，我方失败了。");
            canvas.handleBattleEvent(new BattleEvent(canvas,BattleEvent.BATTLE_DEFEATED));
        }else {
            canvas.turnBegin();
        }
        System.err.println("thread:"+Thread.currentThread().getId()+" Command Controller turnBattle end");
	}

    /**
	 * 生成npc的指令
	 */
	protected void prepareNpcCommands() {
		//生成npc的指令
		List<Player> t1 = canvas.getAdversaryTeam();
		List<Player> t2 = canvas.getOwnsideTeam();
		for (int i = 0; i < t1.size(); i++) {
			Player elf = t1.get(i);
			Player target = t2.get(random.nextInt(t2.size()));
			cmdlist.add( new Command("attack",elf,target));
		}

	}
	
	/**
	 * 检查双方的状态，是否胜利或失败
     * @return  0-正常状态（继续战斗），1-胜利，2-失败
	 */
	protected int checkTeamStatus() {
		int result = 0;
		List<Player> t1 = canvas.getAdversaryTeam();
		List<Player> t2 = canvas.getOwnsideTeam();
		//如果敌方单位都已死亡，则我方胜利
		boolean win = true;
		for(int i=0;i<t1.size();i++) {
			Player elf = t1.get(i);
			if(elf.getData().hp > 0) {
				win = false;
				break;
			}
		}
		if(win) {
			return 1;
		}
		
		//如果我方单位全部死亡，则战斗失败
		boolean failure = true;
		for(int i=0;i<t2.size();i++) {
			Player player = t2.get(i);
			if(player.getData().hp>0) {
				failure = false;
				break;
			}
		}
		if(failure) {
			return 2;
		}

        return 0;
	}
	
	public void addCmd(Command cmd) {
		cmdlist.add(cmd);
	}

}
