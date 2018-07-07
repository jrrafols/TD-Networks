public interface World {
	public void set_pos(int[] temp);
    public int[] get_pos();
	
	public void takeAction(int a);
	public int getObs();
	public String toString();
}