package cloudstudios.XClient;

public interface ChannelEventReceiver {
	public void OnMuteChanged();
	public void OnLevelChanged();
	public void OnGainChanged();
	public void OnDelayChanged();
	public void OnBypassChanged();
}
