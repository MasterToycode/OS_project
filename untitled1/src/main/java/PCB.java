import java.util.ArrayList;
import java.util.*;
public class PCB extends Job {
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }
    private int pid;//PCB的ID
    private int pc; //程序计数器，存放下一条执行的指令
    private int state; // 进程状态
    private int pysical_address;//PCB的起始物理地址
    private int address;//PCB的逻辑地址
    private int size;//用于计算PCB占用的内存大小
    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }
    private int start_time;//进程的创建时刻
    public int getTatalruntimes() {
        return tatalruntimes;
    }
    public void setTatalruntimes(int tatalruntimes) {
        this.tatalruntimes = tatalruntimes;
    }
    private int tatalruntimes;//进程的结束时刻
    public ArrayList<Integer> read_in_queue=new ArrayList<>();//存放该进程每次进入就绪队列的时刻
    private int timesilve;

    private int cal_pc=0;//记录已经执行了的计算类的指令

    public void note_cal(){
        this.cal_pc++;
    }

    public int getCal_pc() {
        return cal_pc;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getIr() {
        if (this.pc<this.getInstructionCount()){
        return this.getInstructions().get(this.pc).getState();}
        else if (this.pc>=this.getInstructionCount()) {
            System.out.println("PC越界");
            return -1;
        }
        return -1;
    }


    public int getTimesilve() {
        return timesilve;
    }

    public void setTimesilve(int timesilve) {
        this.timesilve = timesilve;
    }

    public int getPysical_address() {
        return pysical_address;
    }
    public void setPysical_address(int pysical_address) {
        this.pysical_address = pysical_address;
    }

    public PCB(int jobId, int inTime, int instructionCount, int pid, ArrayList<Instruction> instructions)
    {
        super(jobId, inTime, instructionCount,instructions);
        this.pid = pid;
        this.pc = 0;
        this.state = 0; // Assume 0 represents a ready state
        this.pysical_address=0;
        this.address=0;
        this.start_time = 0;
        this.timesilve=1;
        this.tatalruntimes=0;
        this.cal_pc=0;
    }

    //0 代表就绪态
    //1 代表运行态
    //-1 代表阻塞态


    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }

    public int cal_size(){
        int a=0;
        for(Instruction instruction :super.getInstructions()){
            if (instruction.getState()==0){
                a++;
            }
        }
        return a*100;
    }//计算一个进程的占用的内存大小

    public  String get_ir_state(){
        //检查PC是否越界
        if(this.pc>=super.getInstructionCount())
        {
            return "";
        }
        switch (this.getIr()){
            case 0:  return "计算类指令";
            case 1:  return "键盘输入";
            case 2:  return "屏幕输出";
        }
        return "";
    }
}
