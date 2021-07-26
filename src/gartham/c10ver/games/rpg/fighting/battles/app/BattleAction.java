package gartham.c10ver.games.rpg.fighting.battles.app;

public class BattleAction {
	public enum ActionType {
		SURRENDER, SKIP_TURN, ATTACK, SPECIAL_ATTACK;
	}

	private final ActionType type;
	private final Attack specialAttack;

	private BattleAction(ActionType type) {
		this.type = type;
		specialAttack = null;
	}

	private BattleAction(Attack specialAttack) {
		this.specialAttack = specialAttack;
		type = ActionType.SPECIAL_ATTACK;
	}

	public ActionType getType() {
		return type;
	}

	public Attack getSpecialAttack() {
		return specialAttack;
	}

	public boolean isSpecialAttack() {
		return getType() == ActionType.SPECIAL_ATTACK;
	}

}