import java.util.Vector;

public class Test{

    public static void main(String[] args){
        Vector v = new Vector();
        v.addElement(new Integer(10));
        Integer a = new Integer(10);
        System.out.println(v.contains(a));
    }

}