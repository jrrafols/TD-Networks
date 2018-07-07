import java.util.Random;
import java.io.*;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.Vector;

public class OffTraining{    
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
    public static double[] behavior = new double[3];
    public static String filename;
    
    static WorldReader w;
    static ColorWorldTDNet3 t;
    
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
			//Save the TDNet State
			else if(input.equals("save")){
                a = 99;
                print_("FileName: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                save(input2);
			}
			//Train for a fixed number of steps
            else if(input.equals("load")){
                a = 99;
                print_("FileName: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                load(input2);
			}
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
			else if(input.equals("reset")){
                a = 99;
                
                double alpha, lambda;
                print_("Alpha val: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                alpha = Double.parseDouble(input2);
                
                print_("Lambda val: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                lambda = Double.parseDouble(input2);
                
                w = new WorldReader(filename+".world",35,9);
                t = new ColorWorldTDNet3(alpha, lambda, w.numFeatures, behavior);
			}
			else if(input.equals("output")){
                outputTDNet();
			}
			
			if (a <= 2){
                w.takeAction(a);
                //t.computeNet(a,w.getObs(),w.getRandomFeatures());
                t.computeNet(a,w.getObs(),w.getFeatures());
                //print(w.getRandomFeatures());
			}
            print(" ");
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
    
    //public static void save(){
    public static void save(String input){
        t.set_pos(w.get_pos());
        
        /*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		print_("File Name: ");
		try{input = br.readLine();}
        catch(IOException e){}*/
        
        try{
            FileOutputStream ostream = new FileOutputStream(input+".agt");
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            p.writeObject(t);
            p.flush();
            ostream.close();
	     }
	     catch(FileNotFoundException e){}
	     catch(IOException e){}
    }
    
    public static void load(String name){
	    Object test = null;
		try{
	        FileInputStream istream = new FileInputStream(name+".agt");
	        ObjectInputStream q = new ObjectInputStream(istream);
	        test = q.readObject();
	        q.close();
	        istream.close();
	      }
	      catch(FileNotFoundException e){System.out.println("test1");}
	      catch(ClassNotFoundException e){System.out.println("test2");}
	      catch(StreamCorruptedException e){System.out.println("test3");}
	      catch(IOException e){System.out.println("test4");}
	      t = (ColorWorldTDNet3)test;
	      w.set_pos(t.get_pos());
	}
    
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
        double rand_num;
        int counter = 0;
        
        while (counter <= steps){
            rand_num = r.nextDouble();
            if (rand_num < behavior[0]){
                w.takeAction(0);
                t.computeNet(0, w.getObs(), w.getRandomFeatures());
            }
            else if (rand_num < behavior[0]+behavior[1]){
                w.takeAction(1);
                t.computeNet(1, w.getObs(), w.getRandomFeatures());
            }
            else{
                w.takeAction(2);
                t.computeNet(2, w.getObs(), w.getRandomFeatures());
            }
            checkError(counter++);
        }
        //print(counter);
    }
    
    public static void train2(int steps){
        double rand_num;
        int counter = 0;
        
        while (counter <= steps){
            rand_num = r.nextDouble();
            if (rand_num < behavior[0]){
                w.takeAction(0);
                t.computeNet(0, w.getObs(), w.getFeatures());
            }
            else if (rand_num < behavior[0]+behavior[1]){
                w.takeAction(1);
                t.computeNet(1, w.getObs(), w.getFeatures());
            }
            else{
                w.takeAction(2);
                t.computeNet(2, w.getObs(), w.getFeatures());
            }
            checkError(counter++);
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
            //print(blah/45.0);
            error = new double[40];
        }
    }
    
    public static void train3(int steps){
        double rand_num;
        int counter = 0;
        int a;
        Vector actions = new Vector();
        
        int window = 5;
        
        for (int i = 0; i < window; i++){
            a = r_seeded.nextInt(4);
            if (a == 3) a = 0;
            actions.addElement(new Integer(a));
        }
        
        while (counter <= steps){
            a = ((Integer)actions.elementAt(0)).intValue();
            w.takeAction(a);
            t.computeNet(a, w.getObs(), w.getFeatures());
            checkError2(counter++);
            
            actions.remove(0); //pop action
            a = r_seeded.nextInt(4);
            if (a == 3) a = 0;
            actions.addElement(new Integer(a)); //push new action
        }
        //print(counter);
    }
    
    public static void train4(int steps){
        double rand_num;
        int counter = 0;
        int a;
        Vector actions = new Vector();
        
        int window = 5;
        
        for (int i = 0; i < window; i++){
            a = r_seeded.nextInt(4);
            if (a == 3) a = 0;
            actions.addElement(new Integer(a));
        }
        
        while (counter <= steps){
            a = ((Integer)actions.elementAt(0)).intValue();
            w.takeAction(a);
            t.computeNet(a, w.getObs(), w.getFeatures());
            counter++;
            
            actions.remove(0); //pop action
            a = r_seeded.nextInt(4);
            if (a == 3) a = 0;
            actions.addElement(new Integer(a)); //push new action
        }
        //print(counter);
    }
    
    public static void outputTDNet(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		print_("File Name: ");
		try{input = br.readLine();}
        catch(IOException e){}
        
        int[] temp = w.get_pos();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(input+".pos"));
            out.write(temp[0]+" "+temp[1]+" "+temp[2]);
            out.close();
        }
        catch (IOException e) {}
        print("Position Saved");
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(input+".state"));
            out.write(t.stateToString());
            out.close();
        }
        catch (IOException e) {}
        print("TD Network State Saved");
    }
    
    public static void main (String[] args){
        filename = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader("config"));
		  filename = br.readLine();
        }
        catch(IOException e){}
        
        w = new WorldReader(filename+".world",41,13);
        x_size = w.size0;
        y_size = w.size1;
        
        behavior[0] = 0.5; //F
        behavior[1] = 0.25; //R
        behavior[2] = 0.25; //L
        
        t = new ColorWorldTDNet3(0.001, 0, w.numFeatures, behavior);
        
        //print(w);
        //print(t);
        
        error = new double[45];
        int[] temp = {w.pos[0],w.pos[1],w.pos[2]};
        wanderNum = 10;
        getPredictions();
        w.pos[0]=temp[0];w.pos[1]=temp[1];w.pos[2]=temp[2];
        
        //interactive();
        
        /*
        //Off-Policy Random Features
        int[][] randSettings = {{60,3},{80,4},{100,5}, {60,6},{80,8},{100,10}, {60,12},{80,16},{100,20}};
        
        for (int i = 0; i < randSettings.length; i++){
            print_("numFeatures = ");print_(randSettings[i][0]);print_(" numActive = ");print(randSettings[i][1]);
                for(int k = 0; k < 10; k++){
                    w = new WorldReader(filename+".world",randSettings[i][0],randSettings[i][1]);
                    t = new ColorWorldTDNet3(0.005, 0.5, w.numFeatures, behavior);
                    train(500000);
                    //save("OffPolicyAgent"+k+", alpha0.005, lambda0.5 "+randSettings[i][0]+","+randSettings[i][1]);
                    print(" ");
                }
        }*/
        
        /*
        //Off-Policy Tilings
        double[] alphas = {0.001,0.005,0.01, 0.05};
        double[] lambdas = {0.5, 0.75, 0.9, 1.0};
        
        for (int i = 0; i < alphas.length; i++){
            for(int j = 0; j < lambdas.length; j++){
            print_("alpha = ");print_(alphas[i]);print_(" lambda = ");print(lambdas[j]);
                for(int k = 0; k < 10; k++){
                    t = new ColorWorldTDNet3(alphas[i], lambdas[j], w.numFeatures, behavior);
                    w = new WorldReader(filename+".world",41,13);
                    train2(500000);
                    save("OffPolicyAgent"+k+", alpha "+alphas[i]+", lambda "+lambdas[j]);
                    print(" ");
                }
            }
        }*/
        double[][] expected_update = new double[t.nodes][t.inputs];
        int iterations = 10000;
        for(int k = 0; k < iterations; k++){
            w = new WorldReader(filename+".world",41,13);
            t = new ColorWorldTDNet3(0.001, 0.0, w.numFeatures, behavior);
            train4(100);
            //save("1m steps,OffPolicyAgent"+k+", alpha 0.001, lambda 0.0");
            
            for (int i = 0; i < t.W_accumulate.length; i++){
                for (int j = 0; j < t.W_accumulate[i].length; j++){
                    expected_update[i][j] += t.W_accumulate[i][j];
                    //print(t.W_accumulate[i]);
                }
            }
            //print(" ");
        }
        for (int i = 0; i < t.W_accumulate.length; i++){
            for (int j = 0; j < t.W_accumulate[i].length; j++){
                expected_update[i][j] /= (double)iterations;
            }
            print(expected_update[i]);
        }
        //interactive();
    }

}