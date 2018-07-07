import java.util.Random;
import java.io.*;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.Vector;

public class OnTraining{    
    ///////////////////////////////////////////////////////////
    //General printing functions
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
    public static void print(double o){System.out.println(o);}
    public static void print_(double o){System.out.print(o);}
    public static void print(String o){System.out.println(o);}
    public static void print_(String o){System.out.print(o);}
    public static void print(double[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    public static void print(int[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    ///////////////////////////////////////////////////////////
    
    
    public static int x_size, y_size, wanderNum;
    public static double[][] predictions;
    static Random r = new Random();
    static Random r_seeded = new Random(15);
    public static DecimalFormat df = new DecimalFormat("0.000");
    public static String filename;
    
    static WorldReader w;
    static ColorWorldTDNet7 t;
    
    public static String[] node_names = new String[] {"Leap\t\t", "L-Leap\t\t","R-Leap\t\t","Leap-L-Leap\t","Leap-R-Leap\t","F\t\t","L\t\t","R\t\t","Wander\t\t"};
    
    public static void interactive() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		String input2 = "";
		
		int a = 0;
		while(!input.equals("quit")){
			System.out.print("Choose action: ");
			try{input = br.readLine();}
			catch(IOException e){}
			//Simple Movement
			if(input.equals("w")){ a = 0; }
			else if(input.equals("a")){ a = 2; }
			else if(input.equals("d")){ a = 1; }
			//Train for a fixed number of steps
            else if(input.equals("train")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                train(Integer.parseInt(input2));
			}
			else if(input.equals("train2")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                train2(Integer.parseInt(input2));
			}
			else if(input.equals("train3")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                train3(Integer.parseInt(input2));
			}
			
			if (a <= 2){
                w.takeAction(a);
                t.computeNet(a,a,w.getObs(),w.getFeatures());
			}
            
			print(w);
			print(t);
		}
    }
    
    ///////////////////////////////////////////////////
    //Error Calc code
    public static int stateNum(int pos0,int pos1,int pos2){return pos0 * y_size * 4 + pos1*4 + pos2;}
    public static int stateNum(int[] pos){return pos[0] * y_size * 4 + pos[1]*4 + pos[2];}
    public static double[] takeSeq(int seq){
        double[] result = null;
        if(seq == 0){
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 1){
            w.takeAction(2);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 2){
            w.takeAction(1);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 3){
            while (w.getObs() == 0){w.takeAction(0);}
            w.takeAction(2);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 4){
            while (w.getObs() == 0){w.takeAction(0);}
            w.takeAction(1);
            while (w.getObs() == 0){w.takeAction(0);}
            result = w.getBitObs();
        }
        else if(seq == 5){
            w.takeAction(0);
            result = w.getBitObs();
        }
        else if(seq == 6){
            w.takeAction(2);
            result = w.getBitObs();
        }
        else if(seq == 7){
            w.takeAction(1);
            result = w.getBitObs();
        }
        else if(seq == 8){
            int[] temp = new int[3];
            temp[0] = w.pos[0]; temp[1] = w.pos[1]; temp[2] = w.pos[2];
            double[] temp2;
            double randDouble;
            int a;
            result = new double[6];
            int reps = wanderNum;
            
            for (int i = 0; i < reps; i++){
                w.set_pos(temp);
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    w.takeAction(a); //Random Step
                }while(w.getObs() == 0 && randDouble < 0.5);
                
                for (int j = 0; j < 6; j++){
                    temp2 = w.getBitObs();
                    result[j] += temp2[j];
                }
            }
            for(int i = 0; i < 6; i++){
                result[i] = result[i]/reps;
            }
            w.set_pos(temp);
        }
        
        return result;
    }
    
    public static void getPredictions(){
        int[] temp_pos = new int[3];
        double[] result;
        predictions = new double[x_size*y_size*4][45];
        
        for (int i = 1; i < x_size - 1; i++){
            for (int j = 1; j < y_size - 1; j++){
                for (int k = 0; k < 4; k++){
                    temp_pos[0] = i; temp_pos[1] = j; temp_pos[2] = k;
                    for (int l = 0; l < 9; l++){
                        //w.pos[0] = i; w.pos[1] = j; w.pos[2] = k;
                        w.set_pos(temp_pos);
                        result = takeSeq(l);
                        for (int m = 0; m < 5; m++){
                            predictions[stateNum(temp_pos)][l*5+m] = result[m+1];
                        }
                    }
                    //w.set_pos(temp_pos);
                    //print(w.pos);
                    //print(w);
                    //System.out.println(statePredictions(temp_pos));
                }
            }
        }
    }
    ///////////////////////////////////////////////////
    public static double[] error;
    public static String stateErrors(){
        String s = "\t\t    1     2     3     4     5\n";
        for (int i = 0; i < 9; i++){
            s+= node_names[i]+ ": ";
            for (int j = 0; j < 5; j++){
                s+= df.format(error[(i*5)+j]) + " ";
            }
            s+= "\n";
		}
        return s;
    }
    
    public static void checkError(int counter){
        double blah;
        for (int i = 0; i < 45; i++){
            blah = predictions[stateNum(w.get_pos())][i] - t.y[i];
            error[i] += blah*blah;
        }
        if (counter != 0 && counter % 10000 == 0){
            /*print_("Step: ");
            print_(counter);
            print_(", Average Error: ");
            double sum = 0;
            for (int i = 0; i < 45; i++){
                sum += error[i];
                error[i] /= 100;
            }
            print(sum/45/100.0);
            print("Average Node Errors");
            print(stateErrors());
            error = new double[45];*/
            
            print_(counter);
            print_(" Network_Average, ");
            blah = 0;
            for (int i = 0; i < 45; i++){
                error[i] = Math.sqrt(error[i]/10000);
                blah+=error[i];
            }
            print_(blah/45.0); print_(" "); print(error);
            //print(blah/45.0);
            error = new double[45];
        }
    }
    
    public static void train(int steps){
        int a, rand_num;
        int counter = 0;
        double randDouble;
        while (counter <= steps){
            rand_num = r.nextInt(100);
			if (rand_num < 50){ //F,R,L
                w.takeAction(0);
                t.computeNet(0, 0, w.getObs(),w.getFeatures());
                checkError(counter++);
            }
            else if (rand_num < 70){ //F,R,L
                w.takeAction(1);
                t.computeNet(1, 1, w.getObs(), w.getFeatures());
                checkError(counter++);
            }
            else if (rand_num < 90){ //F,R,L
                w.takeAction(2);
                t.computeNet(2, 2, w.getObs(), w.getFeatures());
                checkError(counter++);
            }
            else if (rand_num < 95){ //Leap
                do{
                    w.takeAction(0); //Step Forward
                    t.computeNet(0, 3, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }while(w.getObs() == 0);
                
                w.takeAction(0); //Step Forward
                t.computeNet(0, 3, w.getObs(), w.getFeatures());
                checkError(counter++);
            }
            else{ //Wander
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    w.takeAction(a); //Random Step
                    t.computeNet(a, 4, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }while(w.getObs() == 0 && randDouble < 0.5);
                
            }
        }
        //print(counter);
    }
    
    public static void train2(int steps){
        int a, rand_num;
        int counter = 0;
        double randDouble;
        while (counter <= steps){
            for (int i = 0; i < 100; i++){
                rand_num = r.nextInt(100);
                if (rand_num < 50){ //F,R,L
                    w.takeAction(0);
                    t.computeNet(0, 0, w.getObs(),w.getFeatures());
                    checkError(counter++);
                }
                else if (rand_num < 75){ //F,R,L
                    w.takeAction(1);
                    t.computeNet(1, 1, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }
                else{ //F,R,L
                    w.takeAction(2);
                    t.computeNet(2, 2, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }
                if (counter > steps) break;
            }
            rand_num = r.nextInt(100);
            if (rand_num < 50){ //Leap
                do{
                    w.takeAction(0); //Step Forward
                    t.computeNet(0, 3, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }while(w.getObs() == 0);
                
                w.takeAction(0); //Step Forward
                t.computeNet(0, 3, w.getObs(), w.getFeatures());
                checkError(counter++);
            }
            else{ //Wander
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    w.takeAction(a); //Random Step
                    t.computeNet(a, 4, w.getObs(), w.getFeatures());
                    checkError(counter++);
                }while(w.getObs() == 0 && randDouble < 0.5);
                
            }
        }
        //print(counter);
    }
    
    public static void checkError2(int counter){
        double blah;
        for (int i = 0; i < 40; i++){
            blah = predictions[stateNum(w.get_pos())][i] - t.y[i];
            error[i] += blah*blah;
        }
        if (counter != 0 && counter % 10000 == 0){
            print_(counter);
            print_(" Network_Average, ");
            blah = 0;
            for (int i = 0; i < 40; i++){
                error[i] = Math.sqrt(error[i]/10000);
                blah+=error[i];
            }
            print_(blah/40.0); print_(" "); print(error);
            error = new double[40];
        }
    }
    
    public static boolean checkLeap(Vector v){
        int[] agent_pos = {w.pos[0],w.pos[1],w.pos[2]};
        //print(w.pos);
        int v_length = v.size();
        int action = ((Integer)v.elementAt(0)).intValue();
        
        for (int i = 0; i < v_length && (action == 0); i++)
        {
            w.takeAction(action);
            if (w.getObs() != 0) {
                w.pos[0]=agent_pos[0];w.pos[1]=agent_pos[1];w.pos[2]=agent_pos[2];
                //print(w.pos); print(" ");
                if (r.nextDouble() < 0.1) return true;//Gotta learn about F sometimes
                else return false;
            }
            action = ((Integer)v.elementAt(i)).intValue();
            
        }
        
        w.pos[0]=agent_pos[0];w.pos[1]=agent_pos[1];w.pos[2]=agent_pos[2];
        //print(w.pos); print(" ");
        return false;
    }
    
    public static void train3(int steps){
        int a, rand_num;
        int counter = 0;
        double randDouble;
        Vector actions = new Vector();
        
        int[] test = new int[3];
        
        int window = 5;
        
        for (int i = 0; i < window; i++){
            a = r_seeded.nextInt(4);
            if (a == 3) a = 0;
            actions.addElement(new Integer(a));
        }
        
        while (counter <= steps){
            //print(counter);
            //print(actions);
            if (checkLeap(actions)){ //Leap
                do{
                    //print(actions);
                    w.takeAction(0); //Step Forward
                    t.computeNet(0, 3, w.getObs(), w.getFeatures());
                    checkError2(counter++);
                    
                    actions.remove(0); //pop action
                    a = r_seeded.nextInt(4);
                    if (a == 3) a = 0;
                    actions.addElement(new Integer(a)); //push new action
                }while(w.getObs() == 0);
                test[0]++;
            }
            /*else if (checkWander(actions)){
                do{
                    a = ((Integer)actions.elementAt(0)).intValue();
                    w.takeAction(a);
                    t.computeNet(a, 4, w.getObs(), w.getFeatures());
                    checkError2(counter++);
                    
                    actions.remove(0); //pop action
                    a = r.nextInt(4);
                    if (a == 3) a = 0;
                    actions.addElement(new Integer(a)); //push new action
                }while(w.getObs() == 0 && r.nextDouble() < 0.5);
                test[1]++;
            }*/
            else {
                a = ((Integer)actions.elementAt(0)).intValue();
                w.takeAction(a);
                t.computeNet(a,a,w.getObs(), w.getFeatures());
                checkError2(counter++);
                
                actions.remove(0); //pop action
                a = r_seeded.nextInt(4);
                if (a == 3) a = 0;
                actions.addElement(new Integer(a)); //push new action
                test[2]++;
            }
        }
        print(test);
    }
    
    public static void train4(int steps){
        int a, rand_num;
        int counter = 0;
        double randDouble;
        while (counter <= steps){
            rand_num = r.nextInt(100);
			if (rand_num < 50){ //F,R,L
                w.takeAction(0);
                t.computeNet(0, 0, w.getObs(),w.getFeatures());
                counter++;
            }
            else if (rand_num < 70){ //F,R,L
                w.takeAction(1);
                t.computeNet(1, 1, w.getObs(), w.getFeatures());
                counter++;
            }
            else if (rand_num < 90){ //F,R,L
                w.takeAction(2);
                t.computeNet(2, 2, w.getObs(), w.getFeatures());
                counter++;            }
            else if (rand_num < 95){ //Leap
                do{
                    w.takeAction(0); //Step Forward
                    t.computeNet(0, 3, w.getObs(), w.getFeatures());
                    counter++;
                }while(w.getObs() == 0);
                
                w.takeAction(0); //Step Forward
                t.computeNet(0, 3, w.getObs(), w.getFeatures());
                counter++;
            }
            else{ //Wander
                do{
                    a = r.nextInt(3);
                    randDouble = r.nextDouble();
                    w.takeAction(a); //Random Step
                    t.computeNet(a, 4, w.getObs(), w.getFeatures());
                    counter++;
                }while(w.getObs() == 0 && randDouble < 0.5);
                
            }
        }
        //print(counter);
    }
    
	public static void train5(){
		do{
			w.takeAction(0); //Step Forward
			t.computeNet(0, 3, w.getObs(), w.getFeatures());
		}while(w.getObs() == 0);
    	}

    public static void main (String[] args){
        try{
            BufferedReader br = new BufferedReader(new FileReader("config"));
		  filename = br.readLine();
        }
        catch(IOException e){}
        
        w = new WorldReader(filename+".world");
        x_size = w.size0;
        y_size = w.size1;
        
        //t = new ColorWorldTDNet(0.05, 0.9);
        
        //print(w);
        //print(t);
        
        /*error = new double[45];
        int[] temp = {w.pos[0],w.pos[1],w.pos[2]};
        wanderNum = 1000;
        getPredictions();
        w.pos[0]=temp[0];w.pos[1]=temp[1];w.pos[2]=temp[2];*/
        
        /*//Prints out the randomly generated state Vectors
        int stateNumber;
        for (int i = 1; i < x_size - 1; i++){
            for (int j = 1; j < y_size - 1; j++){
                for (int k = 0; k < 4; k++){
                    stateNumber = stateNum(i,j,k);
                    print(w.stateVectors[stateNumber]);
                }
            }
        }*/
        
        
        w = new WorldReader(filename+".world",41,13);
        t = new ColorWorldTDNet7(0.05, 1.0,w.numFeatures);
            
        //interactive();   
        
        
        /*double[] alphas = {0.01, 0.05, 0.1};
        double[] lambdas = {0, 0.25, 0.5, 0.75, 0.9, 1.0};
        
        for (int i = 0; i < alphas.length; i++){
            for(int j = 0; j < lambdas.length; j++){
            print_("alpha = ");print_(alphas[i]);print_("lambda = ");print(lambdas[j]);
                for(int k = 0; k < 10; k++){
                    w = new WorldReader(filename+".world",41,13);
                    t = new ColorWorldTDNet7(alphas[i], lambdas[j],w.numFeatures);
                    train(250000);
                    save("OnPolicyAgent"+k+", alpha "+alphas[i]+", lambda "+lambdas[j]);
                    print(" ");
                }
            }
        }*/
        
        /*for(int k = 0; k < 30; k++){
            w = new WorldReader(filename+".world",41,13);
            t = new ColorWorldTDNet(0.05, 0.0 ,w.numFeatures);
            train3(1000000);
            //save("OnPolicyAgent"+k+", alpha 0.001, lambda 0.0");
            print(" ");
        }*/


	double[][] expected_update = new double[t.nodes][t.inputs];
	double[] temp_vec = new double[t.inputs];
        
	int iterations = 1;
	for(int k = 0; k < iterations; k++){
 		w = new WorldReader(filename+".world",41,13);
		w.pos[2] = 1;
		print(w);
		t = new ColorWorldTDNet7(0.05, 0.0, w.numFeatures);
		train5();

		for (int i = 0; i < t.W_accumulate.length; i++){
			for (int j = 0; j < t.W_accumulate[i].length; j++){
				expected_update[i][j] += t.W_accumulate[i][j];
			}
		}
		
		if(k != 0 && k % 250 == 0){
			print(k);
			for (int i = 0; i < t.W_accumulate.length; i++){
            			for (int j = 0; j < t.W_accumulate[i].length; j++){
                			temp_vec[j] = expected_update[i][j]/(double)k;
            			}
            			print(temp_vec);
       		}
			print(" ");
		}
	  }

	print(w);
	for (int i = 0; i < t.W_accumulate.length; i++){	
		print(expected_update[i]);
	}
	print(" ");
	}
}