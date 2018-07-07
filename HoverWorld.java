import java.io.*;
import java.util.Vector;

public class HoverWorld implements World{
	public int size0,size1;
	public int[][] env;
	public int[] pos = new int[2];
	public boolean[] allowed = {true,true,true,true};
	
	public HoverWorld(String filename){
		BufferedReader input = null;
        Vector v = new Vector();
        try {
            input = new BufferedReader( new FileReader(filename) );
            String line = null;
            String temp_str;
            
            int counter = 0;
            while (( line = input.readLine()) != null) {
                Vector temp_vec = new Vector();
                for (int i = 0; i < line.length(); i++){
                    temp_vec.addElement(String.valueOf(line.charAt(i)));
                }
                v.addElement(temp_vec.clone());
                counter++;
            }
        }
        catch (FileNotFoundException ex) {}
        catch (IOException ex){}
        finally {
            try {
                if (input!= null) {
                  input.close();
                }
            }
            catch (IOException ex) {}
        }
        this.size0 = v.size();
        this.size1 = ((Vector)v.elementAt(0)).size();
        this.env = new int[this.size0][this.size1];
        
        for (int i = 0; i < size0; i++){
            for (int j = 0; j < size1; j++){
                this.env[i][j] = Integer.parseInt((String)((Vector)v.elementAt(i)).elementAt(j));
            }
        }
	}
	
	public int[] get_pos(){ return this.pos; }
	public void set_pos(int[] temp){ for (int i = 0; i < 2; i++) this.pos[i] = temp[i]; }
	
	public void takeAction(int a){
        int current_spot = this.getObs();
        
        if(allowed[a]){
            if(a == 0){ //N
                this.pos[0] = (this.pos[0] + this.size0 - 1)%this.size0;
                if (this.env[this.pos[0]][this.pos[1]] == 9){
                    this.pos[0] = (this.pos[0] + 1)%this.size0;
                }
            }
            else if(a == 1){ //E
                this.pos[1] = (this.pos[1] + 1)%this.size1;
                if (this.env[this.pos[0]][this.pos[1]] == 9){
                    this.pos[1] = (this.pos[1] + this.size1 - 1)%this.size1;
                }
            }
            else if(a == 2){ //S
                this.pos[0] = (this.pos[0] + 1)%this.size0;
                if (this.env[this.pos[0]][this.pos[1]] == 9){
                    this.pos[0] = (this.pos[0] + this.size0 - 1)%this.size0;
                }
            }
            else if(a == 3){ //W
                this.pos[1] = (this.pos[1] + this.size1 - 1)%this.size1;
                if (this.env[this.pos[0]][this.pos[1]] == 9){
                    this.pos[1] = (this.pos[1] + 1)%this.size1;
                }
            }
        }
		
		if (this.env[this.pos[0]][this.pos[1]] == 6 && current_spot != 6){
            if (a == 0){
                allowed[0] = false;
                allowed[1] = false;
                allowed[2] = true;
                allowed[3] = false;
            }
            else if (a == 1){
                allowed[0] = false;
                allowed[1] = false;
                allowed[2] = false;
                allowed[3] = true;
            }
            else if (a == 2){
                allowed[0] = true;
                allowed[1] = false;
                allowed[2] = false;
                allowed[3] = false;
            }
            else if (a == 3){
                allowed[0] = false;
                allowed[1] = true;
                allowed[2] = false;
                allowed[3] = false;
            }
		}
		else if (this.getObs() != 6){
            allowed[0] = true;
            allowed[1] = true;
            allowed[2] = true;
            allowed[3] = true;
        }
	}
	
	public int getObs(){ return this.env[this.pos[0]][this.pos[1]]; }
    
    public String toString(){
        String s = "";
        int temp = this.env[this.pos[0]][this.pos[1]];
		this.env[this.pos[0]][this.pos[1]] = -1;
		for(int i = 0; i < this.size0; i++){
			for(int j = 0; j < this.size1;j++){
				if (this.env[i][j] >= 0) s+=this.env[i][j];
				else if (this.env[i][j] == -1) s+="A";
			}
			s+="\n";
		}
		s+="\n";
		this.env[this.pos[0]][this.pos[1]] = temp;
        return s;
	}
}