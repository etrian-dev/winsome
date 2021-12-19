package WinsomeRequests;

public class FollowRequest {
	String newFollower;

	public FollowRequest(String follower) {
		this.newFollower = follower;
	}

	public String getFollower() {
		return this.newFollower;
	}

	public boolean setFollower(String follower) {
		if (follower == null) {
			return false;
		}
		this.newFollower = follower;
		return true;
	}
}
