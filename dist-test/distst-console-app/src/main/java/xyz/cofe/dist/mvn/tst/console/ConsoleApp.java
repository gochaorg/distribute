package xyz.cofe.dist.mvn.tst.console;

public class ConsoleApp {
    public static void main(String[] args){
        System.out.println("console app sample");
        for( int i=0; i<args.length; i++ ){
            System.out.println("arg["+i+"]="+args[i]);
        }
    }
}
