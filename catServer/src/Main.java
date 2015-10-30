public class Main {
    public static void main(String[] args){
        CatServer cs = new CatServer();
        try {
            while (true)
                cs.startServer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
