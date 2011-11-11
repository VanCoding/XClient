package cloudstudios.XClient;

public interface ClientEventReceiver {
	public void OnConnect();
	public void OnError(String message);
}
