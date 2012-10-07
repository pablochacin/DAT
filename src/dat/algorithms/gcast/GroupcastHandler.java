package dat.algorithms.gcast;


import dat.Message;

public interface GroupcastHandler {

	public void handleCast(String group, Message message);
}
