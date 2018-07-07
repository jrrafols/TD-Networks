import java.util.Random;
import java.io.*;

public class Training2{    
    
    //General printing functions
    public static void print(Object o){System.out.println(o);}
    public static void print(int o){System.out.println(o);}
    public static void print_(int o){System.out.print(o);}
    public static void print(String o){System.out.println(o);}
    public static void print_(String o){System.out.print(o);}
    public static void print(double[] p){
        for (int i = 0; i < p.length; i++){
            System.out.print(p[i]+" ");
        }
        System.out.println("");
    }
    
    static Random r = new Random(); //put number  to seed
    
    static World w;
    //static TDNet t;
    static HoverWorldTDNet t;
    static boolean debug = true;
    
    static double[] probs = {0.5,0.5}; //Must sum to 1
    
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
			else if(input.equals("d")){ a = 1; }
			else if(input.equals("s")){ a = 2; }
			else if(input.equals("a")){ a = 3; }
			//Save the TDNet State
			else if(input.equals("dbg")){
                t.debug_toggle(debug);
                t.debug = debug;
                debug = !debug;
                print("Debug "+(!debug));
                a = 99;
			}
			else if(input.equals("save")){
                a = 99;
                save();
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
            else if(input.equals("trainOnP")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                trainOnP(Integer.parseInt(input2));
			}
			else if(input.equals("trainOffP")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                trainOffP(Integer.parseInt(input2));
			}
			/*else if(input.equals("trainN")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                trainN(Integer.parseInt(input2));
			}
			else if(input.equals("trainS")){
                a = 99;
                print_("# of steps: ");
                try{input2 = br.readLine();}
                catch(IOException e){}
                trainS(Integer.parseInt(input2));
			}
			else if(input.equals("output")){
                outputTDNet();
			}*/
			else{
                a = 99;
            }
            
			if (a <= 3){
                w.takeAction(a);
                t.computeNet(a,w.getObs());
                //t.updateNet(a,w.getObs());
			}
            
            print("");
			print(w);
			print(t);
		}
    }
    
    public static void save(){
        t.set_pos(w.get_pos());
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		print_("File Name: ");
		try{input = br.readLine();}
        catch(IOException e){}
        
        try{
            FileOutputStream ostream = new FileOutputStream(input+".tmp");
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            p.writeObject(t);
            p.flush();
            ostream.close();
	     }
	     catch(FileNotFoundException e){}
	     catch(IOException e){}
    }
    
    public static void load(String name){
	    /*Object test = null;
		try{
	        FileInputStream istream = new FileInputStream(name+".tmp");
	        ObjectInputStream q = new ObjectInputStream(istream);
	        test = q.readObject();
	        q.close();
	        istream.close();
	      }
	      catch(FileNotFoundException e){System.out.println("test1");}
	      catch(ClassNotFoundException e){System.out.println("test2");}
	      catch(StreamCorruptedException e){System.out.println("test3");}
	      catch(IOException e){System.out.println("test4");}
	      t = (TDNet)test;
	      w.set_pos(t.get_pos());*/
	      print("load");
	}
    
    public static void trainOnP(int steps){
        int a;
        int count1 = 0;
        int count2 = 0;
        // a  = 0 : Up, a = 2 : Down
        for (int i = 0; i < steps; i++){
            a = r.nextInt(9);
            if (a == 0){
                //print_("Wandering...");
                do{
                    a = r.nextInt(2);
                    if (a == 1) a = 2;
                    //print_(a);print_(",");
                    w.takeAction(a);
                    t.updateNet(a, w.getObs());
                    //t.computeNet(a, w.getObs());
                    count1++;
                }while(r.nextDouble() < 0.1);
                //print("");
            }
            /*if (a == 0){
                count2 = count2;
            }*/
            else{
                a = a % 2;
                if (a == 1) a = 2;
                //print_("Option: "); print(a);
                do{    
                    w.takeAction(a);
                    t.computeNet(a,w.getObs());
                    count2++;
                }while (w.getObs() == 0);
            }
			if (i % 10000 == 9999){
                print(i+1);
                print(w);
                print(t);
			}
        }
        print_(count1+count2);print(" Steps taken");
        print_(count1);print(" Wandering");
        print_(count2);print(" On-policy");
    }
    
    /*public static void trainN(int steps){
        int a = 0;
        for (int i = 0; i < steps; i++){
            w.takeAction(a);
            t.computeNet(a,w.getObs());
			if (i % 10000 == 9999){
                print(i+1);
                print(w);
                print(t);
			}
        }
    }
    
    public static void trainS(int steps){
        int a = 2;
        for (int i = 0; i < steps; i++){
            w.takeAction(a);
            t.computeNet(a,w.getObs());
			if (i % 10000 == 9999){
                print(i+1);
                print(w);
                print(t);
			}
        }
    }*/
    
    public static void trainOffP(int steps){
        int a;
        double rand;
        for (int i = 0; i < steps; i++){
            rand = r.nextDouble();
            
            if (rand < probs[0]) a = 0;
            else a = 2;
            
            w.takeAction(a);
            t.computeNet(a,w.getObs());
			if (i % 10000 == 9999){
                print(i+1);
                print(w);
                print(t);
			}
        }
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
        String filename = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader("config2"));
		  filename = br.readLine();
        }
        catch(IOException e){}
        
        w = new HoverWorld(filename+".world");
        t = new HoverWorldTDNet(1.0,0.001, probs);
        
        print(w);
        print(t);
        
        interactive();   
    }

}