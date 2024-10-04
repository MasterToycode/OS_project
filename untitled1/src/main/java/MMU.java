import java.util.HashMap;
import java.util.HashSet;

public class MMU
{
    public  HashMap<Integer,Integer> addresss=new HashMap<>();
    //内存分配的映射表，用于存放进程的pid和物理地址的映射关系
    public static final int Baseaddress=0;//进程起始的物理地址
    public  int Address_transfomer(PCB pcb){
        return (pcb.getPysical_address()+Baseaddress+pcb.getCal_pc()*100);
    }

}
