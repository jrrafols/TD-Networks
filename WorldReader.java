import java.io.*;
import java.util.Vector;
import java.util.Random;
import java.lang.Math;

//public class WorldReader implements World{
public class WorldReader{
	public int size0,size1;
	public int[][] env;
	public int[] pos = new int[3];
	
	//Random State Vector variables
	public int numFeatures;
	public int numActive;
	public int[][] stateVectors;
	public Random r = new Random();//param is seed
	
	public WorldReader(String filename){
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
                    temp_str = String.valueOf(line.charAt(i));
                    if (temp_str.equals("A")){
                        pos[0] = counter;
                        pos[1] = i;
                        pos[2] = 0;
                        temp_vec.addElement("0");
                    }
                    else{
                        temp_vec.addElement(temp_str);
                    }
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
	
	public WorldReader(String filename, int nf, int na){
		this.numFeatures = nf;
		this.numActive = na;
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
                    temp_str = String.valueOf(line.charAt(i));
                    if (temp_str.equals("A")){
                        pos[0] = counter;
                        pos[1] = i;
                        pos[2] = 0;
                        temp_vec.addElement("0");
                    }
                    else{
                        temp_vec.addElement(temp_str);
                    }
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
        
        //Generate random state vectors for each state
        stateVectors = new int[size0*size1*4][numActive];
        int[] tempVector;
        int count = 0;
        int stateNumber;
        int temp;
        for (int i = 1; i < size0 - 1; i++){
            for (int j = 1; j < size1 - 1; j++){
                for (int k = 0; k < 4; k++){
                    stateNumber = stateNum(i,j,k);
                    count = 0;
                    tempVector = new int[numFeatures];
                    temp = getRand();
                    tempVector[temp] = 1;
                    stateVectors[stateNumber][count++] = temp;
                    while (count < numActive){
                        temp = getRand();
                        while(tempVector[temp] == 1){
                            temp = getRand();
                        }
                        tempVector[temp] = 1;
                        stateVectors[stateNumber][count++] = temp;
                    }
                }
            }
        }
	}
	
	public int getRand(){return Math.abs(r.nextInt() % numFeatures);}
	public int stateNum(int pos0,int pos1,int pos2){return pos0 * size1 * 4 + pos1*4 + pos2;}
	public int stateNum(int[] pos){return pos[0] * size1 * 4 + pos[1]*4 + pos[2];}
	
	public int[] get_pos(){ return this.pos; }
	public void set_pos(int[] temp){ for (int i = 0; i < 3; i++) this.pos[i] = temp[i]; }
	
	public void takeAction(int a){
		if(a == 0){ //F
			if(this.getObs()==0){
				if(this.pos[2]==0)
				    if (this.pos[0] != 0) this.pos[0] -= 1;
				    else this.pos[0] = this.size0 - 1;
				else if(this.pos[2]==1)
				    if (this.pos[1] != this.size1 - 1) this.pos[1] += 1;
				    else this.pos[1] = 0;
				else if(this.pos[2]==2)
				    if (this.pos[0] != this.size0 - 1) this.pos[0] += 1;
				    else this.pos[0] = 0;
				else if(this.pos[2]==3)
				    if (this.pos[1] != 0) this.pos[1] -= 1;
				    else this.pos[1] = this.size1 - 1;
			}
		}
		else if(a == 1){ //R
			this.pos[2] = (this.pos[2]+1)%4;
		}
		else if(a == 2){ //L
			this.pos[2] -= 1;
			if(this.pos[2]<0) this.pos[2]+=4;
		}
		
	}
	
	public int getObs(){
		int obs = 0;
		if (this.pos[2] == 0)
            if (this.pos[0] - 1 >= 0) obs = this.env[this.pos[0] - 1][this.pos[1]];
            else obs = this.env[this.size0-1][this.pos[1]];
		else if (this.pos[2] == 1) 
            if(this.pos[1]+1 < this.size1) obs = this.env[this.pos[0]][this.pos[1]+1];
            else obs = this.env[this.pos[0]][0];
		else if (this.pos[2] == 2) 
            if (this.pos[0] + 1 < this.size0) obs = this.env[this.pos[0] + 1][this.pos[1]];
            else obs = this.env[0][this.pos[1]];
		else if (this.pos[2] == 3)
            if(this.pos[1]-1>=0) obs = this.env[this.pos[0]][this.pos[1]-1];
            else obs = this.env[this.pos[0]][this.size1 - 1];
		return obs;
	}
	
	public double[] getBitObs(){
	   int obsNum = getObs();
	   double[] bitObs = new double[6];
	   bitObs[obsNum] = 1.0;
	   return bitObs;
	}
	
	public int[] getRandomFeatures(){
	   int[] tempVector = new int[numActive+2];//Active+obs+bias
	   int state = stateNum(pos);
	   for(int i = 0; i < numActive; i++){tempVector[i] = stateVectors[state][i];}//Active Random Features
	   tempVector[numActive] = getObs()+numFeatures; //Observation
	   tempVector[numActive+1] = numFeatures+6; //Bias
	   return tempVector;
	}
    
    public int[] getFeatures(){
        int[] features = new int[13];
        int offset = 0;
        
        //V Tilings
        //Width 2
        features[0] = offset+(pos[0]/2);
        offset += 4;
        features[1] = offset+((pos[0]-1)/2);
        offset += 3;
        //Width 3
        features[2] = offset + (pos[0]/3);
        offset += 3;
        features[3] = offset + ((pos[0]-1)/3);
        offset += 2;
        features[4] = offset + ((pos[0]+1)/3);
        offset += 3;
        
        //H Tilings
        //Width 2
        features[5] = offset+(pos[1]/2);
        offset += 4;
        features[6] = offset+((pos[1]-1)/2);
        offset += 3;
        //Width 3
        features[7] = offset + (pos[1]/3);
        offset += 3;
        features[8] = offset + ((pos[1]-1)/3);
        offset += 2;
        features[9] = offset + ((pos[1]+1)/3);
        offset += 3;
        
        //Direction
        features[10] = offset+pos[2];
        offset += 4;
        //Observation
        features[11] = offset+getObs();
        offset += 6;
        //Bias
        features[12] = offset;
        return features;
    }
    
    public String toString(){
        String s = "";
		this.env[this.pos[0]][this.pos[1]] = 10+this.pos[2];
		for(int i = 0; i < this.size0; i++){
			for(int j = 0; j < this.size1;j++){
				if (this.env[i][j] <=9) s+=this.env[i][j];
				else if (this.env[i][j] == 10) s+="^";
				else if (this.env[i][j] == 11) s+=">";
				else if (this.env[i][j] == 12) s+="v";
				else if (this.env[i][j] == 13) s+="<";
			}
			s+="\n";
		}
		s+="\n";
		this.env[this.pos[0]][this.pos[1]] = 0;
        return s;
	}
}