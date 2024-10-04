import java.util.ArrayList;


public class Job {
    public int jobId;
    public int inTime;
    public int instructionCount;//存放指令的数目
    public  ArrayList<Instruction> instructions=new ArrayList<>();//作业的指令集
    public Job(int jobId, int inTime, int instructionCount, ArrayList<Instruction> instructions){
        this.jobId=jobId;
        this.inTime=inTime;
        this.instructionCount=instructionCount;
        this.instructions=instructions;
    }

    public int getJobId() {
        return jobId;
    }

    public int getInTime() {
        return inTime;
    }
    public int getInstructionCount() {
        return instructionCount;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

}