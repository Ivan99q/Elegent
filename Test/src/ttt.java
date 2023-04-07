public class ttt{
    public static void main(String[] args) {
        Thread t=new Thread(new tt());
        t.start();
    }
}
class tt extends Thread{
    public void run(){
        int count=0;
        System.out.println("Pid  剩余时间\t大小  优先权     状态     "+(count++)+"S");
        for(int i=0;i<PCB.N;i++){
            PCB.listPcbs[i].PrintInfo();
        }
        for(int i=0;i<Memory.count;i++){
            System.out.println(Integer.toString(Memory.Table[i].start)+'\t'+Integer.toString(Memory.Table[i].end)+'\t'+Boolean.toString(Memory.Table[i].isf));
        }
        System.out.println("-------------------------------------------------------------------");
        System.out.println("Pid  剩余时间\t大小  优先权     状态     "+(count++)+"S");
        Memory.init();
        for(int i=0;i<PCB.N;i++){
            PCB.listPcbs[i].PrintInfo();
        }
        for(int i=0;i<Memory.count;i++){
            System.out.println(Integer.toString(Memory.Table[i].start)+'\t'+Integer.toString(Memory.Table[i].end)+'\t'+Boolean.toString(Memory.Table[i].isf));
        }
        System.out.println("-------------------------------------------------------------------");
        while(PCB.N!=0){
            Ready.run();
            Running.run();
            System.out.println("Pid  剩余时间\t大小  优先权     状态     "+(count++)+"S");
            for(int i=0;i<PCB.N;i++){
                PCB.listPcbs[i].PrintInfo();
            }            
            for(int i=0;i<Memory.count;i++){
                System.out.println(Integer.toString(Memory.Table[i].start)+'\t'+Integer.toString(Memory.Table[i].end)+'\t'+Boolean.toString(Memory.Table[i].isf));
            }
            System.out.println(Memory.count);
            System.out.println("-------------------------------------------------------------------");
            try{
                Thread.sleep(1000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}