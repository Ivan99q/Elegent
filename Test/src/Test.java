import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;
public class Test{
    static Timer timer;
    static MyPanel MP;
    static Windows W;
    static Thread refresh;
    static Thread start;
    public static void main(String[] args){
        MP=new MyPanel();
        W=new Windows(MP);
        refresh=new Thread(new F5());
        start=new Thread(new Start());
    }
}
class Memory{
    static Memory[] Table=new Memory[2048];
    static int count=0;
    boolean isf;
    int start;
    int end;
    int size;
    static Object[][] MTable=new Object[1][3];
    static{
        MTable[0][0]=Integer.toString(0);
        MTable[0][1]=Integer.toString(2047);
        MTable[0][2]=Boolean.toString(true);
    }
    public Memory(int s,int e,boolean i){
        start=s;
        end=e;
        size=e-s+1;
        isf=i;
        count++;
    }
    public static void init(){
        int s=0;
        int e=-1;
        int i;
        PCB.listPcbs[0].setStatus("running");
        for(i=0;i<PCB.N;i++){
            if(e+PCB.listPcbs[i].Size>2047){
                continue;
            }
            e=e+PCB.listPcbs[i].Size;
            Table[count]=new Memory(s,e,false);
            PCB.listPcbs[i].AS=Table[count-1].start;
            PCB.listPcbs[i].AE=Table[count-1].end;
            s=e+1;
        }
        if(s<2047){
            Table[count]=new Memory(s,2047,true);
        }
        Table=Arrays.copyOf(Table, count);
        for(i=1;i<PCB.N;i++){
            if(PCB.listPcbs[i].AS!=-1){
                PCB.listPcbs[i].setStatus("ready");
            }
            if(PCB.listPcbs[i].AS==-1){
                PCB.listPcbs[i].setStatus("waiting");
            }
        }        
        PCB.Ptable=Arrays.copyOf(PCB.Ptable,PCB.N);
        for(int k=0;k<PCB.N;k++){
            PCB.Ptable[k][0]=Integer.toString(PCB.listPcbs[k].Pid);
            PCB.Ptable[k][1]=Integer.toString(PCB.listPcbs[k].Runtime);
            PCB.Ptable[k][2]=Integer.toString(PCB.listPcbs[k].Priority);
            PCB.Ptable[k][3]=Integer.toString(PCB.listPcbs[k].Size);
            PCB.Ptable[k][4]=PCB.listPcbs[k].Status;
            PCB.Ptable[k][5]=Integer.toString(PCB.listPcbs[k].AS);
            PCB.Ptable[k][6]=Integer.toString(PCB.listPcbs[k].AE);
        }
    }
    public static void combine(){
        for(int i=0;i<count-1;){
            if(Table[i].isf&&Table[i+1].isf){
                count-=2;
                Table[i]=new Memory(Table[i].start,Table[i+1].end,true);
                for(int j=i+1;j<count;j++){
                    Table[j]=Table[j+1];
                }
                Table=Arrays.copyOf(Table,count);
            }
            else{
                i++;
            }
        }
    }
    public static void allocate(){
        for(int i=0;i<PCB.N;i++){
            if(PCB.listPcbs[i].AS==-1&&!PCB.listPcbs[i].suspend){
                for(int j=0;j<count;j++){
                    if(Table[j].size>PCB.listPcbs[i].Size&&Table[j].isf){
                        int t=Table[j].end;
                        count--;
                        Table[j]=new Memory(Table[j].start,Table[j].start+PCB.listPcbs[i].Size-1,false);
                        Table=Arrays.copyOf(Table,count+1);
                        for(int k=count;k>j+1;k--){
                            Table[k]=Table[k-1];
                        }
                        Table[j+1]=new Memory(Table[j].end+1,t,true);
                        PCB.listPcbs[i].AS=Table[j].start;
                        PCB.listPcbs[i].AE=Table[j].end;
                        Table[j].isf=false;
                        break;
                    }
                    if(Table[j].size==PCB.listPcbs[i].Size&&Table[j].isf){
                        PCB.listPcbs[i].AS=Table[j].start;
                        PCB.listPcbs[i].AE=Table[j].end;
                        Table[j].isf=false;
                        break;
                    }
                }
            }
        }
        PCB.Ptable=Arrays.copyOf(PCB.Ptable,PCB.N);
        for(int k=0;k<PCB.N;k++){
            PCB.Ptable[k][0]=Integer.toString(PCB.listPcbs[k].Pid);
            PCB.Ptable[k][1]=Integer.toString(PCB.listPcbs[k].Runtime);
            PCB.Ptable[k][2]=Integer.toString(PCB.listPcbs[k].Priority);
            PCB.Ptable[k][3]=Integer.toString(PCB.listPcbs[k].Size);
            PCB.Ptable[k][4]=PCB.listPcbs[k].Status;
            PCB.Ptable[k][5]=Integer.toString(PCB.listPcbs[k].AS);
            PCB.Ptable[k][6]=Integer.toString(PCB.listPcbs[k].AE);
        }
        MTable=Arrays.copyOf(MTable,count);
        for(int k=0;k<MTable.length;k++){
            MTable[k][0]=Integer.toString(Table[k].start);
            MTable[k][1]=Integer.toString(Table[k].end);
            MTable[k][2]=Boolean.toString(Table[k].isf);
            if(k+1<MTable.length){
                MTable[k+1]=new Object[3];
            }
        }
        MTable=Arrays.copyOf(MTable,count);
    }
    public static boolean sizeenough(){
        for(int i=0;i<count;i++){
            if(Table[i].size>PCB.listPcbs[0].Size){
                return true;
            }
        }
        return false;
    }
    public static void run(){
        combine();
        if(PCB.N==0){
            return;
        }
        if(PCB.listPcbs[0].AS==-1){
            for(int j=PCB.N-1;j>0;j--){
                if(PCB.listPcbs[j].AS==-1){
                    continue;
                }
                for(int i=0;i<count;i++){
                    if(Table[i].start==PCB.listPcbs[j].AS){
                        Table[i].isf=true;
                        PCB.listPcbs[j].AS=-1;
                        PCB.listPcbs[j].AE=-1;
                        combine();
                        break;
                    }
                }
                if(sizeenough()){
                    break;
                }
            }
        }
        allocate();
    }
    public static void printInfo(){
        for(int i=0;i<count;i++){
            System.out.println(Table[i].start+'\t'+Table[i].end+'\t'+Table[i].size+'\t'+Boolean.toString(Table[i].isf));
        }
    }
}
class PCB {
    static int N=0;
    static int countSuspend=0;
    int Pid;
    int Runtime;
    int Size;
    int Priority;
    int AS=-1;
    int AE=-1;
    String Status="waiting";
    boolean Pattribute;
    boolean suspend=false;
    static PCB[] listPcbs={
        new PCB(0,4),
        new PCB(1,3),
        new PCB(2,1),
        new PCB(3,2),
        new PCB(4,5),
    };
    static{
        for(int i=0;i<N-1;i++){
            for(int j=i+1;j<N;j++){
                if(listPcbs[i].Priority>listPcbs[j].Priority){
                   PCB t=listPcbs[j];
                   listPcbs[j]=listPcbs[i];
                   listPcbs[i]=t;
                }
            }
        }    
    }
    static Object[][] Ptable=new Object[N][7];
    static{
        for(int i=0;i<PCB.N;i++){
            Ptable[i][0]=Integer.toString(listPcbs[i].Pid);
            Ptable[i][1]=Integer.toString(listPcbs[i].Runtime);
            Ptable[i][2]=Integer.toString(listPcbs[i].Priority);
            Ptable[i][3]=Integer.toString(listPcbs[i].Size);
            Ptable[i][4]=listPcbs[i].Status;
            Ptable[i][5]=Integer.toString(listPcbs[i].AS);
            Ptable[i][6]=Integer.toString(listPcbs[i].AE);
        }
    }
    public PCB(int pid,int priority){
        N++;
        Pid=pid;
        Runtime=new Random().nextInt(1,10);
        Size=new Random().nextInt(1,1024);
        Priority=priority;
        Pattribute=false;
    }
    public PCB(int pid,int runtime,int priority,int size){
        N++;
        Pid=pid;
        Runtime=runtime;
        Priority=priority;
        Size=size;        
        Pattribute=false;
    }
    public static void Cancel(){
        int a=listPcbs[0].AS;
        for(int i=0;i<N-1;i++){
            listPcbs[i]=listPcbs[i+1];
        }
        N--;
        listPcbs=Arrays.copyOf(listPcbs,N);
        for(int i=0;i<Memory.count;i++){
            if(Memory.Table[i].start==a){
                Memory.Table[i].isf=true;
                break;
            }
        }
    }
    public void setPriority(int priority) {
        Priority=priority;
    }
    public void setStatus(String status) {
        Status=status;
    }
    public void PrintInfo(){
        System.out.println(Integer.toString(Pid)+'\t'+Integer.toString(Runtime)+'\t'+Integer.toString(Size)+'\t'+Integer.toString(Priority)+'\t'+Status+'\t'+Integer.toString(AS)+'\t'+Integer.toString(AE));
    }
}
class Ready{
    private Ready(){
    }
    public static void run(){
        if(PCB.listPcbs[0].Runtime==0){
            PCB.Cancel();
        }
        if(F5.Inserted){
            int pid=Integer.parseInt(Test.W.mp.pid.getText());
            int rt=Integer.parseInt(Test.W.mp.runtime.getText());
            int pr=Integer.parseInt(Test.W.mp.priority.getText());
            int sz=Integer.parseInt(Test.W.mp.size.getText());
            PCB.listPcbs=Arrays.copyOf(PCB.listPcbs,PCB.N+1);
            PCB.listPcbs[PCB.N]=new PCB(pid,rt,pr,sz);
            PCB.Ptable=Arrays.copyOf(PCB.Ptable,PCB.N);
            PCB.Ptable[PCB.N-1]=new Object[7];
            PCB.Ptable[PCB.N-1][0]=Integer.toString(PCB.listPcbs[PCB.N-1].Pid);
            PCB.Ptable[PCB.N-1][1]=Integer.toString(PCB.listPcbs[PCB.N-1].Runtime);
            PCB.Ptable[PCB.N-1][2]=Integer.toString(PCB.listPcbs[PCB.N-1].Priority);
            PCB.Ptable[PCB.N-1][3]=Integer.toString(PCB.listPcbs[PCB.N-1].Size);
            PCB.Ptable[PCB.N-1][4]=PCB.listPcbs[PCB.N-1].Status;
            PCB.Ptable[PCB.N-1][5]=Integer.toString(PCB.listPcbs[PCB.N-1].AS);
            PCB.Ptable[PCB.N-1][6]=Integer.toString(PCB.listPcbs[PCB.N-1].AE);
            F5.Inserted=false;
        }
        int c=0;
        for(int i=0;i<PCB.N;i++){
            if(PCB.listPcbs[i].suspend){
                c++;
            }
        }
        PCB.countSuspend=c;
        if(PCB.countSuspend<PCB.N){
            for(int i=0;i<PCB.N;i++){
                if(PCB.listPcbs[i].suspend){
                    for(int j=PCB.N-1;i<j;j--){
                        if(!PCB.listPcbs[j].suspend){
                            PCB t=PCB.listPcbs[i];
                            PCB.listPcbs[i]=PCB.listPcbs[j];
                            PCB.listPcbs[j]=t;
                            break;
                        }
                    }
                }
            }
            for(int i=0;i<PCB.N-1-PCB.countSuspend;i++){
                for(int j=i+1;j<PCB.N-PCB.countSuspend;j++){
                    if(PCB.listPcbs[i].Priority>PCB.listPcbs[j].Priority){
                       PCB t=PCB.listPcbs[j];
                       PCB.listPcbs[j]=PCB.listPcbs[i];
                       PCB.listPcbs[i]=t;
                    }
                }
            }    
        }
        for(int i=0;i<PCB.N;i++){
            if(PCB.listPcbs[i].suspend&&PCB.listPcbs[i].AS!=-1){
                for(int j=0;j<Memory.count;j++){
                    if(Memory.Table[j].start==PCB.listPcbs[i].AS){
                        Memory.Table[j].isf=true;
                    }
                }
                PCB.listPcbs[i].AS=-1;
                PCB.listPcbs[i].AE=-1;
            }
        }
        Memory.run();
        if(PCB.N>0&&PCB.listPcbs[0].AS==-1){
            Memory.run();
        }
        if(PCB.N==0){
            return;
        }
        if(!PCB.listPcbs[0].suspend){
            PCB.listPcbs[0].setStatus("running");
        }
        else{
            PCB.listPcbs[0].setStatus("suspend");
        }
        for(int i=1;i<PCB.N;i++){
            if(PCB.listPcbs[i].AS==-1){
                PCB.listPcbs[i].setStatus("waiting");
            }
            if(PCB.listPcbs[i].AS!=-1){
                PCB.listPcbs[i].setStatus("ready");
            }
            if(PCB.listPcbs[i].suspend){
                PCB.listPcbs[i].setStatus("suspend");
            }
        }
        if(!PCB.listPcbs[0].suspend){
            PCB.Ptable[0][4]="running";        
        }
        else{
            PCB.Ptable[0][4]="suspend";        
        }
        for(int i=1;i<PCB.N;i++){
            if(PCB.listPcbs[i].AS==-1){
                PCB.Ptable[i][4]="waiting";
            }
            if(PCB.listPcbs[i].AS!=-1){
                PCB.Ptable[i][4]="ready";
            }
            if(PCB.listPcbs[i].suspend){
                PCB.Ptable[i][4]="suspend";
            }
        }
    }
}
class Running{
    private Running(){
    }
    static{
    }
    public static void run(){
        if(PCB.N==0){
            return;
        }
        if(!PCB.listPcbs[0].suspend){
            PCB.listPcbs[0].Runtime--;
            PCB.listPcbs[0].Priority++;    
        }
    }
}
class Windows extends JFrame{
    public MyPanel mp;
    public Windows(MyPanel m){
        mp=m;
        this.add(m);
        this.setVisible(true);
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle("模拟处理机调度以及主存分配与回收");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
class ProcTableModel extends AbstractTableModel{
    public final String[] columnNames={"Pid","剩余时间","优先权","大小","状态","始地址","末地址"};
    public Object[][] rowData=null;
    public ProcTableModel(Object[][] rowData){
        this.rowData=rowData;
    }
    public void clear(){
        rowData=null;
    }
    public int getRowCount(){
        return rowData.length;
    }
    public int getColumnCount(){
        return columnNames.length;    
    }
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    public Object getValueAt(int rowIndex, int columnIndex){
        return rowData[rowIndex][columnIndex];    
    }
    public void refresh(Object[][] rowData){
        this.rowData=rowData;
        this.fireTableDataChanged();
    }
}
class MemoryTableModel extends AbstractTableModel{
    public final String[] columnNames={"始地址","末地址","是否空闲"};
    public Object[][] rowData=null;
    public MemoryTableModel(Object[][] rowData){
        this.rowData=rowData;
    }
    public void clear(){
        rowData=null;
    }
    public int getRowCount(){
        return rowData.length;
    }
    public int getColumnCount(){
        return columnNames.length;    
    }
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    public Object getValueAt(int rowIndex, int columnIndex){
        return rowData[rowIndex][columnIndex];    
    }
    public void refresh(Object[][] rowData){
        this.rowData=rowData;
        this.fireTableDataChanged();
    }
}
class MyPanel extends JPanel{
    public JLabel pidLabel;
    public JLabel runtimeLabel;
    public JLabel prorityLabel;    
    public JLabel sizeLabel;
    public JLabel Procs;
    public JLabel Memorys;
    public JTextField pid;
    public JTextField runtime;
    public JTextField priority;
    public JTextField size;
    public JButton Insert;
    public JButton Start;
    public JButton Suspend;
    public ProcTableModel Procmodel=new ProcTableModel(PCB.Ptable);
    public JTable ProcTable;
    public MemoryTableModel Memorymodel=new MemoryTableModel(Memory.MTable);
    public JTable MemoryTable;
    public JScrollPane ProcSP;
    public JScrollPane MemorySP;
    public MyPanel(){
        setLayout(null);
        pidLabel=new JLabel("Pid");
        runtimeLabel=new JLabel("运行时间");
        prorityLabel=new JLabel("优先权");
        sizeLabel=new JLabel("大小");
        Procs=new JLabel("进程");
        Memorys=new JLabel("内存");
        pid=new JTextField();
        runtime=new JTextField();
        priority=new JTextField();
        size=new JTextField();
        Insert=new JButton("插入进程");
        Start=new JButton("开始/暂停");
        Suspend=new JButton("挂起/解挂");
        ProcTable=new JTable(Procmodel);
        MemoryTable=new JTable(Memorymodel);
        ProcSP=new JScrollPane(ProcTable);
        MemorySP=new JScrollPane(MemoryTable);
        add(pidLabel);
        add(pid);
        add(runtimeLabel);
        add(runtime);
        add(prorityLabel);
        add(priority);
        add(sizeLabel);
        add(size);
        add(Procs);
        add(Memorys);
        add(Insert);
        add(Start);
        add(Suspend);
        add(ProcSP);
        add(MemorySP);
        pidLabel.setBounds(150,30,90,30);
        pid.setBounds(175,30,40,30);
        runtimeLabel.setBounds(225,28,90,30);
        runtime.setBounds(280,30,40,30);
        prorityLabel.setBounds(330,28,90,30);
        priority.setBounds(375,30,40,30);
        sizeLabel.setBounds(425,28,90,30);
        size.setBounds(455,30,40,30);
        Procs.setBounds(220,150,90,30);
        Memorys.setBounds(590,150,90,30);
        Insert.setBounds(530,30,90,30);
        Insert.setFocusable(false);
        Start.setFocusable(false);
        Start.setBounds(450,100,90,30);
        Suspend.setFocusable(false);
        Suspend.setBounds(280,100,90,30);
        ProcTable.getTableHeader().setResizingAllowed(false);
        ProcTable.getTableHeader().setReorderingAllowed(false);
        MemoryTable.getTableHeader().setResizingAllowed(false);
        MemoryTable.getTableHeader().setReorderingAllowed(false);
        ProcTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ProcTable.addMouseListener(new MouseAdpter());
        MemoryTable.setEnabled(false);
        ProcSP.setBounds(30,175,400,250);
        MemorySP.setBounds(500,175,200,250);
        Start.addActionListener(new StartListener());
        Insert.addActionListener(new InsertListener());
        Suspend.addActionListener(new SuspendListener());
        pid.addKeyListener(new KeyAdapter());
        runtime.addKeyListener(new KeyAdapter());
        priority.addKeyListener(new KeyAdapter());
        size.addKeyListener(new KeyAdapter());
    }
}
class MouseAdpter implements MouseListener{
    public static int flag=-1;
    public void mouseClicked(MouseEvent e){
        flag=Test.W.mp.ProcTable.getSelectedRow();
    }
    public void mousePressed(MouseEvent e){
    }
    public void mouseReleased(MouseEvent e){
    }
    public void mouseEntered(MouseEvent e){
    }
    public void mouseExited(MouseEvent e){
    }
} 
class KeyAdapter implements KeyListener{
    public void keyTyped(KeyEvent e){
        int keyChar=e.getKeyChar();				
        if(keyChar>=KeyEvent.VK_0&&keyChar<=KeyEvent.VK_9){
        }
        else{
            e.consume();
        }
    }
    public void keyPressed(KeyEvent e){
    }
    public void keyReleased(KeyEvent e){
    }
}
class StartListener implements ActionListener{
    public static int c=1;
    public void actionPerformed(ActionEvent e){
        while(c>0){
            Memory.init();
            Test.refresh.start();
            Test.start.start();
            c--;
        }
        if(F5.isStart){
            F5.isStart=false;
        }
        else if(!F5.isStart){
            F5.isStart=true;
        }
    }
}
class Start extends Thread{
    public void run(){
        while(PCB.N!=0){
            if(!F5.isStart){
                Test.refresh.suspend();
                Test.W.mp.ProcTable.setEnabled(true);
            }
            else if(F5.isStart){
                Test.refresh.resume();
                Test.W.mp.ProcTable.setEnabled(false);
                MouseAdpter.flag=-1;
            }
        }
    }
}
class InsertListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
        F5.Inserted=true;
    }
}
class SuspendListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
        if((MouseAdpter.flag!=-1)&&!PCB.listPcbs[MouseAdpter.flag].suspend){
            for(int i=0;i<Memory.count;i++){
                if(Memory.Table[i].start==PCB.listPcbs[MouseAdpter.flag].AS){
                    Memory.Table[i].isf=true;
                }
            }
            PCB.listPcbs[MouseAdpter.flag].AS=-1;
            PCB.listPcbs[MouseAdpter.flag].AE=-1;
            PCB.listPcbs[MouseAdpter.flag].suspend=true;
        }
        else if((MouseAdpter.flag!=-1)&&PCB.listPcbs[MouseAdpter.flag].suspend){
            PCB.listPcbs[MouseAdpter.flag].suspend=false;
            PCB.countSuspend--;
            for(int i=0;i<MouseAdpter.flag;i++){
                if(PCB.listPcbs[i].suspend){
                    PCB t=PCB.listPcbs[i];
                    PCB.listPcbs[i]=PCB.listPcbs[MouseAdpter.flag];
                    PCB.listPcbs[MouseAdpter.flag]=t;
                    break;
                }
            }
        }
    }
}
class F5 extends Thread{
    public static boolean isStart=false;
    public static boolean Inserted=false;
    public void run(){
        while(PCB.N!=0){
            Ready.run();
            Running.run();
            Test.W.mp.Procmodel.refresh(PCB.Ptable);
            Test.W.mp.Memorymodel.refresh(Memory.MTable);
            try{
                Thread.sleep(1000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        Memory.MTable[0][0]=Integer.toString(0);
        Memory.MTable[0][1]=Integer.toString(2047);
        Memory.MTable[0][2]=Boolean.toString(true);
        Memory.MTable=Arrays.copyOf(Memory.MTable,1);
        PCB.Ptable[0][0]="";
        PCB.Ptable[0][1]="";
        PCB.Ptable[0][2]="";
        PCB.Ptable[0][3]="";
        PCB.Ptable[0][4]="";
        PCB.Ptable[0][5]="";
        PCB.Ptable[0][6]="";
        PCB.Ptable=Arrays.copyOf(PCB.Ptable,1);
        Test.W.mp.Procmodel.refresh(PCB.Ptable);
        Test.W.mp.Memorymodel.refresh(Memory.MTable);
        Test.W.mp.updateUI();
    }
}