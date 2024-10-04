import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Memory {
    public UI ui;
    public static final int MEMORY_SIZE = 16;//整个内存一共有16个物理块
    public  ArrayList<Memory_Block> AllocationMem = new ArrayList<>();//储存内存物理块分配情况的列表

    public ArrayList<Memory_Block> getMemoryStatus() {
        return AllocationMem; // 返回当前内存块的状态
    }//为了让UI可以实时获取内存占用情况

    public  void inial_block() {
        for (int i = 0; i < Memory.MEMORY_SIZE; i++) {
            Memory_Block memoryBlock = new Memory_Block(false, 0, i);
            memoryBlock.setoccupied(0);
            AllocationMem.add(memoryBlock);
        }
    }
    public MMU mmu=new MMU();//内存管理,指令地址转换器
    public  HashMap<Integer, Integer> Allocation_add = new HashMap<>();//储存内存中起始物理地址和连续空间块的的映射
    public  void op_allocation_add() {
        //每一次分配为进程分配内存之后就查找一次，更新Allocation_add这个map集合，
        //每一个健。key代表起始的物理地址
        //每一个key对应的值value代表从key开始连续空闲空间的物理块
        //每一次释放内存之后也要更新
        //总的来说，就是每一次内存空间变化了就要运行这个函数
        Allocation_add.clear(); // 先清空原来的映射
        int startAddress = -1; // 用于记录连续空闲块的起始物理地址
        int freeBlockCount = 0; // 连续空闲块的计数

        for (int i = 0; i < AllocationMem.size(); i++) {
            Memory_Block block = AllocationMem.get(i);

            if (!block.isOccupied()) { // 如果该块未被占用
                if (startAddress == -1) {
                    startAddress =  block.Block_ID * Memory_Block.BLOCK_SIZE; // 记录起始地址
                }
                freeBlockCount++; // 计数器增加
            }
            else {
                if (freeBlockCount > 0) {
                    // 如果之前记录了空闲块，更新映射
                    Allocation_add.put(startAddress, freeBlockCount);
                }
                // 重置计数器
                startAddress = -1;
                freeBlockCount = 0;
            }
        }
        // 处理最后一段连续空闲块
        if (freeBlockCount > 0) {
            Allocation_add.put(startAddress, freeBlockCount);
        }
        //当最后一段内存块是空闲的，并且遍历到最后一块时，
        // 我们没有机会通过占用块来触发前面的 if (freeBlockCount > 0) 来更新映射。
        // 因此，这个逻辑是为了解决 遍历结束时，最后一段连续空闲块没有被记录 的问题。
    }

    public Memory(MMU mmu,UI ui) {
        this.inial_block();
        this.op_allocation_add();
        this.mmu = mmu;
        this.ui=ui;
    }//初始化内存


    public  boolean AllocateMem(PCB pcb)
    {
        int ob_size = pcb.cal_size();
        int block_number = ob_size / Memory_Block.BLOCK_SIZE;

        // 如果大小不是整块，向上取整
        if (ob_size % Memory_Block.BLOCK_SIZE > 0) {
            block_number++;
        }

        int bestFitStartAddress = -1;
        int bestFitBlockSize = Integer.MAX_VALUE; // 选择最小的可用块

        // 遍历 Allocation_add 找到适合的块
        for (Map.Entry<Integer, Integer> entry : Allocation_add.entrySet()) {
            int startAddress = entry.getKey();
            int freeBlocks = entry.getValue();

            // 如果空闲块大于或等于需要的块数，并且是最小的可用块
            if (freeBlocks >= block_number && freeBlocks < bestFitBlockSize) {
                bestFitStartAddress = startAddress;
                bestFitBlockSize = freeBlocks;
            }
        }
        // 如果找到了适合的块，进行分配
        if (bestFitStartAddress != -1) {
            // 将内存块从 bestFitStartAddress 开始分配给 PCB
            int block_id = bestFitStartAddress / Memory_Block.BLOCK_SIZE;//起始的物理块的id
            if (bestFitStartAddress % Memory_Block.BLOCK_SIZE > 0) block_id++;

            int remainingSize = pcb.cal_size(); // 进程的大小

            for (int i = block_id; i < block_id + block_number; i++) {

                AllocationMem.get(i).setOccupied(true);
                AllocationMem.get(i).setPcb_id(pcb.getPid());//设置被内存块占用的pcb的id

                if (remainingSize >= Memory_Block.BLOCK_SIZE) {
                    // 如果剩余大小大于等于块大小，完全占用该块
                    AllocationMem.get(i).setoccupied(Memory_Block.BLOCK_SIZE);
                    remainingSize -= Memory_Block.BLOCK_SIZE; // 减少剩余大小
                }
                else {
                    // 否则，部分占用该块
                    AllocationMem.get(i).setoccupied(remainingSize); // 记录实际占用的大小
                    remainingSize = 0; // 设置为 0，分配完成
                }
                if (remainingSize == 0) {
                    pcb.setPysical_address(block_id * Memory_Block.BLOCK_SIZE);
                    mmu.addresss.put(pcb.getPid(),pcb.getPysical_address());
                    //为新创建的进程添加物理地址
                }
            }
            // 分配成功后，更新 Allocation_add
            op_allocation_add();
            // 在内存分配完成后更新 UI
            SwingUtilities.invokeLater(() -> {
                // 这里可以调用 UI 更新方法
                ui.updateMemoryStatus(AllocationMem); // 更新内存状态
            });
            return true;  // 分配成功
        }
       return false;
    }


    public  void freemem(PCB pcb) {
        // 进程已经运行结束
        if (pcb.getState() == -1)
        {
            // 获取进程的物理地址
            int startPhysicalAddress = pcb.getPysical_address();
            // 计算进程占用的块数
            int blockNumber = pcb.cal_size() / Memory_Block.BLOCK_SIZE;
            if (pcb.cal_size() % Memory_Block.BLOCK_SIZE > 0) {
                blockNumber++; // 如果还有剩余的大小，加一块
            }
            // 释放内存块
            for (int i = 0; i < blockNumber; i++) {
                int block_id = (startPhysicalAddress / Memory_Block.BLOCK_SIZE) + i; // 计算当前块的 ID
                Memory_Block block = AllocationMem.get(block_id); // 获取内存块
                // 标记块为未占用
                block.setOccupied(false);
                block.setoccupied(0); // 重置占用大小
                block.setPcb_id(0);
            }
            // 更新内存映射
            op_allocation_add(); // 重新计算空闲内存块映射
            // 在内存分配完成后更新 UI
            SwingUtilities.invokeLater(() -> {
                // 这里可以调用 UI 更新方法
                ui.updateMemoryStatus(AllocationMem); // 更新内存状态
            });
        }
    }//进程运行结束，释放内存


    public  PCB get_by_id(int id) {
        for (PCB pcb : OSKernel.readyQueue1) {
            if (pcb.getPid() == id) return pcb;
        }
        for (PCB pcb : OSKernel.readyQueue2) {
            if (pcb.getPid() == id) return pcb;
        }
        for (PCB pcb : OSKernel.readyQueue3) {
            if (pcb.getPid() == id) return pcb;
        }
        for (PCB pcb : OSKernel.O_block_queue) {
            if (pcb.getPid() == id) return pcb;
        }
        for (PCB pcb :OSKernel.I_block_queue){
            if(pcb.getPid()==id) return pcb;
        }
        return null;
    }


    public  int check_number_pcb() {
        return (OSKernel.O_block_queue.size() +
                OSKernel.readyQueue1.size() +
                OSKernel.readyQueue2.size() +
                OSKernel.readyQueue3.size()+
                OSKernel.O_block_queue.size());
    }//返回内存中的进程个数，最大只能有12个进程并发


}


